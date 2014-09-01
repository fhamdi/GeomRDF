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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

import fr.ign.datalift.parser.ShpParser;

public class CSVBuilder {

	public void createCSVFile(String outpuCSVFilePath, SimpleFeatureSource featureSource) throws IOException {

		SimpleFeatureCollection collection = featureSource.getFeatures();
		SimpleFeatureIterator iterator = collection.features();

		FileWriter writer = new FileWriter(outpuCSVFilePath);

		for (int i= 0; i < featureSource.getSchema().getAttributeCount(); i++) {
			if (featureSource.getSchema().getGeometryDescriptor().getType().getName() != featureSource.getSchema().getType(i).getName()){
				writer.append(featureSource.getSchema().getType(i).getName().getLocalPart());
				writer.append(';');
			}
		}

		writer.append(featureSource.getSchema().getGeometryDescriptor().getType().getName().getLocalPart());
		writer.append('\n');

		while (iterator.hasNext()) {

			SimpleFeature feature = (SimpleFeature) iterator.next();

			Geometry geometry = (Geometry) feature.getDefaultGeometry();
			ShapeType type = JTSUtilities.findBestGeometryType(geometry);
			if(!type.isPolygonType() && !type.isLineType() && 
					!type.isPointType()) {
				//log.warnLog("warning message here");
				//warningCount++;
				break;
			}

			if(geometry.isEmpty()) {
				// TODO empty geometry
				continue;
			}

			List<Object> featureAttributes = feature.getAttributes();

			Iterator<Object> itr = featureAttributes.iterator(); 
			while(itr.hasNext()) {
				Object element = itr.next(); 
				if (element != feature.getDefaultGeometry()) {
					writer.append(element.toString());
					writer.append(';');
				}
			}

			writer.append(feature.getDefaultGeometry().toString());
			writer.append('\n');

		}
		writer.flush();
		writer.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		CSVBuilder cvs = new CSVBuilder();
		ShpParser shpfeatures = new ShpParser(".//input/DEPARTEMENT.SHP", false);
		
		try {
			cvs.createCSVFile(".//output/departement.csv", shpfeatures.featureSource);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ShpParser shpfeatures84 = new ShpParser(".//input/DEPARTEMENT.SHP", true);
		try {
			cvs.createCSVFile(".//output/departement84.csv",  shpfeatures84.featureSource);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
