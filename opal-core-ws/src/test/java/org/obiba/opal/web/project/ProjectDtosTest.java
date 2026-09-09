/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project;

import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.web.model.Projects;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class ProjectDtosTest {

  /**
   * Reading a project's timestamps goes through Magma's value types, which leaves an engine behind. Own it, so that
   * the next test class still finds none.
   */
  @BeforeClass
  public static void startMagma() {
    if(!MagmaEngine.isInstantiated()) new MagmaEngine();
  }

  @AfterClass
  public static void stopMagma() {
    if(MagmaEngine.isInstantiated()) MagmaEngine.get().shutdown();
  }

  /**
   * The name of a project-owned database is an implementation detail of this API: the project says that it has one,
   * and the databases API is where that row is named.
   */
  @Test
  public void test_an_internal_database_is_reported_as_such_and_not_named() {
    Projects.ProjectDto dto = Dtos.asDtoDigest(project("CLSA", "_project_CLSA"), projectService("READY"));

    assertThat(dto.getInternalDatabase()).isTrue();
    assertThat(dto.hasDatabase()).isFalse();
    assertThat(dto.getDatasourceStatus()).isEqualTo(Projects.ProjectDatasourceStatusDto.READY);
  }

  @Test
  public void test_a_registered_database_is_named() {
    Projects.ProjectDto dto = Dtos.asDtoDigest(project("CLSA", "opal-data"), projectService("READY"));

    assertThat(dto.getInternalDatabase()).isFalse();
    assertThat(dto.getDatabase()).isEqualTo("opal-data");
    assertThat(dto.getDatasourceStatus()).isEqualTo(Projects.ProjectDatasourceStatusDto.READY);
  }

  @Test
  public void test_a_project_without_storage() {
    Projects.ProjectDto dto = Dtos.asDtoDigest(project("CLSA", null), projectService("READY"));

    assertThat(dto.getInternalDatabase()).isFalse();
    assertThat(dto.hasDatabase()).isFalse();
    assertThat(dto.getDatasourceStatus()).isEqualTo(Projects.ProjectDatasourceStatusDto.NONE);
  }

  /**
   * Storage is not read from a payload: the resources resolve it, so that a client that cannot name an internal
   * database does not detach one by sending back what it read.
   */
  @Test
  public void test_the_mapping_back_carries_no_storage() {
    Projects.ProjectDto dto = Projects.ProjectDto.newBuilder() //
        .setName("CLSA").setTitle("CLSA").setDatabase("opal-data").build();

    assertThat(Dtos.fromDto(dto).hasDatabase()).isFalse();
    assertThat(Dtos.fromDto(dto).getName()).isEqualTo("CLSA");
  }

  @Test
  public void test_the_factory_mapping_carries_no_storage_either() {
    Projects.ProjectFactoryDto dto = Projects.ProjectFactoryDto.newBuilder() //
        .setName("CLSA").setTitle("CLSA").setDatabase("opal-data").build();

    assertThat(Dtos.fromDto(dto).hasDatabase()).isFalse();
  }

  private Project project(String name, String database) {
    Project project = new Project(name);
    project.setTitle(name);
    project.setDatabase(database);
    return project;
  }

  private ProjectService projectService(String state) {
    ProjectService mock = EasyMock.createNiceMock(ProjectService.class);
    expect(mock.getProjectState(anyObject(Project.class))).andReturn(state).anyTimes();
    replay(mock);
    return mock;
  }
}
