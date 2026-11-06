/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.cfg;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueView;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.views.View;
import org.obiba.opal.core.cfg.OpalViewManager.DependentView;

import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpalViewManagerTest {

  private OpalViewPersistenceStrategy persistence;

  private OpalViewManager manager;

  @Before
  public void setUp() {
    persistence = mock(OpalViewPersistenceStrategy.class);
    manager = new OpalViewManager(persistence);
  }

  @After
  public void shutdownEngine() {
    // a view instantiates the engine, which is a singleton in the JVM of the tests
    MagmaEngine.get().shutdown();
  }

  private Datasource decorate(String name, ValueView... views) {
    Datasource datasource = mock(Datasource.class);
    when(datasource.getName()).thenReturn(name);
    when(persistence.readViews(name)).thenReturn(Set.of(views));
    return manager.decorate(datasource);
  }

  private static ValueTable reference(String reference) {
    ValueTableReference table = mock(ValueTableReference.class);
    when(table.getReference()).thenReturn(reference);
    when(table.getEntityType()).thenReturn("Participant");
    when(table.isForEntityType("Participant")).thenReturn(true);
    return table;
  }

  @Test
  public void finds_views_of_any_datasource_sorted() {
    decorate("a", new View("v", reference("ds.t")), new View("other", reference("ds.u")));
    decorate("b", new View("v", reference("ds.t")));

    assertThat(manager.getDependentViews("ds", "t"))
        .containsExactly(new DependentView("a", "v"), new DependentView("b", "v"));
  }

  @Test
  public void finds_view_joining_tables() {
    decorate("a", new View("join", reference("ds.t"), reference("ds.u")));

    assertThat(manager.getDependentViews("ds", "u")).containsExactly(new DependentView("a", "join"));
  }

  @Test
  public void finds_broken_view_without_resolving_its_reference() {
    // a real reference to a table and a datasource that do not exist, and a view that is not initialised
    decorate("a", new View("broken", new ValueTableReference("gone.t")));

    assertThat(manager.getDependentViews("gone", "t")).containsExactly(new DependentView("a", "broken"));
  }

  @Test
  public void same_table_name_in_another_datasource_is_not_a_dependency() {
    decorate("a", new View("v", reference("ds.t")));

    assertThat(manager.getDependentViews("other", "t")).isEmpty();
  }

  @Test
  public void ignores_views_that_do_not_refer_to_tables() {
    ValueView resourceView = mock(ValueView.class);
    when(resourceView.getName()).thenReturn("resource");
    decorate("a", resourceView);

    assertThat(manager.getDependentViews("ds", "t")).isEmpty();
  }

  @Test
  public void view_is_not_its_own_dependent() {
    decorate("a", new View("self", reference("a.self")));

    assertThat(manager.getDependentViews("a", "self")).isEmpty();
  }

  @Test
  public void view_of_the_same_name_in_another_datasource_is_a_dependent() {
    decorate("b", new View("t", reference("a.t")));

    assertThat(manager.getDependentViews("a", "t")).containsExactly(new DependentView("b", "t"));
  }

  @Test
  public void malformed_view_does_not_fail_the_lookup() {
    ValueTable noDatasource = mock(ValueTable.class);
    when(noDatasource.getEntityType()).thenReturn("Participant");
    decorate("a", new View("malformed", noDatasource), new View("v", reference("ds.t")));

    assertThat(manager.getDependentViews("ds", "t")).containsExactly(new DependentView("a", "v"));
  }

  @Test
  public void reads_the_views_under_the_lock_of_those_changing_them() throws Exception {
    Datasource datasource = decorate("a", new View("v", reference("ds.t")));
    Thread lookup = new Thread(() -> manager.getDependentViews("ds", "t"));

    synchronized (datasource) {
      lookup.start();
      lookup.join(200);
      assertThat(lookup.isAlive()).isTrue();
    }
    lookup.join();
  }

  @Test
  public void forgets_released_and_unregistered_datasources() {
    Datasource released = decorate("a", new View("v", reference("ds.t")));
    decorate("b", new View("v", reference("ds.t")));

    manager.release(released);
    assertThat(manager.getDependentViews("ds", "t")).containsExactly(new DependentView("b", "v"));

    manager.unregisterDatasource("b");
    assertThat(manager.getDependentViews("ds", "t")).isEmpty();
  }

  @Test
  public void replaces_datasource_decorated_again() {
    decorate("a", new View("v", reference("ds.t")));
    decorate("a", new View("w", reference("ds.t")));

    assertThat(manager.getDependentViews("ds", "t")).isEqualTo(List.of(new DependentView("a", "w")));
  }
}
