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

package fr.ign.datalift.repository;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.DirectTypeHierarchyInferencer;
import org.openrdf.sail.memory.MemoryStore;


public class SesameConnection {

	protected RepositoryConnection connection;
	protected ValueFactory vf;
	protected SesameRepository sesameRepository;
	
	public ValueFactory getVf() throws RepositoryException {
		if (vf == null) {
			vf = getConnection().getValueFactory(); 
		}
		return vf;
	}

	protected RepositoryConnection getConnection() throws RepositoryException {
		if (connection == null) {
			this.initRepository(); 
		}

		return connection;
	}
	
	public void connectSesameRepository(){
		try {
			sesameRepository = new SesameRepository();
		} catch (RepositoryException e) {
			System.out.println(e);
		}
	}


	private void initRepository() throws RepositoryException {
	
		Repository tempRepository = new SailRepository(new DirectTypeHierarchyInferencer(new MemoryStore()));
		tempRepository.initialize();
		this.vf = tempRepository.getValueFactory();
		this.setConnection(tempRepository.getConnection());
		getConnection().begin();

	}
	
	public void setConnection(RepositoryConnection repositoryConnection) {
		this.connection = repositoryConnection;
	}

}
