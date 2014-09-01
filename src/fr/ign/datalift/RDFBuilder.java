/*
 * Copyright / Copr. IGN 2013
 * Contributor(s) : Faycal Hamdi
 *
 * Contact: hamdi.faycal@gmail.com
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.ign.datalift;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.WordUtils;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
//import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.io.ParseException;

import fr.ign.datalift.constants.GeoSPARQL;
import fr.ign.datalift.constants.Geometrie;
import fr.ign.datalift.constants.CRS;
import fr.ign.datalift.model.AbstractFeature;
import fr.ign.datalift.model.FeatureProperty;
import fr.ign.datalift.model.GeometryProperty;
import fr.ign.datalift.parser.Features_Parser;
import fr.ign.datalift.repository.SesameConnection;

public class RDFBuilder extends SesameConnection {

	protected ValueFactory vf;
	protected Statement geoStatement;
	private int count;
	private List<Statement> aboutGeometry;
	private Resource context;
	private String featureNS = "http://localhost:8080/test/";

	static Logger log = Logger.getLogger(RDFBuilder.class.getName());

	public RDFBuilder() throws RepositoryException {
		vf = super.getVf();
		super.connectSesameRepository();
	}

	public void translateToRDF(ArrayList<AbstractFeature> featureList, String crs, ArrayList<String> asGmlList, 
			URI context, String typeName, String vocabNS, boolean fileOut, String fileOutPath)
					throws RepositoryException, IOException, RDFParseException, RDFHandlerException, ParseException {

		this.context = context;
		//String cleanedFT = this.cleanUpString(typeName);
		String cleanedFT = typeName;
		this.featureNS = this.featureNamespace(context.toString(), cleanedFT);

		Statement statement;
		List<Statement> aboutAttributes = new ArrayList<Statement>();
		aboutGeometry = new ArrayList<Statement>();

		// create blank node for CRS
		/*BNode systcoord = vf.createBNode();
		if (crs != null) {
			statement = vf.createStatement(systcoord, RDFS.LABEL, vf.createLiteral(crs));
			aboutGeometry.add(statement);
		}*/

		if (crs != null) CRS.setCrsValue(crs);

		// serialize a featureCollection into RDF
		for (int i = 0; i < featureList.size(); i++) {
			count = i + 1;
			URI feature = vf.createURI(featureNS, cleanedFT + "_" + count);

			statement = vf.createStatement(feature, RDF.TYPE, vf.createURI(featureNS));
			aboutAttributes.add(statement);

			ArrayList<FeatureProperty> featureProperties = (ArrayList<FeatureProperty>) featureList.get(i).getProperties();

			for (int j = 0; j < featureProperties.size(); j++) {

				FeatureProperty fp = featureProperties.get(j);

				if (fp instanceof GeometryProperty) {

					GeometryProperty gp = (GeometryProperty)fp;

					String geoType = gp.getType();

					//URI geomFeature = vf.createURI(featureNS, this.cleanUpString(geoType) + "_" + count);
					URI geomFeature = vf.createURI(featureNS, geoType + "_" + count);

					geoStatement = vf.createStatement(feature, Geometrie.GEOMETRIE, geomFeature);
					aboutGeometry.add(geoStatement);

					geoStatement = vf.createStatement(geomFeature, GeoSPARQL.ASWKT, vf.createLiteral("<" + CRS.IGNFCRS + "> " + gp.getValue(),GeoSPARQL.WKTLITERAL));
					aboutGeometry.add(geoStatement);

					if (asGmlList != null) {
						geoStatement = vf.createStatement(geomFeature, GeoSPARQL.ASGML, vf.createLiteral(asGmlList.get(j)));
						aboutGeometry.add(geoStatement);
					}

					if (geoType.equals("MultiPolygon")){
						this.serializeMultipolygon(gp,geomFeature,crs);
					}

					if (geoType.equals("MultiLineString")){
						this.serializeMultiLineString(gp,geomFeature,crs);
					}

					if (geoType.equals("MultiPoint")){
						this.serializeMultiPoint(gp,geomFeature,crs);
					}

					if (geoType.equals("Polygon")){
						this.serializePolygon(gp,geomFeature,crs);
					}

					if (geoType.equals("LineString")){
						this.serializeLineString(gp,geomFeature,crs,0);
					}

					if (geoType.equals("LinearRing")){
						this.serializeLineString(gp,geomFeature,crs,0);
					}
					if (geoType.equals("Point")){
						this.serializePoint(gp,geomFeature,crs,0);
					}

				} else {
					if (fp.getType() != null){
						if (fp.getType().contains("int")) {
							statement = vf.createStatement(feature, vf.createURI(vocabNS + fp.getName()), vf.createLiteral(fp.getIntValue()));
							aboutAttributes.add(statement);
						} else {
							statement = vf.createStatement(feature, vf.createURI(vocabNS + fp.getName()), vf.createLiteral(fp.getDoubleValue()));
							aboutAttributes.add(statement);
						}
					} else {
						statement = vf.createStatement(feature, vf.createURI(vocabNS + fp.getName()), vf.createLiteral(fp.getValue()));
						aboutAttributes.add(statement);
					}
				}
			}
		}
		if (!fileOut){
			sesameRepository.getConnection().clear(this.context);
			sesameRepository.getConnection().add(aboutAttributes, this.context);
			sesameRepository.getConnection().add(aboutGeometry, this.context);
			sesameRepository.getConnection().commit();
			sesameRepository.getConnection().close();
		}
		else{
			FileOutputStream out = new FileOutputStream(fileOutPath);
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
			try {
				writer.startRDF();
				for (Statement st: aboutAttributes) {
					writer.handleStatement(st);
				}
				for (Statement st: aboutGeometry) {
					writer.handleStatement(st);
				}
				writer.endRDF();
			}
			catch (RDFHandlerException e) {
				// oh no, do something!
			}
		}

	}

	private String featureNamespace(String context, String featureTypeName) {
		String featureNS = context + "/" + featureTypeName + "/";
		return featureNS;
	}

	protected String cleanUpString(String str) {
		if (str.contains(":"))
			str = str.substring(str.lastIndexOf(':') + 1);
		return WordUtils.capitalizeFully(str, new char[] { ' ' }).replaceAll(" ", "").trim();
	}


	// serialize Geometry Features into RDF

	protected void serializeMultipolygon(GeometryProperty gp, Resource geomFeature, String crs){
		geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.MULTIPOLYGON);
		aboutGeometry.add(geoStatement);
		this.setCrs(geomFeature,crs);
		int n = gp.getNumGeometries(); 
		for (int i = 0; i < n; i++) {
			BNode polygonMember = vf.createBNode();
			geoStatement = vf.createStatement(geomFeature, Geometrie.POLYGONMEMBER, polygonMember);
			aboutGeometry.add(geoStatement);
			this.serializePolygon(gp, polygonMember, crs);
		}
	}

	protected void serializeMultiLineString(GeometryProperty gp, Resource geomFeature, String crs){
		geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.MULTILINESTRING);
		aboutGeometry.add(geoStatement);
		this.setCrs(geomFeature,crs);
		int n = gp.getNumGeometries(); 
		for (int i = 0; i < n; i++) {
			BNode lineStringMember = vf.createBNode();
			geoStatement = vf.createStatement(geomFeature, Geometrie.LINESTRINGMEMBER, lineStringMember);
			aboutGeometry.add(geoStatement);
			this.serializeLineString(gp, lineStringMember, crs, i);
		}
	}

	protected void serializeMultiPoint(GeometryProperty gp, Resource geomFeature, String crs){
		geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.MULTIPOINT);
		aboutGeometry.add(geoStatement);
		this.setCrs(geomFeature,crs);
		int n = gp.getNumGeometries(); 
		for (int i = 0; i < n; i++) {
			BNode pointMember = vf.createBNode();
			geoStatement = vf.createStatement(geomFeature, Geometrie.POINTMEMBER, pointMember);
			aboutGeometry.add(geoStatement);
			this.serializePoint(gp, pointMember, crs, i);
		}
	}

	protected void serializePolygon(GeometryProperty gp, Resource geomFeature, String crs){
		int indexPointList = 0;
		geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.POLYGON);
		aboutGeometry.add(geoStatement);
		this.setCrs(geomFeature,crs);
		BNode exterior = vf.createBNode();
		geoStatement = vf.createStatement(geomFeature, Geometrie.EXTERIOR, exterior);
		aboutGeometry.add(geoStatement);
		this.serializeLineString(gp, exterior, crs, indexPointList);
		int n = gp.getNumInteriorRing();
		for (int i = 0; i < n; i++) {
			indexPointList++;
			BNode interiorMember = vf.createBNode();
			geoStatement = vf.createStatement(geomFeature, Geometrie.INTERIOR, interiorMember);
			aboutGeometry.add(geoStatement);
			this.serializeLineString(gp, interiorMember, crs, indexPointList);
		}
	}

	protected void serializeLineString(GeometryProperty gp, Resource geomFeature, String crs, int indexPointList){
		if (gp.getIsRing(indexPointList)) {
			geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.LINEARRING);
			aboutGeometry.add(geoStatement);
			this.setCrs(geomFeature,crs);
		}
		else {			
			// carry out the Line case (when numPoints = 2)
			if (gp.getNumPoint(indexPointList) == 2){
				geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.LINE);
				aboutGeometry.add(geoStatement);
				this.setCrs(geomFeature,crs);
			}
			else{
				geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.LINESTRING);
				aboutGeometry.add(geoStatement);
				this.setCrs(geomFeature,crs);
			}
		}
		////// Add  the line Case

		BNode points = vf.createBNode();
		geoStatement = vf.createStatement(geomFeature, Geometrie.POINTS, points);
		aboutGeometry.add(geoStatement);
		this.serializePointsList(gp, points, crs, indexPointList);
	}

	protected void serializePointsList(GeometryProperty gp, Resource geomFeature, String crs, int indexPointList){
		int n = gp.getNumPoint(indexPointList);
		int indexPoint = getCurrentIndexPoint(gp, indexPointList);
		boolean ring = gp.getIsRing(indexPointList);
		Resource firstPoint = null;
		// In the case of LinearRing the first point and the last point is the same
		if (ring) n = n - 1;
		for (int i = 0; i < n; i++) {
			geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.POINTSLIST);
			aboutGeometry.add(geoStatement);
			this.setCrs(geomFeature,crs);
			BNode first = vf.createBNode();
			if (!ring) {
				geoStatement = vf.createStatement(geomFeature, RDF.FIRST, first);
			}
			else {
				geoStatement = vf.createStatement(geomFeature, Geometrie.FIRSTANDLAST, first);
				firstPoint = geomFeature;
				ring = false;
			}
			aboutGeometry.add(geoStatement);
			this.setCrs(geomFeature,crs);
			this.serializePoint(gp, first, crs, i+indexPoint);
			if (i == n - 1) {
				if (gp.getIsRing(indexPointList)){
					geoStatement = vf.createStatement(geomFeature, RDF.REST, firstPoint);
				}
				else {
					geoStatement = vf.createStatement(geomFeature, RDF.REST, RDF.NIL);
				}
				aboutGeometry.add(geoStatement);
			}
			else {
				BNode rest = vf.createBNode();
				geoStatement = vf.createStatement(geomFeature, RDF.REST, rest);
				aboutGeometry.add(geoStatement);
				geomFeature = rest;
			}
		}
	}

	protected void serializePoint(GeometryProperty gp, Resource geomFeature, String crs, int indexPoint){
		Double[] point = gp.getPoint(indexPoint);
		geoStatement = vf.createStatement(geomFeature, RDF.TYPE, Geometrie.POINT);
		aboutGeometry.add(geoStatement);
		this.setCrs(geomFeature,crs);
		geoStatement = vf.createStatement(geomFeature, Geometrie.COORDX, vf.createLiteral(point[0]));
		aboutGeometry.add(geoStatement);
		geoStatement = vf.createStatement(geomFeature, Geometrie.COORDY, vf.createLiteral(point[1]));
		aboutGeometry.add(geoStatement);

	}

	protected int getCurrentIndexPoint(GeometryProperty gp, int currentIndexPoint){
		int indexPoint = 0;
		for (int i=0; i < currentIndexPoint; i++){
			indexPoint = indexPoint + gp.getNumPoint(i);
		}
		return indexPoint;
	}

	protected void setCrs(Resource geomFeature, String crs){
		if (crs != null){
			geoStatement = vf.createStatement(geomFeature, Geometrie.SYSTCOORD, CRS.IGNFCRS);
			aboutGeometry.add(geoStatement);
		}
	}

	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws RepositoryException 
	 * @throws ParseException 
	 * @throws RDFHandlerException 
	 * @throws RDFParseException 
	 */
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, 
	RepositoryException, RDFParseException, RDFHandlerException, ParseException {
		// TODO Auto-generated method stub

		RDFBuilder builder = new RDFBuilder();
		URI context = builder.getVf().createURI("http://data.ign.fr/id/geofla");
		
		// Convert Region
		String shpPathRegion = ".//input/DonneesGeoflaWGS84/region.shp";
		Features_Parser parserRegion = new Features_Parser();
		//parserRegion.parseSHP(shpPathRegion, true, "EPSG:4326");
		parserRegion.parseSHP(shpPathRegion, false, "");
		String nameRegion = "region";
		//String crsRegion = parserRegion.crs;
		String crsRegion = "EPSG:4326";
		builder.translateToRDF(parserRegion.readFeatureCollection(), crsRegion, parserRegion.asGmlList, 
				context, nameRegion, "http://data.ig.fr/ontology/geofla/", true, ".//output/regions.ttl");
		
		// Convert Departement
		String shpPathDepartement = ".//input/DonneesGeoflaWGS84/departement.shp";
		Features_Parser parserDepartement = new Features_Parser();
		//parserDepartement.parseSHP(shpPathDepartement, true, "EPSG:4326");
		parserDepartement.parseSHP(shpPathDepartement, false, "");
		String nameDepartement = "departement";
		//String crsDepartement = parserDepartement.crs;
		String crsDepartement = "EPSG:4326";
		builder.translateToRDF(parserDepartement.readFeatureCollection(), crsDepartement, parserDepartement.asGmlList, 
				context, nameDepartement, "http://data.ig.fr/ontology/geofla/", true, ".//output/departements.ttl");
		
		// Convert Commune
		String shpPathCommune = ".//input/DonneesGeoflaWGS84/commune.shp";
		Features_Parser parserCommune = new Features_Parser();
		//parserCommune.parseSHP(shpPathCommune, true, "EPSG:4326");
		parserCommune.parseSHP(shpPathCommune, false, "");
		String nameCommune = "commune";
		//String crsCommune = parserCommune.crs;
		String crsCommune = "EPSG:4326";
		builder.translateToRDF(parserCommune.readFeatureCollection(), crsCommune, parserCommune.asGmlList, 
				context, nameCommune, "http://data.ig.fr/ontology/geofla/", true, ".//output/communes.ttl");


		// Convert Canton
		String shpPathCanton = ".//input/DonneesGeoflaWGS84/canton.shp";
		Features_Parser parserCanton = new Features_Parser();
		//parserCanton.parseSHP(shpPathCanton, true, "EPSG:4326");
		parserCanton.parseSHP(shpPathCanton, false, "");
		String nameCanton = "canton";
		//String crsCanton = parserCanton.crs;
		String crsCanton = "EPSG:4326";
		builder.translateToRDF(parserCanton.readFeatureCollection(), crsCanton, parserCanton.asGmlList, 
				context, nameCanton, "http://data.ig.fr/ontology/geofla/", true, ".//output/cantons.ttl");
		
		// Convert Arrondissement
		String shpPathArrondissement = ".//input/DonneesGeoflaWGS84/arrondissement.shp";
		Features_Parser parserArrondissement = new Features_Parser();
		//parserArrondissement.parseSHP(shpPathArrondissement, true, "EPSG:4326");
		parserArrondissement.parseSHP(shpPathArrondissement, false, "");
		String name = "arrondissement";
		//String crsArrondissement = parserArrondissement.crs;
		String crsArrondissement = "EPSG:4326";
		builder.translateToRDF(parserArrondissement.readFeatureCollection(), crsArrondissement, parserArrondissement.asGmlList, 
				context, name, "http://data.ig.fr/ontology/geofla/", true, ".//output/arrondissements.ttl");
	}

}