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

package fr.ign.datalift.model;

import java.util.ArrayList;
import java.util.List;

import org.geotools.gml3.ApplicationSchemaXSD;



public class AbstractFeature {

	List<FeatureProperty> properties;
	String name;
	String label;
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	ApplicationSchemaXSD schema;

	public AbstractFeature(ApplicationSchemaXSD schema) {
		this.setSchema(schema);
	}

	public AbstractFeature() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addProperty(FeatureProperty prop) {
		getProperties().add(prop);
	}

	public List<FeatureProperty> getProperties() {
		if (properties == null) {
			properties = new ArrayList<FeatureProperty>();
		}
		return properties;
	}

	public ApplicationSchemaXSD getSchema() {
		return schema;
	}

	public void setSchema(ApplicationSchemaXSD arg0) {
		this.schema = arg0;

	}

}
