/*
 * Copyright / Copr. IGN 2014
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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;


public class DBMSParser {

	public SimpleFeatureSource featureSource;
	private Map<String,Object> params = new HashMap<String,Object>();

	static Logger log = Logger.getLogger(ShpParser.class.getName());

	public DBMSParser(String dbtype, String host, String port, String schema, 
			String database, String user, String password){
		this.params.put( "dbtype", dbtype);
		this.params.put( "host", host);
		this.params.put( "port", port);
		this.params.put( "schema", schema);
		this.params.put( "database", database);
		this.params.put( "user", user);
		this.params.put( "passwd", password);
		this.featureSource = readShpAndDbfFile();

	}

	public SimpleFeatureSource readShpAndDbfFile() {
		SimpleFeatureSource featureSource = null;

		try {

			DataStore dataStore = DataStoreFinder.getDataStore(this.params);
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];

			featureSource = dataStore.getFeatureSource(typeName);

		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		} 

		return featureSource;
	}

}

