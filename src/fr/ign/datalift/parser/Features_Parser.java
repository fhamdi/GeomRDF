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

package fr.ign.datalift.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
//import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fr.ign.datalift.model.AbstractFeature;
import fr.ign.datalift.model.FeatureProperty;
import fr.ign.datalift.model.GeometryProperty;

public class Features_Parser {

	private ArrayList<fr.ign.datalift.model.AbstractFeature> features;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> fc;
	private fr.ign.datalift.model.AbstractFeature ft;
	public String crs = "EPSG:4326";
	public ArrayList<String> asGmlList = null;
	private MathTransform transform = null;

	static Logger log = Logger.getLogger(Features_Parser.class.getName());

	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	// Parsing shp file: to use when conversion from RGF93 to WGS84 fails
	public void parseSHP(String shpPath, boolean wgs84) throws IOException, SAXException, ParserConfigurationException {
		// extract Features
		ShpParser shpfeatures = new ShpParser(shpPath, wgs84);
		this.fc = shpfeatures.featureSource.getFeatures();
		this.crs = CRS.toSRS(shpfeatures.featureSource.getSchema().getCoordinateReferenceSystem());
	}

	// Parsing gml file
	public void parseGML(String gmlPath, boolean reproject, String targetCRS) throws IOException, SAXException, ParserConfigurationException {
		GmlParser gmlparser = new GmlParser(gmlPath);
		this.fc = gmlparser.featureCollection;
		this.asGmlList = gmlparser.asGmlList;
		this.crs = gmlparser.crs;
		if (reproject){
			try {
				Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
				CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
				CoordinateReferenceSystem sourceCRS = factory.createCoordinateReferenceSystem(gmlparser.crs);
				reprojectCRS(sourceCRS, targetCRS);
			} 
			catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Parsing DBMS
	public void parseDBMS(String dbtype, String host, String port, String schema, 
			String database, String user, String password, boolean reproject, String targetCRS) throws IOException, SAXException, ParserConfigurationException {
		DBMSParser dbmsparser = new DBMSParser(dbtype, host, port, schema, database, user, password);
		this.fc = dbmsparser.featureSource.getFeatures();
		this.crs = CRS.toSRS(dbmsparser.featureSource.getSchema().getCoordinateReferenceSystem());
		if (reproject){
			CoordinateReferenceSystem sourceCRS = dbmsparser.featureSource.getSchema().getCoordinateReferenceSystem();
			reprojectCRS(sourceCRS, targetCRS);
		}
	}

	// Parsing shp file
	public void parseSHP(String shpPath, boolean reproject, String targetCRS) throws IOException, SAXException, ParserConfigurationException {
		// extract Features
		ShpParser shpfeatures = new ShpParser(shpPath);
		this.fc = shpfeatures.featureSource.getFeatures();
		CoordinateReferenceSystem sourceCRS = shpfeatures.featureSource.getSchema().getCoordinateReferenceSystem();
		this.crs = CRS.toSRS(sourceCRS);
		if (reproject){
			reprojectCRS(sourceCRS, targetCRS);
		}
	}


	protected void reprojectCRS(CoordinateReferenceSystem sourceCRS, String targetCRS){
		try {
			Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
			CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
			CoordinateReferenceSystem tmpCRS = factory.createCoordinateReferenceSystem(targetCRS);
			boolean lenient = true; // allow for some error due to different datums
			this.transform = CRS.findMathTransform(sourceCRS, tmpCRS, lenient);
			this.crs = CRS.toSRS(tmpCRS);
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	public ArrayList<AbstractFeature> readFeatureCollection() {

		features = new ArrayList<AbstractFeature>();

		for (FeatureIterator<SimpleFeature> j = fc.features(); j.hasNext();) {
			// To handle geometries, SimpleFeature is used instead of Feature
			SimpleFeature f = j.next();
			ft = new AbstractFeature();
			if (f.getIdentifier() != null) {
				//ft.setName(new String (f.getIdentifier().toString()));
				ft.setName(new StringBuilder(new String(f.getIdentifier().toString().getBytes(),UTF8_CHARSET)).toString());
			}

			Collection<Property> propColl = f.getProperties();
			for (Iterator<Property> iterator = propColl.iterator(); iterator.hasNext();) {
				Property prop = iterator.next();
				if (!prop.getName().toString().equals("metaDataProperty")
						&& !prop.getName().toString().equals("description")
						&& !prop.getName().toString().equals("name")
						&& !prop.getName().toString().equals("boundedBy")
						&& !prop.getName().toString().equals("location")) {

					// checks if the property value is not null
					if (prop.getValue() != null) {

						// check if Property is GeometryProperty
						if (prop.getType() instanceof GeometryTypeImpl) {
							GeometryProperty gp = new GeometryProperty();

							Geometry geom = (Geometry) f.getDefaultGeometry();

							if (transform != null) {
								try {
									geom = JTS.transform((Geometry) f.getDefaultGeometry(), transform);
								} catch (MismatchedDimensionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (TransformException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							// Parse GeometryCollection (MultiLineString, MultiPoint, MultiPolygon)							
							// Parse MultiPolygon
							if (geom instanceof MultiPolygon) {
								MultiPolygon mp = (MultiPolygon) geom;
								gp.setType(new StringBuilder(new String(mp.getGeometryType().getBytes(),UTF8_CHARSET)).toString());
								parseMultiPolygon(gp,mp);
							}
							// Parse MultiLineString
							if (geom instanceof MultiLineString) {
								MultiLineString mls = (MultiLineString) geom;
								gp.setType(new StringBuilder(new String(mls.getGeometryType().getBytes(),UTF8_CHARSET)).toString());
								parseMultiLineString(gp,mls);
							}

							// Parse MultiPoint
							if (geom instanceof MultiPoint) {
								MultiPoint mpt = (MultiPoint) geom;
								gp.setType(new StringBuilder(new String(mpt.getGeometryType().getBytes(),UTF8_CHARSET)).toString());
								parseMultiPoint(gp,mpt);
							}

							// Parse Polygon
							if (geom instanceof Polygon) {
								Polygon polygon = (Polygon) geom;
								gp.setType(new StringBuilder(new String(polygon.getGeometryType().getBytes(),UTF8_CHARSET)).toString());
								parsePolygon(gp,polygon);
							}

							// Parse LineString
							if (geom instanceof LineString) {							
								LineString ls = (LineString) geom;
								gp.setType(new StringBuilder(new String(ls.getGeometryType().getBytes(),UTF8_CHARSET)).toString());
								parseLineString(gp,ls);
							}

							// Parse LinearRing
							if (geom instanceof LinearRing) {							
								LinearRing lr = (LinearRing) geom;
								gp.setType(new StringBuilder(new String(lr.getGeometryType().getBytes(),UTF8_CHARSET)).toString());
								parseLineString(gp,lr);
							}

							// Parse Point							
							if (geom instanceof Point) {
								Point pt = (Point) geom;
								gp.setType(new StringBuilder(new String(pt.getGeometryType().getBytes(),UTF8_CHARSET)).toString());
								parsePoint(gp,pt);
							}


							gp.setName(new StringBuilder(new String(prop.getName().toString().getBytes(),UTF8_CHARSET)).toString());
							gp.setValue(new StringBuilder(new String(geom.toString().getBytes(),UTF8_CHARSET)).toString());

							//gp.setValue(new StringBuilder(new String(prop.getValue().toString().getBytes(),UTF8_CHARSET)).toString());
							//gp.setType(new StringBuilder(new String(prop.getDescriptor().getType().getBinding().getSimpleName().getBytes(),UTF8_CHARSET)).toString());

							ft.addProperty(gp);

						} else {
							FeatureProperty fp = new FeatureProperty();
							fp.setName(new StringBuilder(new String(prop.getName().toString().getBytes(),UTF8_CHARSET)).toString());
							if (prop.getType().toString().contains("Float") || (prop.getType().toString().contains("Double"))) {
								fp.setDoubleValue(Double.parseDouble(prop.getValue().toString()));
								fp.setType("double");
								ft.addProperty(fp);
							}
							if (prop.getType().toString().contains("Integer") || (prop.getType().toString().contains("Long"))) {
								fp.setIntValue(Integer.parseInt(prop.getValue().toString()));
								fp.setType("int");
								ft.addProperty(fp);
							}
							else  {
								fp.setValue(new StringBuilder(new String(prop.getValue().toString().getBytes(),UTF8_CHARSET)).toString());
								ft.addProperty(fp);
							}

							if(prop.getName().toString().equals("name")){
								ft.setLabel(new StringBuilder(new String(prop.getValue().toString().getBytes(),UTF8_CHARSET)).toString());

							} if(prop.getName().toString().contains("label")){
								ft.setLabel(new StringBuilder(new String(prop.getValue().toString().getBytes(),UTF8_CHARSET)).toString());
							}

						}
					}
				} 

			}

			features.add(ft);
		}

		return features;
	}

	protected void parseMultiPolygon(GeometryProperty gp, MultiPolygon mp){
		int numGeometries = mp.getNumGeometries();
		gp.setNumGeometries(numGeometries);
		for (int i=0; i<numGeometries ; i++){
			parsePolygon(gp,(Polygon)mp.getGeometryN(i));
		}
	}

	protected void parseMultiLineString(GeometryProperty gp, MultiLineString mls){
		int numGeometries = mls.getNumGeometries();
		gp.setNumGeometries(numGeometries);
		for (int i=0; i<numGeometries ; i++){
			parseLineString(gp,(LineString)mls.getGeometryN(i));
		}
	}

	protected void parseMultiPoint(GeometryProperty gp, MultiPoint mpt){
		int numGeometries = mpt.getNumGeometries();
		gp.setNumGeometries(numGeometries);
		for (int i=0; i<numGeometries ; i++){
			parsePoint(gp,(Point)mpt.getGeometryN(i));
		}
	}

	protected void parsePolygon(GeometryProperty gp, Polygon polygon){
		parseLineString(gp,polygon.getExteriorRing());
		int numInteriorRing = polygon.getNumInteriorRing();
		gp.setNumInteriorRing(numInteriorRing);
		for (int i=0; i<numInteriorRing ; i++){
			parseLineString(gp,polygon.getInteriorRingN(i));
		}
	}

	protected void parseLineString(GeometryProperty gp, LineString ls){
		// in the case of LinearRing, setIsRing true
		gp.setIsRing(ls.isClosed());
		int numPoint = ls.getNumPoints();
		gp.setNumPoint(numPoint);
		for (int i=0; i<numPoint ; i++){
			Point pt = ls.getPointN(i);
			parsePoint(gp,pt);
		}
	}

	protected void parsePoint(GeometryProperty gp, Point p){
		Double[] pt = {p.getX(), p.getY()};
		gp.setPointsLists(pt);
	}

}
