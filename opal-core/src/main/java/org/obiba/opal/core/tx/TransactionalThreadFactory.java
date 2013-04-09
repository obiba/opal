/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.tx;

import java.util.concurrent.ThreadFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TransactionalThreadFactory implements ThreadFactory {

  private final TransactionTemplate txTemplate;

  @Autowired
  public TransactionalThreadFactory(PlatformTransactionManager txManager) {
    if(txManager == null) throw new IllegalArgumentException("txManager cannot be null");
    txTemplate = new TransactionTemplate(txManager);
  }

  @Override
  public Thread newThread(Runnable r) {
    return new TransactionalThread(r);
  }

  private class TransactionalThread extends Thread {

    private final Runnable runnable;

    private TransactionalThread(Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public void run() {
      txTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          runnable.run();
        }
      });
    }
  }
}
