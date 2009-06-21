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
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.DelegatingRepositoryConnection;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.flushable.FlushableConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 *
 */
public class XARepositoryConnection extends RepositoryConnectionWrapper implements XAResource {

  private static final Logger log = LoggerFactory.getLogger(XARepositoryConnection.class);

  private final TransactionManager txm;

  private final XARepository xaRepository;

  private Transaction tx = null;

  private Xid xid;

  private int status;

  /**
   * @param repository
   */
  public XARepositoryConnection(TransactionManager txm, XARepository repository, RepositoryConnection delegate) throws RepositoryException {
    super(repository, delegate);
    this.txm = txm;
    this.xaRepository = repository;
    enlistIfNecessary();
  }

  @Override
  protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts) throws RepositoryException {
    enlistIfNecessary();
    super.addWithoutCommit(subject, predicate, object, contexts);
  }

  @Override
  protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts) throws RepositoryException {
    enlistIfNecessary();
    super.removeWithoutCommit(subject, predicate, object, contexts);
  }

  @Override
  public void close() throws RepositoryException {
    if(enlisted() == false) {
      TransactionSynchronizationManager.unbindResource(xaRepository);
      super.close();
    }
  }

  @Override
  public void commit() throws RepositoryException {
    if(enlisted()) {
      throw new RepositoryException("cannot commit(): connection is taking part in a global transaction.");
    } else {
      super.commit();
    }
  }

  @Override
  public void rollback() throws RepositoryException {
    if(enlisted()) {
      throw new RepositoryException("cannot rollback(): connection is taking part in a global transaction.");
    } else {
      super.rollback();
    }
  }

  protected boolean enlisted() {
    return tx != null;
  }

  protected void enlistIfNecessary() throws RepositoryException {
    if(tx == null) {

      try {
        Transaction currentTx = txm.getTransaction();
        if(currentTx != null && currentTx.getStatus() == Status.STATUS_ACTIVE) {
          log.debug("associating connection {} to transaction {}", this, currentTx);
          tx = currentTx;
        }
      } catch(SystemException e) {
        throw new RepositoryException(e);
      }

      if(tx != null) {
        try {
          tx.enlistResource(this);
        } catch(IllegalStateException e) {
          throw new RepositoryException(e);
        } catch(RollbackException e) {
          throw new RepositoryException(e);
        } catch(SystemException e) {
          throw new RepositoryException(e);
        }
      }
    }
  }

  public void commit(Xid xid, boolean onePhase) throws XAException {
    log.debug("commit({})", xid);
    try {
      getDelegate().commit();
      status = Status.STATUS_COMMITTED;
    } catch(RepositoryException e) {
      throw new XAException(XAException.XA_RBROLLBACK);
    } finally {
      try {
        super.close();
      } catch(RepositoryException e) {
      }
    }
  }

  public void end(Xid xid, int flags) throws XAException {
    log.debug("end({})", xid);
    checkXid(xid);

    // Remove the connection from transaction synchronization. This locks the repository until either commit or rollback
    // is called.
    TransactionSynchronizationManager.unbindResource(xaRepository);
  }

  public void forget(Xid xid) throws XAException {
    log.debug("forget({})", xid);
    try {
      setAutoCommit(true);
    } catch(RepositoryException e) {
      throw new XAException(XAException.XAER_RMERR);
    }
    this.tx = null;
    this.xid = null;
    this.status = Status.STATUS_NO_TRANSACTION;
  }

  public int getTransactionTimeout() throws XAException {
    return 3600 * 24;
  }

  public boolean isSameRM(XAResource xares) throws XAException {
    if(xares instanceof XARepositoryConnection) {
      XARepositoryConnection xarc = (XARepositoryConnection) xares;
      try {
        return this.getDelegate().equals(xarc.getDelegate());
      } catch(RepositoryException e) {
        throw new XAException(XAException.XAER_RMERR);
      }
    }
    return false;
  }

  public int prepare(Xid xid) throws XAException {
    log.debug("prepare({})", xid);
    try {
      FlushableConnection flushable = extractFlushableConnection(getDelegate());
      if(flushable != null) {
        flushable.flush();
      }
    } catch(RepositoryException e) {
      throw new XAException(XAException.XAER_RMERR);
    }
    status = Status.STATUS_PREPARED;
    return XA_OK;
  }

  public Xid[] recover(int flag) throws XAException {
    if(flag != TMSTARTRSCAN && flag != TMENDRSCAN && flag != TMNOFLAGS) {
      throw new XAException(XAException.XAER_INVAL);
    }
    if(status == Status.STATUS_PREPARED || status == Status.STATUS_PREPARING) {
      return new Xid[] { this.xid };
    }
    return null;
  }

  public void rollback(Xid xid) throws XAException {
    checkXid(xid);
    log.warn("rollback({})", xid);
    try {
      getDelegate().rollback();
    } catch(RepositoryException e) {
      throw new XAException(XAException.XA_RBROLLBACK);
    } finally {
      try {
        super.close();
      } catch(RepositoryException e) {
      }
    }
  }

  public boolean setTransactionTimeout(int seconds) throws XAException {
    return false;
  }

  public void start(Xid xid, int flags) throws XAException {
    if(xid == null) {
      throw new XAException(XAException.XAER_INVAL);
    }
    log.debug("start({})", xid);
    try {
      if(isAutoCommit()) {
        setAutoCommit(false);
      }
    } catch(RepositoryException e) {
      throw new XAException(e.getMessage());
    }
    this.xid = xid;
    this.status = Status.STATUS_ACTIVE;
  }

  private void checkXid(Xid xid) throws XAException {
    if(xid == null || xid.equals(this.xid) == false) {
      throw new XAException(XAException.XAER_INVAL);
    }
  }

  private FlushableConnection extractFlushableConnection(RepositoryConnection connection) throws RepositoryException {
    if(connection instanceof FlushableConnection) {
      return (FlushableConnection) connection;
    }
    if(connection instanceof DelegatingRepositoryConnection) {
      return extractFlushableConnection(((DelegatingRepositoryConnection) connection).getDelegate());
    }
    return null;
  }
}
