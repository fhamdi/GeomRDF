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

public class GeometryProperty extends FeatureProperty {

	int numGeometries = 0;
	int numInteriorRing = 0;
	ArrayList<Double[]> points = new ArrayList<Double[]>();
	ArrayList<Integer> numPoints = new ArrayList<Integer>();
	ArrayList<Boolean> isRing = new ArrayList<Boolean>();

	public int getNumGeometries() {
		return numGeometries;
	}

	public void setNumGeometries(int numPolygon) {
		this.numGeometries = numPolygon;
	}

	public int getNumInteriorRing() {
		return numInteriorRing;
	}

	public void setNumInteriorRing(int numInteriorRing) {
		this.numInteriorRing = numInteriorRing;
	}

	public void setPointsLists(Double[] point){
		this.points.add(point);
	}

	public void setIsRing(boolean isRing){
		this.isRing.add(isRing);
	}

	public boolean getIsRing(int indexRing){
		return this.isRing.get(indexRing);
	}

	public int getNumPoint(int indexPointList) {
		return this.numPoints.get(indexPointList);
	}

	public void setNumPoint(int numPoint) {
		this.numPoints.add(numPoint);
	}

	public Double[] getPoint(int indexPoint) {
		return this.points.get(indexPoint);
	}


}
