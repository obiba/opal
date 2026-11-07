/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.analysis;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.security.DatasourcePermissionConverter;
import org.obiba.opal.core.security.TablePermissionConverter;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.realm.OpalPermissionResolver;
import org.obiba.opal.web.model.Projects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * The permissions are the actual ones of each role, resolved as the security realm does.
 */
public class ProjectAnalysisResourceTest {

  private static final String TABLE_A = "/datasource/project/table/a";

  private static final String TABLE_B = "/datasource/project/table/b";

  private static final String DATASOURCE = "/datasource/project";

  private final OpalPermissionResolver resolver = new OpalPermissionResolver();

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void testGetAnalyses_OnlyTheAnalysesOfTheTableWithValuesAccess() throws Exception {
    assertThat(listedTables(tablePermission(TABLE_A, TablePermissionConverter.Permission.TABLE_VALUES)))
        .containsExactly("a");
  }

  @Test
  public void testGetAnalyses_OnlyTheAnalysesOfTheTableWithFullAccess() throws Exception {
    assertThat(listedTables(tablePermission(TABLE_A, TablePermissionConverter.Permission.TABLE_ALL)))
        .containsExactly("a");
  }

  @Test
  public void testGetAnalyses_ReadingATableDoesNotGiveItsAnalyses() throws Exception {
    List<String> granted = tablePermission(TABLE_A, TablePermissionConverter.Permission.TABLE_VALUES);
    granted.addAll(tablePermission(TABLE_B, TablePermissionConverter.Permission.TABLE_READ));

    assertThat(listedTables(granted)).containsExactly("a");
  }

  @Test
  public void testGetAnalyses_AllTheAnalysesWithViewAccessToTheDatasource() throws Exception {
    assertThat(listedTables(datasourcePermission(DatasourcePermissionConverter.Permission.DATASOURCE_VIEW)))
        .containsExactly("a", "b");
  }

  @Test
  public void testGetAnalyses_AllTheAnalysesWithFullAccessToTheDatasource() throws Exception {
    assertThat(listedTables(datasourcePermission(DatasourcePermissionConverter.Permission.DATASOURCE_ALL)))
        .containsExactly("a", "b");
  }

  private List<String> tablePermission(String node, TablePermissionConverter.Permission permission) {
    List<String> granted = new ArrayList<>();
    new TablePermissionConverter().convert("opal", node, permission.toString()).forEach(granted::add);
    return granted;
  }

  private List<String> datasourcePermission(DatasourcePermissionConverter.Permission permission) {
    List<String> granted = new ArrayList<>();
    new DatasourcePermissionConverter().convert("opal", DATASOURCE, permission.toString()).forEach(granted::add);
    return granted;
  }

  private List<String> listedTables(List<String> granted) throws Exception {
    Subject subject = createMock(Subject.class);
    expect(subject.isPermitted(EasyMock.anyString()))
        .andStubAnswer(() -> isGranted(granted, (String) getCurrentArguments()[0]));
    replay(subject);
    ThreadContext.bind(subject);

    OpalAnalysisService analysisService = createMock(OpalAnalysisService.class);
    expect(analysisService.getAnalysesByDatasource("project")).andStubReturn(List.of(analysis("a"), analysis("b")));
    ProjectService projectService = createNiceMock(ProjectService.class);
    replay(analysisService, projectService);

    ProjectAnalysisResource resource = new ProjectAnalysisResource();
    set(resource, "analysisService", analysisService);
    set(resource, "projectService", projectService);
    set(resource, "name", "project");

    return resource.getAnalyses().getAnalysesList().stream().map(Projects.OpalAnalysisDto::getTable)
        .collect(Collectors.toList());
  }

  private boolean isGranted(List<String> granted, String requested) {
    Permission permission = resolver.resolvePermission(requested);
    return granted.stream().anyMatch(perm -> resolver.resolvePermission(perm).implies(permission));
  }

  private OpalAnalysis analysis(String table) {
    OpalAnalysis analysis = createMock(OpalAnalysis.class);
    expect(analysis.getName()).andStubReturn("analysis-" + table);
    expect(analysis.getDatasource()).andStubReturn("project");
    expect(analysis.getTable()).andStubReturn(table);
    expect(analysis.getPluginName()).andStubReturn("plugin");
    expect(analysis.getTemplateName()).andStubReturn("template");
    expect(analysis.getParameters()).andStubReturn(new JSONObject());
    expect(analysis.getCreated()).andStubReturn(new Date(1));
    expect(analysis.getUpdated()).andStubReturn(new Date(1));
    expect(analysis.getVariables()).andStubReturn(List.of());
    replay(analysis);
    return analysis;
  }

  private void set(ProjectAnalysisResource resource, String field, Object value) throws Exception {
    Field f = ProjectAnalysisResource.class.getDeclaredField(field);
    f.setAccessible(true);
    f.set(resource, value);
  }
}
