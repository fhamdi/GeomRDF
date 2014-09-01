package fr.ign.datalift.reprojection;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

public class Wgs84Reprojection {

	public SimpleFeatureSource featureSource;

	static Logger log = Logger.getLogger(Wgs84Reprojection.class.getName());

	public Wgs84Reprojection(SimpleFeatureSource featureSource, String shpFilepath) {

		try {

			CoordinateReferenceSystem dataCRS = featureSource.getSchema().getCoordinateReferenceSystem();

			// If used, it invert lat and log due the order of the "AXIS" parameter
			//CoordinateReferenceSystem worldCRS2 = CRS.decode("EPSG:4326");

			String wkt = "GEOGCS[" + "\"WGS 84\"," + "  DATUM[" + "    \"WGS_1984\","
					+ "    SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],"
					+ "    TOWGS84[0,0,0,0,0,0,0]," + "    AUTHORITY[\"EPSG\",\"6326\"]],"
					+ "  PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],"
					+ "  UNIT[\"DMSH\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9108\"]],"
					+ "  AXIS[\"Long\",EAST]," + "  AXIS[\"Lat\",NORTH],"
					+ "  AUTHORITY[\"EPSG\",\"4326\"]]";
			CoordinateReferenceSystem worldCRS = CRS.parseWKT(wkt);

			boolean lenient = true; // allow for some error due to different datums
			MathTransform transform = CRS.findMathTransform(dataCRS, worldCRS, lenient);

			DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
			Map<String, Serializable> create = new HashMap<String, Serializable>();
			File shpFile = new File(shpFilepath.substring(0, shpFilepath.lastIndexOf(".")) + "_wgs84.shp");
			if (shpFile.exists()) {
				shpFile.delete();
			}
			create.put("url", shpFile.toURI().toURL());
			create.put("create spatial index", Boolean.TRUE);
			DataStore dataStore = factory.createNewDataStore(create);
			SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(featureSource.getSchema(), worldCRS);
			dataStore.createSchema(featureType);

			Transaction transaction = new DefaultTransaction("Reproject");
			FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
					dataStore.getFeatureWriterAppend(featureType.getTypeName(), transaction);

			SimpleFeatureCollection featureCollection = featureSource.getFeatures();
			SimpleFeatureIterator iterator = featureCollection.features();

			try {
				while (iterator.hasNext()) {
					// copy the contents of each feature and transform the geometry
					SimpleFeature feature = iterator.next();
					SimpleFeature copy = writer.next();
					copy.setAttributes(feature.getAttributes());

					Geometry geometry = (Geometry) feature.getDefaultGeometry();
					Geometry geometry2 = JTS.transform(geometry, transform);

					copy.setDefaultGeometry(geometry2);
					writer.write();
				}
				transaction.commit();

			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				iterator.close();
				transaction.close();
			}

			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];

			this.featureSource = dataStore.getFeatureSource(typeName);

		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
		}

	}

}
