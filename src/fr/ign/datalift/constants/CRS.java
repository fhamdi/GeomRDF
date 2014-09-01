package fr.ign.datalift.constants;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class CRS {

	public static String NS = "http://data.ign.fr/id/ignf/crs/";

	public static URI IGNFCRS;

	private static String ignfCRS;

	public static void setCrsValue(String crs){
		if (crs.equals("EPSG:4326")) ignfCRS = "WGS84GDD";
		if (crs.equals("RGF93_Lambert_93")) ignfCRS = "RGF93LAMB93";
		ValueFactory vf = ValueFactoryImpl.getInstance();
		IGNFCRS = vf.createURI(NS, ignfCRS);
	}

}