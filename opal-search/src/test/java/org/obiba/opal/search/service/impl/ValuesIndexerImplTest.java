/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.service.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Test;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.service.VariableSummaryHandler;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class ValuesIndexerImplTest {

  @After
  public void tearDown() {
    ThreadContext.remove();
  }

  @Test
  public void test_summaries_computed_as_indexing_subject() throws InterruptedException {
    Subject subject = new Subject.Builder(new DefaultSecurityManager())
        .principals(new SimplePrincipalCollection("indexer", "realm")).authenticated(true).buildSubject();
    ThreadContext.bind(subject);

    AtomicReference<Object> principal = new AtomicReference<>();
    VariableSummaryHandler summaryHandler = mock(VariableSummaryHandler.class);
    doAnswer(invocation -> {
      // fails when no subject is bound, as no security manager is set: the principal then stays null
      principal.set(SecurityUtils.getSubject().getPrincipal());
      return null;
    }).when(summaryHandler).computeSummaries(any(ValueTable.class));

    // summaries do not use the transactional thread factory of the indexing
    ValuesIndexManagerImpl indexManager = new ValuesIndexManagerImpl(summaryHandler, null);
    ValueTable table = mock(ValueTable.class);
    ValuesIndexerImpl indexer = new ValuesIndexerImpl(indexManager, table, new ValuesIndexImpl(indexManager, table));

    indexer.computeSummaries().join();

    assertThat(principal.get()).isEqualTo("indexer");
  }
}
