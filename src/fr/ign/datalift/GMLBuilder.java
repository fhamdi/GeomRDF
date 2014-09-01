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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.gml.producer.FeatureTransformer;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.w3c.dom.Document;

import fr.ign.datalift.parser.ShpParser;

public class GMLBuilder {

	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	static Logger log = Logger.getLogger(GMLBuilder.class.getName());

	public void creatGMLFile(String outputXSDFilePath, String outputGMLFilePath, SimpleFeatureSource featureSource) throws IOException {

		SimpleFeatureType schema = featureSource.getSchema();
		SimpleFeatureCollection collection = featureSource.getFeatures();

		File schemaFile = new File(outputXSDFilePath);
		schemaFile = schemaFile.getCanonicalFile();
		schemaFile.createNewFile();

		//URL schemaURL = schemaFile.toURI().toURL();
		//URL baseURL = schemaFile.getParentFile().toURI().toURL();
		URL baseURL = new URL("http://schemas.opengis.net");

		// define NS
		String prefix = (String) schema.getName().getLocalPart();
		String schemaNS = schema.getName().getNamespaceURI();
		String defaultNS = "http://www.opengis.net/test";
		if (schemaNS == null) schemaNS = defaultNS;
		boolean gmlNS = false;
		if (schemaNS.equals("http://www.opengis.net/gml")) gmlNS = true;
		

		FileOutputStream xsd = new FileOutputStream(schemaFile);

		GML encode = new GML(Version.GML2);
		encode.setBaseURL(baseURL);
		// XSD : Force the schema NameSpace value to defaultSpace if NameSpace is the same as the GML one ("http://www.opengis.net/gml")
		if (!gmlNS)
			encode.setNamespace(prefix, schemaNS);
		else encode.setNamespace(prefix, defaultNS);
		encode.encode(xsd, schema);

		xsd.close();

		FileOutputStream gml = new FileOutputStream(outputGMLFilePath);

		FeatureTransformer transform = new FeatureTransformer();
		transform.setEncoding(UTF8_CHARSET);
		transform.setIndentation(4);
		transform.setGmlPrefixing(false);

		// define feature information
		transform.getFeatureTypeNamespaces().declareDefaultNamespace(prefix, schemaNS);
		transform.addSchemaLocation(schemaNS, schemaFile.getName());

		String srsName = CRS.toSRS(schema.getCoordinateReferenceSystem());
		if (srsName != null) {
			transform.setSrsName(srsName);
		}

		// define feature collection
		//transform.setCollectionPrefix(prefix);
		//transform.setCollectionNamespace(schemaNS);

		// other configuration
		transform.setCollectionBounding(true); // include bbox info

		try {
			transform.transform(collection, gml);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
		}

		// GML : Force the schema NameSpace value to defaultSpace if NameSpace is the same as the GML one ("http://www.opengis.net/gml")
		if (gmlNS) {
			try {
				setDefaultNS(prefix, defaultNS, schemaFile.getName(), outputGMLFilePath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(e.getLocalizedMessage());
			}
		}
		gml.close();

	}

	public void setDefaultNS(String prefix, String defaultNS, String xsdFile, String outputGMLFilePath) throws Exception {

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dGml = db.parse(new FileInputStream(outputGMLFilePath));

		dGml.getDocumentElement().setAttribute("xmlns:" + prefix, defaultNS);
		dGml.getDocumentElement().setAttribute("xsi:schemaLocation", defaultNS + " " + xsdFile);

		File gml = new File(outputGMLFilePath);
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dGml), new StreamResult(gml));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		GMLBuilder gml = new GMLBuilder();
		/*ShpParser shpfeatures = new ShpParser(".//input/adresse.SHP", true);

		try {
			gml.creatGMLFile(".//output/adresse_wgs84.xsd", ".//output/adresse_wgs84.gml", shpfeatures.featureSource);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
		}*/

		ShpParser shpfeatures84 = new ShpParser(".//input/DEPARTEMENT.SHP", true);

		try {
			gml.creatGMLFile(".//output/departement_wgs84.xsd", ".//output/departement_wgs84.gml", shpfeatures84.featureSource);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
		}

	}

}
