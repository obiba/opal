/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.tx;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalThread extends Thread {

  private static final Logger log = LoggerFactory.getLogger(TransactionalThread.class);

  private final TransactionTemplate transactionTemplate;

  private final Runnable runnable;

  public TransactionalThread(TransactionTemplate transactionTemplate, Runnable runnable) {
    super("transactional-thread-" + runnable.getClass().getName());
    this.transactionTemplate = transactionTemplate;
    this.runnable = withCurrentSubject(runnable);
    setPriority(MIN_PRIORITY);
  }

  /**
   * Shiro 3 binds the subject to a plain thread local, so a thread does not inherit it from the one that creates it.
   * Run the runnable as the creator's subject, if any, so that secured lookups (e.g. a script referring to another
   * project's table) are authorized as that user.
   */
  public static Runnable withCurrentSubject(Runnable runnable) {
    Subject subject = ThreadContext.getSubject();
    return subject == null ? runnable : subject.associateWith(runnable);
  }

  @Override
  public void run() {
    try {
      transactionTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          runnable.run();
        }
      });
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error("Error in transaction execution", e);
      } else {
        log.error("Error in transaction execution: {}", e.getMessage());
      }
    }
  }
}