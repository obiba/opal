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

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    RepositoryConnection c = super.getConnection();
    try {
      if(manager.getStatus() == Status.STATUS_ACTIVE) {
        c.setAutoCommit(false);
        manager.getTransaction().enlistResource(new XARepositoryConnection(c));
      }
    } catch(IllegalStateException e) {
      throw new RepositoryException(e);
    } catch(RollbackException e) {
      throw new RepositoryException(e);
    } catch(SystemException e) {
      throw new RepositoryException(e);
    }
    return c;
  }

  private class XARepositoryConnection implements XAResource {

    private RepositoryConnection resource;

    /**
     * @param repository
     * @param delegate
     */
    public XARepositoryConnection(RepositoryConnection resource) {
      this.resource = resource;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
      log.warn("commit {}", xid, onePhase);
      try {
        resource.commit();
      } catch(RepositoryException e) {
        throw new XAException(XAException.XAER_RMERR);
      }
    }

    public void end(Xid xid, int flags) throws XAException {
      log.warn("end {}", xid, flags);
    }

    public void forget(Xid xid) throws XAException {
      log.warn("forget {}", xid);
    }

    public int getTransactionTimeout() throws XAException {
      return 0;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
      log.warn("isSameRM");
      if(xares instanceof XARepositoryConnection) {
        XARepositoryConnection xarpo = (XARepositoryConnection) xares;
        return xarpo.resource.equals(resource);
      }
      return false;
    }

    public int prepare(Xid xid) throws XAException {
      log.warn("prepare {}", xid);
      return XA_OK;
    }

    public Xid[] recover(int flag) throws XAException {
      log.warn("recover {}", flag);
      return null;
    }

    public void rollback(Xid xid) throws XAException {
      log.warn("ROLLBACK {}", xid);
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
      log.warn("setTransactionTimeout {}", seconds);
      return false;
    }

    public void start(Xid xid, int flags) throws XAException {
      log.warn("START {}, {}", xid, flags);
      try {
        this.resource.setAutoCommit(false);
      } catch(RepositoryException e) {
        throw new XAException(XAException.XAER_RMERR);
      }
    }
  }
}
