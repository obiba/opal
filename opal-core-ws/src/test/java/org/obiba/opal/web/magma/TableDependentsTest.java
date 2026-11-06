/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import com.google.common.eventbus.EventBus;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableStatus;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.cfg.OpalViewManager;
import org.obiba.opal.core.cfg.OpalViewManager.DependentView;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Ws.ClientErrorDto;

import java.lang.reflect.Field;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TableDependentsTest {

  private static final String DS = "ds";

  private final DependentView readable = new DependentView("other", "readable");

  private final DependentView unreadable = new DependentView("other", "unreadable");

  private Subject subject;

  private Datasource datasource;

  private OpalViewManager viewManager;

  private EventBus eventBus;

  @Before
  public void setUp() {
    new MagmaEngine();
    subject = mock(Subject.class);
    when(subject.isPermitted("rest:/datasource/other/table/readable:GET")).thenReturn(true);
    ThreadContext.bind(subject);
    datasource = mock(Datasource.class);
    when(datasource.getName()).thenReturn(DS);
    viewManager = mock(OpalViewManager.class);
    eventBus = mock(EventBus.class);
  }

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
    MagmaEngine.get().shutdown();
  }

  private ValueTable table(String name, boolean view) {
    ValueTable table = mock(ValueTable.class);
    when(table.getName()).thenReturn(name);
    when(table.getDatasource()).thenReturn(datasource);
    when(table.getEntityType()).thenReturn("Participant");
    when(table.isView()).thenReturn(view);
    Timestamps timestamps = mock(Timestamps.class);
    when(timestamps.getCreated()).thenReturn(DateTimeType.get().now());
    when(timestamps.getLastUpdate()).thenReturn(DateTimeType.get().now());
    when(table.getTimestamps()).thenReturn(timestamps);
    when(table.getStatus()).thenReturn(ValueTableStatus.READY);
    when(datasource.getValueTable(name)).thenReturn(table);
    when(datasource.hasValueTable(name)).thenReturn(true);
    when(datasource.canDropTable(name)).thenReturn(true);
    when(subject.isPermitted("rest:/datasource/" + DS + "/table/" + name + ":DELETE")).thenReturn(true);
    return table;
  }

  private void dependents(String table, DependentView... views) {
    when(viewManager.getDependentViews(DS, table)).thenReturn(List.of(views));
  }

  private static void inject(Object target, Class<?> type, String field, Object value) throws Exception {
    Field f = type.getDeclaredField(field);
    f.setAccessible(true);
    f.set(target, value);
  }

  private void assertConflict(Response response, String... arguments) {
    assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    ClientErrorDto error = (ClientErrorDto) response.getEntity();
    assertThat(error.getStatus()).isEqualTo("TableHasDependentViews");
    assertThat(List.copyOf(error.getArgumentsList())).isEqualTo(List.of(arguments));
  }

  private <T extends TableResourceImpl> T init(T resource, ValueTable table) throws Exception {
    resource.setValueTable(table);
    resource.setViewManager(viewManager);
    return resource;
  }

  private DatasourceTablesResourceImpl tablesResource() throws Exception {
    DatasourceTablesResourceImpl resource = new DatasourceTablesResourceImpl();
    resource.setDatasource(datasource);
    resource.setViewManager(viewManager);
    inject(resource, DatasourceTablesResourceImpl.class, "eventBus", eventBus);
    inject(resource, DatasourceTablesResourceImpl.class, "subjectProfileService", mock(SubjectProfileService.class));
    return resource;
  }

  @Test
  public void table_names_readable_views_and_counts_the_others() throws Exception {
    ValueTable table = table("t", false);
    dependents("t", readable, unreadable);
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath(false)).thenReturn("/datasource/ds/table/t");

    Magma.TableDto dto = init(new TableResourceImpl(), table).get(null, uriInfo, false);

    assertThat(dto.getDependentsList()).containsExactly("other.readable");
    assertThat(dto.getHiddenDependents()).isEqualTo(1);
  }

  @Test
  public void drop_is_refused_and_does_not_name_unreadable_views() throws Exception {
    ValueTable table = table("t", false);
    dependents("t", readable, unreadable);

    assertConflict(init(new DroppableTableResourceImpl(), table).drop(), "other.readable");

    verify(datasource, never()).dropTable(anyString());
  }

  @Test
  public void drop_is_refused_by_a_view_the_caller_cannot_read() throws Exception {
    ValueTable table = table("t", false);
    dependents("t", unreadable);

    assertConflict(init(new DroppableTableResourceImpl(), table).drop());

    verify(datasource, never()).dropTable(anyString());
  }

  @Test
  public void view_on_a_view_prevents_its_removal() throws Exception {
    ValueTable view = table("v", true);
    dependents("v", readable);
    ViewResourceImpl resource = init(new ViewResourceImpl(), view);

    assertConflict(resource.removeView(), "other.readable");

    verify(viewManager, never()).removeView(anyString(), anyString());
  }

  @Test
  public void rename_of_table_is_refused() throws Exception {
    ValueTable table = table("t", false);
    dependents("t", readable);
    when(datasource.canRenameTable("t")).thenReturn(true);
    TableResourceImpl resource = init(new TableResourceImpl(), table);

    assertConflict(resource.update(Magma.TableDto.newBuilder().setName("new").setEntityType("Participant").build()),
        "other.readable");

    verify(datasource, never()).renameTable(anyString(), anyString());
  }

  @Test
  public void rename_of_view_is_refused() throws Exception {
    ValueTable view = table("v", true);
    dependents("v", readable);
    ViewResourceImpl resource = init(new ViewResourceImpl(), view);
    Magma.ViewDto dto = Magma.ViewDto.newBuilder().setName("new")
        .setExtension(Magma.VariableListViewDto.view, Magma.VariableListViewDto.getDefaultInstance()).build();

    assertConflict(resource.updateView(dto, null), "other.readable");

    verify(viewManager, never()).removeView(anyString(), anyString());
    verify(viewManager, never()).addView(anyString(), any(), any(), any());
  }

  @Test
  public void batch_is_refused_as_a_whole() throws Exception {
    table("free", false);
    table("t", false);
    dependents("t", readable);

    assertConflict(tablesResource().deleteTables(List.of("free", "t")), "other.readable");

    verify(datasource, never()).dropTable(anyString());
  }

  @Test
  public void batch_deletes_table_with_its_dependent_view() throws Exception {
    table("t", false);
    table("v", true);
    dependents("t", new DependentView(DS, "v"));

    Response response = tablesResource().deleteTables(List.of("t", "v"));

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    InOrder order = inOrder(viewManager, datasource);
    order.verify(viewManager).removeView(DS, "v");
    order.verify(datasource).dropTable("t");
  }

  @Test
  public void batch_deletes_a_chain_from_its_end() throws Exception {
    table("t", false);
    table("v1", true);
    table("v2", true);
    dependents("t", new DependentView(DS, "v1"));
    dependents("v1", new DependentView(DS, "v2"));

    tablesResource().deleteTables(List.of("t", "v1", "v2"));

    InOrder order = inOrder(viewManager, datasource);
    order.verify(viewManager).removeView(DS, "v2");
    order.verify(viewManager).removeView(DS, "v1");
    order.verify(datasource).dropTable("t");
  }

  @Test
  public void failed_removal_of_a_view_keeps_its_table() throws Exception {
    table("t", false);
    table("v", true);
    dependents("t", new DependentView(DS, "v"));
    doThrow(new IllegalStateException()).when(viewManager).removeView(DS, "v");

    try {
      tablesResource().deleteTables(List.of("t", "v"));
      failBecauseExceptionWasNotThrown(IllegalStateException.class);
    } catch(IllegalStateException e) {
      verify(datasource, never()).dropTable(anyString());
    }
  }

  @Test
  public void batch_deletes_views_referring_to_each_other() throws Exception {
    table("a", true);
    table("b", true);
    dependents("a", new DependentView(DS, "b"));
    dependents("b", new DependentView(DS, "a"));

    Response response = tablesResource().deleteTables(List.of("a", "b"));

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    verify(viewManager).removeView(DS, "a");
    verify(viewManager).removeView(DS, "b");
  }

  @Test
  public void batch_is_refused_by_the_end_of_a_chain_left_out() throws Exception {
    table("t", false);
    table("v1", true);
    dependents("t", new DependentView(DS, "v1"));
    dependents("v1", new DependentView(DS, "v2"));

    assertConflict(tablesResource().deleteTables(List.of("t", "v1")));

    verify(datasource, never()).dropTable(anyString());
  }

  @Test
  public void batch_is_refused_by_a_dependent_skipped_for_lack_of_permission() throws Exception {
    table("t", false);
    table("v", true);
    dependents("t", new DependentView(DS, "v"));
    when(subject.isPermitted("rest:/datasource/" + DS + "/table/v:DELETE")).thenReturn(false);

    assertConflict(tablesResource().deleteTables(List.of("t", "v")));

    verify(datasource, never()).dropTable(anyString());
  }

  @Test
  public void same_name_in_another_datasource_is_not_deleted_with_the_batch() throws Exception {
    table("t", false);
    table("v", true);
    dependents("t", new DependentView("other", "v"));
    when(subject.isPermitted("rest:/datasource/other/table/v:GET")).thenReturn(true);

    assertConflict(tablesResource().deleteTables(List.of("t", "v")), "other.v");

    verify(datasource, never()).dropTable(anyString());
  }
}
