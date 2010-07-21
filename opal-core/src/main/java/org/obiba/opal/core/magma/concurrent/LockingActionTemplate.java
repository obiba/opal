/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.magma.concurrent;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.obiba.magma.MagmaEngine;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public abstract class LockingActionTemplate {

  public void execute() throws InvocationTargetException {
    Set<String> lockNames = getLockNames();

    try {
      MagmaEngine.get().lock(lockNames);
      doInTransaction(getAction());
    } catch(ActionRuntimeException ex) {
      throw new InvocationTargetException(ex.getCause());
    } catch(Exception ex) {
      throw new InvocationTargetException(ex);
    } finally {
      MagmaEngine.get().unlock(lockNames);
    }
  }

  private void doInTransaction(final Action action) throws TransactionException {
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {

      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          action.execute();
        } catch(Exception ex) {
          // Wrap the exception in an unchecked exception, since doInTransactionWithoutResult
          // does not declare any checked exceptions.
          throw new ActionRuntimeException(ex);
        }
      }
    });
  }

  protected abstract Set<String> getLockNames();

  protected abstract TransactionTemplate getTransactionTemplate();

  protected abstract Action getAction();

  //
  // Inner Classes / Interfaces
  // 

  public interface Action {

    void execute() throws Exception;
  }

  public static class ActionRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ActionRuntimeException(Throwable cause) {
      super(cause);
    }
  }
}
