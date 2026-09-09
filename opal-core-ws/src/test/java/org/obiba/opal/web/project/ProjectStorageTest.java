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

import com.google.common.eventbus.EventBus;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.ProjectService.ProjectStorage;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.VCFSamplesMappingService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.web.model.Projects;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * How a payload is read as the storage a project should have. The case that matters is the one that says nothing:
 * a client that predates internal storage sends back the project it read, in which an internal database is not named.
 */
public class ProjectStorageTest {

  private ProjectService projectService;

  private Capture<ProjectStorage> storage;

  @Before
  public void setUp() {
    bindAPermittedSubject();
    projectService = createMock(ProjectService.class);
    storage = newCapture();
  }

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
    SecurityUtils.setSecurityManager(null);
  }

  /**
   * The sharpest edge of internal storage: this payload is what a 5.x client PUTs back, and reading it as "no
   * storage" would delete the project's database and its data.
   */
  @Test
  public void test_a_put_that_says_nothing_leaves_an_internal_database_alone() {
    assertThat(storageOfUpdate(project("CLSA", "_project_CLSA"), legacyDto("CLSA")).kind())
        .isEqualTo(ProjectStorage.Kind.INTERNAL);
  }

  @Test
  public void test_a_put_that_says_nothing_leaves_a_registered_database_alone() {
    ProjectStorage resolved = storageOfUpdate(project("CLSA", "opal-data"), legacyDto("CLSA"));
    assertThat(resolved.kind()).isEqualTo(ProjectStorage.Kind.REGISTERED);
    assertThat(resolved.databaseName()).isEqualTo("opal-data");
  }

  @Test
  public void test_a_put_that_says_nothing_leaves_a_project_without_storage_alone() {
    assertThat(storageOfUpdate(project("CLSA", null), legacyDto("CLSA")).kind()).isEqualTo(ProjectStorage.Kind.NONE);
  }

  @Test
  public void test_a_put_asking_for_an_internal_database() {
    Projects.ProjectDto dto = legacyDto("CLSA").toBuilder().setInternalDatabase(true).build();
    assertThat(storageOfUpdate(project("CLSA", "opal-data"), dto).kind()).isEqualTo(ProjectStorage.Kind.INTERNAL);
  }

  @Test
  public void test_a_put_asking_for_a_registered_database() {
    Projects.ProjectDto dto = legacyDto("CLSA").toBuilder().setDatabase("opal-data").build();
    ProjectStorage resolved = storageOfUpdate(project("CLSA", "_project_CLSA"), dto);
    assertThat(resolved.kind()).isEqualTo(ProjectStorage.Kind.REGISTERED);
    assertThat(resolved.databaseName()).isEqualTo("opal-data");
  }

  /**
   * Detaching a project from its storage is asked for - an empty database name - rather than left out, because
   * leaving it out is what an older client does with a database it cannot name.
   */
  @Test
  public void test_a_put_asking_for_no_storage_at_all() {
    Projects.ProjectDto dto = legacyDto("CLSA").toBuilder().setDatabase("").build();
    assertThat(storageOfUpdate(project("CLSA", "opal-data"), dto).kind()).isEqualTo(ProjectStorage.Kind.NONE);
  }

  @Test
  public void test_creating_a_project_with_each_kind_of_storage() {
    assertThat(storageOfCreate(factoryDto("CLSA").setInternalDatabase(true).build()).kind())
        .isEqualTo(ProjectStorage.Kind.INTERNAL);

    ProjectStorage registered = storageOfCreate(factoryDto("CLSA").setDatabase("opal-data").build());
    assertThat(registered.kind()).isEqualTo(ProjectStorage.Kind.REGISTERED);
    assertThat(registered.databaseName()).isEqualTo("opal-data");

    assertThat(storageOfCreate(factoryDto("CLSA").build()).kind()).isEqualTo(ProjectStorage.Kind.NONE);
    assertThat(storageOfCreate(factoryDto("CLSA").setDatabase("").build()).kind()).isEqualTo(ProjectStorage.Kind.NONE);
  }

  //
  // Private methods
  //

  private ProjectStorage storageOfUpdate(Project stored, Projects.ProjectDto dto) {
    reset(projectService);
    expect(projectService.getProject(stored.getName())).andReturn(stored).anyTimes();
    projectService.save(anyObject(Project.class), capture(storage));
    expectLastCall().once();
    replay(projectService);

    projectResource().update(dto, stored.getName());

    verify(projectService);
    return storage.getValue();
  }

  private ProjectStorage storageOfCreate(Projects.ProjectFactoryDto dto) {
    reset(projectService);
    expect(projectService.hasProject(dto.getName())).andReturn(false).once();
    Capture<ProjectStorage> captured = newCapture();
    projectService.save(anyObject(Project.class), capture(captured));
    // stop before the resource builds the location URI, which needs a JAX-RS runtime
    expectLastCall().andThrow(new StopHere()).once();
    replay(projectService);

    try {
      projectsResource().createProject(null, dto);
    } catch(StopHere ignored) {
    }

    verify(projectService);
    return captured.getValue();
  }

  private ProjectResource projectResource() {
    return new ProjectResource(EasyMock.createNiceMock(OpalRuntime.class), projectService,
        EasyMock.createNiceMock(EventBus.class), EasyMock.createNiceMock(ProjectsKeyStoreService.class),
        EasyMock.createNiceMock(ApplicationContext.class),
        EasyMock.createNiceMock(VCFSamplesMappingService.class),
        EasyMock.createNiceMock(SubjectProfileService.class));
  }

  private ProjectsResource projectsResource() {
    try {
      ProjectsResource resource = new ProjectsResource();
      Field field = ProjectsResource.class.getDeclaredField("projectService");
      field.setAccessible(true);
      field.set(resource, projectService);
      return resource;
    } catch(ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The project as an older client sends it back: what it read, minus the fields it does not know about.
   */
  private Projects.ProjectDto legacyDto(String name) {
    return Projects.ProjectDto.newBuilder().setName(name).setTitle(name).build();
  }

  private Projects.ProjectFactoryDto.Builder factoryDto(String name) {
    return Projects.ProjectFactoryDto.newBuilder().setName(name).setTitle(name);
  }

  private Project project(String name, String database) {
    Project project = new Project(name);
    project.setTitle(name);
    project.setDatabase(database);
    return project;
  }

  private void bindAPermittedSubject() {
    DefaultSecurityManager securityManager = new DefaultSecurityManager(new AuthorizingRealm() {
      @Override
      protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addStringPermission("*");
        return info;
      }

      @Override
      protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        return new SimpleAuthenticationInfo("tester", "", getName());
      }
    });
    SecurityUtils.setSecurityManager(securityManager);
    ThreadContext.bind(new Subject.Builder(securityManager)
        .principals(new SimplePrincipalCollection("tester", "test-realm")).authenticated(true).buildSubject());
  }

  private static class StopHere extends RuntimeException {
  }
}
