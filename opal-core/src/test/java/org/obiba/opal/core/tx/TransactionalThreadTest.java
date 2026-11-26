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

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Test;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionalThreadTest {

  @After
  public void tearDown() {
    ThreadContext.remove();
  }

  @Test
  public void test_thread_runs_as_creator_subject() throws InterruptedException {
    Subject subject = new Subject.Builder(new DefaultSecurityManager())
        .principals(new SimplePrincipalCollection("user", "realm")).authenticated(true).buildSubject();
    ThreadContext.bind(subject);

    AtomicReference<Subject> seen = new AtomicReference<>();
    TransactionTemplate tx = new TransactionTemplate() {
      @Override
      public <T> T execute(org.springframework.transaction.support.TransactionCallback<T> action) {
        return action.doInTransaction(null);
      }
    };
    Thread thread = new TransactionalThread(tx, () -> seen.set(ThreadContext.getSubject()));
    thread.start();
    thread.join();

    assertNotNull(seen.get());
    assertEquals("user", seen.get().getPrincipal());
  }
}
