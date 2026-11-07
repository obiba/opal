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

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueView;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.views.DefaultViewManagerImpl;
import org.obiba.magma.views.JoinTable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewAwareDatasource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
public class OpalViewManager extends DefaultViewManagerImpl {

  /**
   * A view that refers to a table.
   */
  public record DependentView(String datasource, String name) {
    public String reference() {
      return datasource + "." + name;
    }
  }

  // the datasources of MagmaEngine are filtered by the permissions of the caller, this is not
  private final Map<String, ViewAwareDatasource> datasources = new ConcurrentHashMap<>();

  @Autowired
  public OpalViewManager(OpalViewPersistenceStrategy viewPersistenceStrategy) {
    super(viewPersistenceStrategy);
  }

  @Override
  public Datasource decorate(Datasource datasource) {
    Datasource decorated = super.decorate(datasource);
    // after super, which unregisters the datasource of the same name
    datasources.put(decorated.getName(), (ViewAwareDatasource) decorated);
    return decorated;
  }

  @Override
  public void release(Datasource datasource) {
    super.release(datasource);
    if (datasource != null) datasources.remove(datasource.getName());
  }

  @Override
  public void unregisterDatasource(String datasourceName) {
    super.unregisterDatasource(datasourceName);
    datasources.remove(datasourceName);
  }

  /**
   * Get the views of all the datasources that refer to a table, whatever the permissions of the caller. The references
   * are read without initialising the views, so that a broken view is found as well. A view referring to itself is not
   * its own dependent, otherwise it could not be deleted.
   */
  public List<DependentView> getDependentViews(String datasource, String table) {
    String reference = datasource + "." + table;
    return datasources.values().stream()
        .flatMap(ds -> getViews(ds).stream()
            .filter(view -> view instanceof View tableView && getReferences(tableView).anyMatch(reference::equals))
            .map(view -> new DependentView(ds.getName(), view.getName())))
        .filter(view -> !view.reference().equals(reference))
        .sorted(Comparator.comparing(DependentView::reference))
        .toList();
  }

  private static Set<ValueView> getViews(ViewAwareDatasource datasource) {
    // getViews() is not synchronized, but what changes the views is: without this lock a concurrent change fails the copy
    synchronized (datasource) {
      return datasource.getViews();
    }
  }

  private static Stream<String> getReferences(View view) {
    return getReferences(view.getWrappedValueTable());
  }

  private static Stream<String> getReferences(ValueTable table) {
    if (table instanceof JoinTable join) return join.getTables().stream().flatMap(OpalViewManager::getReferences);
    if (table instanceof ValueTableReference reference) return Stream.of(reference.getReference());
    // without a datasource it cannot refer to a table by name: a malformed view must not fail the table requests
    if (table == null || table.getDatasource() == null) return Stream.empty();
    return Stream.of(table.getDatasource().getName() + "." + table.getName());
  }

}
