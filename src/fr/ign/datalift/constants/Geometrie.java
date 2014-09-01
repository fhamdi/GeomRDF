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

package fr.ign.datalift.constants;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class Geometrie {

	public static String NS = "http://data.ign.fr/def/geometrie#";

	// Classes
	public static URI MULTIPOLYGON;
	public static URI MULTILINESTRING;
	public static URI MULTIPOINT;
	public static URI POLYGON;
	public static URI LINESTRING;
	public static URI LINEARRING;
	public static URI LINE;
	public static URI POINT;

	// Properties
	public static URI GEOMETRIE;
	public static URI SYSTCOORD;
	public static URI POLYGONMEMBER;
	public static URI EXTERIOR;
	public static URI INTERIOR;
	public static URI LINESTRINGMEMBER;
	public static URI POINTMEMBER;
	public static URI POINTS;
	public static URI POINTSLIST;
	public static URI FIRSTANDLAST;
	public static URI COORDX;
	public static URI COORDY;

	static {

		ValueFactory vf = ValueFactoryImpl.getInstance();

		// Classes
		MULTIPOLYGON = vf.createURI(NS, "MultiPolygon");
		MULTILINESTRING = vf.createURI(NS, "MultiLineString");
		MULTIPOINT = vf.createURI(NS, "MultiPoint");
		POLYGON = vf.createURI(NS, "Polygon");
		LINESTRING = vf.createURI(NS, "LineString");
		LINEARRING = vf.createURI(NS, "LinearRing");
		LINE = vf.createURI(NS, "Line");
		POINT = vf.createURI(NS, "Point");

		// Properties
		GEOMETRIE = vf.createURI(NS, "geometry");
		SYSTCOORD = vf.createURI(NS, "crs");
		POLYGONMEMBER = vf.createURI(NS, "polygonMember");
		EXTERIOR = vf.createURI(NS, "exterior");
		INTERIOR = vf.createURI(NS, "interior");
		LINESTRINGMEMBER = vf.createURI(NS, "lineStringMember");
		POINTMEMBER = vf.createURI(NS, "pointMember");
		POINTS = vf.createURI(NS, "points");
		POINTSLIST = vf.createURI(NS, "PointsList");
		FIRSTANDLAST = vf.createURI(NS, "firstAndLast");
		COORDX = vf.createURI(NS, "coordX");
		COORDY = vf.createURI(NS, "coordY");
	}
}
