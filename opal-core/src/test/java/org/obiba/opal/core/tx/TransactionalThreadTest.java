/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.tx;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Test;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertEquals;

public class TransactionalThreadTest {

  private static final TransactionTemplate TX = new TransactionTemplate() {
    @Override
    public <T> T execute(TransactionCallback<T> action) {
      return action.doInTransaction(null);
    }
  };

  @After
  public void tearDown() {
    ThreadContext.remove();
  }

  /**
   * Subject bound to the creating thread directly, in its thread local.
   */
  @Test
  public void test_thread_runs_as_bound_subject() throws InterruptedException {
    ThreadContext.bind(newSubject());
    AtomicReference<Object> principal = new AtomicReference<>();
    Thread thread = newThread(principal);
    thread.start();
    thread.join();
    assertEquals("user", principal.get());
  }

  /**
   * Subject bound by Shiro around a runnable, as for a command job: a scoped value from Java 25, a thread local before.
   */
  @Test
  public void test_thread_runs_as_associated_subject() throws InterruptedException {
    AtomicReference<Object> principal = new AtomicReference<>();
    AtomicReference<Thread> thread = new AtomicReference<>();
    newSubject().execute(() -> thread.set(newThread(principal)));
    thread.get().start();
    thread.get().join();
    assertEquals("user", principal.get());
  }

  private static Subject newSubject() {
    return new Subject.Builder(new DefaultSecurityManager())
        .principals(new SimplePrincipalCollection("user", "realm")).authenticated(true).buildSubject();
  }

  private static Thread newThread(AtomicReference<Object> principal) {
    // reading the subject without one bound fails, as no security manager is set: the principal then stays null
    return new TransactionalThread(TX, () -> principal.set(SecurityUtils.getSubject().getPrincipal()));
  }
}
