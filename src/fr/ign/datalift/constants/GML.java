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

public class GML {
	
	public static String NS = "http://purl.org/ifgi/gml/0.2#";
	
	public static URI ID;

	public static URI ABSTRACTGEOMETRY;
	
	public static URI HASPROPERTY;
	
	public static URI HASGEOMETRY;
	
	public static URI FEATURECOLLECTION;
	
	public static URI FEATUREMEMBER;
	
	public static URI HASVALUE;
	
	static {
		ValueFactory vf = ValueFactoryImpl.getInstance(); 
		ABSTRACTGEOMETRY = vf.createURI(NS, "AbstractGeometry"); 
		ID = vf.createURI(NS, "id"); 
		HASGEOMETRY = vf.createURI(NS, "hasGeometry"); 
		HASPROPERTY = vf.createURI(NS, "hasProperty");
		FEATURECOLLECTION = vf.createURI(NS, "FeatureCollection");
		FEATUREMEMBER = vf.createURI(NS, "featureMember");
		HASVALUE = vf.createURI(NS, "hasValue");
	}
}

