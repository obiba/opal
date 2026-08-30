/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.repository.ResourceReferenceRepository;
import org.obiba.opal.core.service.security.CryptoService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Deleting the resources of a project used to go through the listing the read path uses, which hides the references
 * the current subject cannot view. A subject with project-scoped rather than global permissions therefore left rows
 * behind - orphaned, credentials and all - and the node permissions removed alongside them meant nobody could see
 * them afterwards either, so no later deletion could reach them.
 */
@ContextConfiguration(classes = ResourceReferenceServiceImplTest.Config.class)
public class ResourceReferenceServiceImplTest extends AbstractConfigDbTest {

  private static final String PROJECT = "project";

  @Autowired
  private ResourceReferenceService resourceReferenceService;

  @Autowired
  private ResourceReferenceRepository resourceReferenceRepository;

  @Before
  public void setUpReferences() {
    resourceReferenceRepository.deleteAll();
    resourceReferenceRepository.upsert(reference("visible"));
    resourceReferenceRepository.upsert(reference("hidden"));
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
    SecurityUtils.setSecurityManager(null);
  }

  @Test
  public void test_delete_all_takes_the_references_the_subject_cannot_view() {
    login("rest:/project/" + PROJECT + "/resource/visible:GET");

    resourceReferenceService.deleteAll(PROJECT);

    assertThat(resourceReferenceRepository.findByProject(PROJECT)).isEmpty();
  }

  @Test
  public void test_delete_all_takes_everything_when_the_subject_can_view_nothing() {
    // What the project deletion event looks like when the subject that triggered it has no rights on the resources.
    login();

    resourceReferenceService.deleteAll(PROJECT);

    assertThat(resourceReferenceRepository.findByProject(PROJECT)).isEmpty();
  }

  @Test
  public void test_delete_all_leaves_the_references_of_other_projects() {
    resourceReferenceRepository.upsert(reference("elsewhere", "other"));
    login("rest:*");

    resourceReferenceService.deleteAll(PROJECT);

    List<ResourceReference> others = resourceReferenceRepository.findByProject("other");
    assertThat(others).hasSize(1);
    assertThat(others.get(0).getName()).isEqualTo("elsewhere");
  }

  @Test
  public void test_delete_by_name_takes_a_reference_the_subject_cannot_view() {
    login("rest:/project/" + PROJECT + "/resource/visible:GET");

    resourceReferenceService.delete(PROJECT, "hidden");

    assertThat(resourceReferenceRepository.findByProjectAndName(PROJECT, "hidden").isPresent()).isFalse();
    assertThat(resourceReferenceRepository.findByProjectAndName(PROJECT, "visible").isPresent()).isTrue();
  }

  @Test
  public void test_delete_by_name_of_an_unknown_reference_is_ignored() {
    login("rest:*");

    resourceReferenceService.delete(PROJECT, "nope");

    assertThat(resourceReferenceRepository.findByProject(PROJECT)).hasSize(2);
  }

  private void login(String... permissions) {
    DefaultSecurityManager securityManager = new DefaultSecurityManager(new PermissionsRealm(Set.of(permissions)));
    SecurityUtils.setSecurityManager(securityManager);
    Subject subject = new Subject.Builder(securityManager).buildSubject();
    ThreadContext.bind(subject);
    subject.login(new UsernamePasswordToken("user", "password"));
  }

  private ResourceReference reference(String name) {
    return reference(name, PROJECT);
  }

  private ResourceReference reference(String name, String project) {
    ResourceReference reference = new ResourceReference();
    reference.setProject(project);
    reference.setName(name);
    reference.setProvider("provider");
    reference.setFactory("factory");
    reference.setEncryptedCredentialsModel("{\"secret\":\"encrypted\"}");
    return reference;
  }

  private static class PermissionsRealm extends AuthorizingRealm {

    private final Set<String> permissions;

    private PermissionsRealm(Set<String> permissions) {
      this.permissions = permissions;
      setName("test");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
      SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
      info.setStringPermissions(permissions);
      return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
      return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
    }
  }

  @Configuration
  public static class Config extends AbstractConfigDbTestConfig {

    @Bean
    public CryptoService cryptoService() {
      CryptoService mock = EasyMock.createNiceMock(CryptoService.class);
      EasyMock.expect(mock.encrypt(EasyMock.anyString())).andReturn("encrypted").anyTimes();
      EasyMock.expect(mock.decrypt(EasyMock.anyString())).andReturn("decrypted").anyTimes();
      EasyMock.replay(mock);
      return mock;
    }

    @Bean
    public SubjectAclService subjectAclService() {
      SubjectAclService mock = EasyMock.createNiceMock(SubjectAclService.class);
      EasyMock.replay(mock);
      return mock;
    }

    @Bean
    public ResourceProvidersService resourceProvidersService() {
      ResourceProvidersService mock = EasyMock.createNiceMock(ResourceProvidersService.class);
      EasyMock.replay(mock);
      return mock;
    }

    @Bean
    public ResourceReferenceService resourceReferenceService(ResourceReferenceRepository resourceReferenceRepository) {
      return new ResourceReferenceServiceImpl(resourceReferenceRepository, cryptoService(), subjectAclService(),
          resourceProvidersService());
    }
  }
}
