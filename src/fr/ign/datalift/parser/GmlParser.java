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

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.wfs.FeatureCollectionType;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.wfs.v1_0.WFSConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GmlParser {

	public String gmlPath;
	public String xsdPath;
	public String crs = "EPSG:4326";
	public SimpleFeatureSource featureSource;
	public SimpleFeatureType simpleFeatureType;
	public FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;
	public ArrayList<String> asGmlList = null;
	
	static Logger log = Logger.getLogger(GmlParser.class.getName());

	public GmlParser(String gmlPath){
		this.gmlPath = gmlPath;
		this.xsdPath = this.gmlPath.substring(0,this.gmlPath.lastIndexOf('.') + 1) + "xsd";

		try {
			this.setCRS();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			log.error(e1.getLocalizedMessage());
		}

		File xsd = new File(this.xsdPath);
		if (xsd.exists()){
			try {
				parsexsd();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(e.getLocalizedMessage());
			}
		}
		else {
			try {
				parse();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error(e.getLocalizedMessage());
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				log.error(e.getLocalizedMessage());
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				log.error(e.getLocalizedMessage());
			}
		}

		try {
			this.asGmlList = asGML(this.featureCollection.getSchema().getName().getLocalPart());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
		}

	}

	/**
	 * Parses GML2 without specifying a schema location.
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */

	public void parse() throws IOException, SAXException, ParserConfigurationException {

		File gmlFile = new File(this.gmlPath);

		// extract Features
		URL gmlURL = gmlFile.toURI().toURL();
		InputStream in = gmlURL.openStream();
		GML gml = new GML(Version.WFS1_0);
		this.featureCollection = gml.decodeFeatureCollection(in);
	}

	/**
	 * Parses GML2 by specifying the schema location.
	 * @throws Exception 
	 */

	@SuppressWarnings("unchecked")
	public void parsexsd() throws Exception {

		File gml = setSchemaLocation();
		InputStream gmlStream = new FileInputStream(gml);

		//GMLConfiguration gmlConf = new GMLConfiguration();
		WFSConfiguration gmlConf = new WFSConfiguration();
		Parser parser = new Parser(gmlConf);
		parser.setStrict(false);

		FeatureCollectionType fc = (FeatureCollectionType) parser.parse(gmlStream);
		EList<?> featureCollections = fc.getFeature();
		this.featureCollection = (FeatureCollection<SimpleFeatureType, SimpleFeature>) featureCollections.get(0);

		gmlStream.close();

	}

	public File setSchemaLocation() throws Exception {

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dGml = db.parse(new FileInputStream(this.gmlPath));

		File xsd = new File(xsdPath);
		Document dXsd = db.parse(new FileInputStream(xsdPath));

		dGml.getDocumentElement().setAttribute("xsi:schemaLocation", 
				dXsd.getDocumentElement().getAttribute("targetNamespace") + " " + xsd.getCanonicalPath());

		File gml = File.createTempFile("gmltemp", "gml");
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dGml), new StreamResult(gml));
		return gml;
	}

	public void setCRS() throws Exception {

		FileInputStream gml = new FileInputStream(this.gmlPath);

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dGml = db.parse(gml);

		this.crs = dGml.getElementsByTagName("gml:Box").item(0).getAttributes().getNamedItem("srsName").getNodeValue();
		gml.close();

	}

	public ArrayList<String> asGML(String typeName) throws Exception {

		ArrayList<String> GmlLiteralList = new ArrayList<String>();

		FileInputStream gml = new FileInputStream(this.gmlPath);

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dGml = db.parse(gml);

		NodeList nl1 = dGml.getElementsByTagName(typeName + ":the_geom");

		for(int i = 0; i < nl1.getLength(); i++) {
			//Set up the transformer to write the output string
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);

			NodeList nl2 = nl1.item(i).getChildNodes();
			DOMSource source = null;
			for(int j = 0; j < nl2.getLength(); j++) {
				Node e = nl2.item(j);
				if(e instanceof Element){
					source = new DOMSource(e);
				}

			}

			//Do the transformation and output
			transformer.transform(source, result);
			GmlLiteralList.add(sw.toString());
		}
		gml.close();
		return GmlLiteralList;
	}

}
