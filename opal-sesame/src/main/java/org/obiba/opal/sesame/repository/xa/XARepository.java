/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.repository.xa;

import javax.transaction.TransactionManager;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 
 */
public class XARepository extends RepositoryWrapper {

  private static final Logger log = LoggerFactory.getLogger(XARepository.class);

  private TransactionManager manager;

  public XARepository(Repository repo) {
    super(repo);
  }

  public XARepository() {

  }

  public void setTransactionManager(TransactionManager manager) {
    this.manager = manager;
  }

  @Override
  public RepositoryConnection getConnection() throws RepositoryException {
    XARepositoryConnection xac = (XARepositoryConnection) TransactionSynchronizationManager.getResource(this);
    if(xac == null) {
      RepositoryConnection connection = super.getConnection();
      xac = new XARepositoryConnection(manager, this, connection);
      TransactionSynchronizationManager.bindResource(this, xac);
    }
    return xac;
  }

}
