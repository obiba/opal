/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.KeyStoreState;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.*;
import org.obiba.opal.core.service.security.realm.OpalApplicationRealm;
import org.obiba.opal.core.service.security.realm.OpalUserRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;

import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

@ContextConfiguration(classes = SubjectCredentialsServiceImplTest.Config.class)
public class SubjectCredentialsServiceImplTest extends AbstractOrientdbServiceTest {

//  private static final Logger log = LoggerFactory.getLogger(SubjectCredentialsServiceImplTest.class);

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

  @Override
  public void startDB() throws Exception {
    super.startDB();
    orientDbService.deleteAll(SubjectCredentials.class);
    orientDbService.deleteAll(Group.class);
    orientDbService.deleteAll(KeyStoreState.class);
  }

  @Test
  public void test_validate_special_chars_password() {
    subjectCredentialsService.hashPassword("P@ssw0rd");
    subjectCredentialsService.hashPassword("P#ssw0rd");
    subjectCredentialsService.hashPassword("P$ssw0rd");
    subjectCredentialsService.hashPassword("P%ssw0rd");
    subjectCredentialsService.hashPassword("P^ssw0rd");
    subjectCredentialsService.hashPassword("P&ssw0rd");
    subjectCredentialsService.hashPassword("P+ssw0rd");
    subjectCredentialsService.hashPassword("P=ssw0rd");
    subjectCredentialsService.hashPassword("P!ssw0rd");
    try {
      subjectCredentialsService.hashPassword("Pssw0rd*éà");
      assertThat(true).isFalse();
    } catch (PasswordTooWeakException e) {
      assertThat(true).isTrue();
    }
    try {
      subjectCredentialsService.hashPassword("P@ssw0rd ");
      assertThat(true).isFalse();
    } catch (PasswordTooWeakException e) {
      assertThat(true).isTrue();
    }
  }

  @Test
  public void test_create_new_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password")
        .enabled(true).build();
    subjectCredentialsService.save(subjectCredentials);

    List<SubjectCredentials> list = newArrayList(
        subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.PASSWORD));
    assertThat(list).hasSize(1);
    assertSubjectEquals(subjectCredentials, list.get(0));

    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    assertSubjectEquals(subjectCredentials, found);
  }

  @Test
  public void test_create_new_application() throws IOException {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.CERTIFICATE).name("app1")
        .certificate(getCertificate()).enabled(true).build();
    subjectCredentialsService.save(subjectCredentials);

    List<SubjectCredentials> list = newArrayList(
        subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.CERTIFICATE));
    assertThat(list).hasSize(1);
    assertSubjectEquals(subjectCredentials, list.get(0));

    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    assertSubjectEquals(subjectCredentials, found);

    OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
    assertThat(keyStore.aliasExists(subjectCredentials.getCertificateAlias())).isTrue();
    assertThat(keyStore.getKeyType(subjectCredentials.getCertificateAlias()))
        .isEqualTo(OpalKeyStore.KeyType.CERTIFICATE);
  }

  @Test
  public void test_update_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password")
        .enabled(true).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentials.setPassword("new password");
    subjectCredentialsService.save(subjectCredentials);

    List<SubjectCredentials> list = newArrayList(
        subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.PASSWORD));
    assertThat(list).hasSize(1);
    assertSubjectEquals(subjectCredentials, list.get(0));

    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    assertSubjectEquals(subjectCredentials, found);
    Asserts.assertUpdatedTimestamps(subjectCredentials, found);
  }

  private byte[] getCertificate() throws IOException {
    return Files.toByteArray(
        ResourceUtils.getFile("classpath:org/obiba/opal/core/service/security/test-certificate-public.pem"));
  }

  @Test
  public void test_create_new_group() {
    Group group = Group.Builder.create().name("group1").build();
    subjectCredentialsService.createGroup(group.getName());

    List<Group> groups = newArrayList(subjectCredentialsService.getGroups());
    assertThat(groups).hasSize(1);
    assertGroupEquals(group, groups.get(0));

    Group found = subjectCredentialsService.getGroup(group.getName());
    assertGroupEquals(group, found);
  }

  @Test
  public void test_create_groups_from_user() {

    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password")
        .groups(Sets.newHashSet("group1", "group2")).build();
    subjectCredentialsService.save(subjectCredentials);

    assertThat(subjectCredentialsService.getGroups()).hasSize(2);

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertThat(group1).isNotNull();
    assertThat(group1.getSubjectCredentials()).hasSize(1);
    assertThat(group1.getSubjectCredentials()).contains(subjectCredentials.getName());

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertThat(group2).isNotNull();
    assertThat(group2.getSubjectCredentials()).hasSize(1);
    assertThat(group2.getSubjectCredentials()).contains(subjectCredentials.getName());
  }

  @Test
  public void test_remove_groups_from_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password")
        .groups(Sets.newHashSet("group1", "group2")).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentials.removeGroup("group1");
    subjectCredentialsService.save(subjectCredentials);

    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    assertSubjectEquals(subjectCredentials, found);

    assertThat(subjectCredentialsService.getGroups()).hasSize(2);

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertThat(group1).isNotNull();
    assertThat(group1.getSubjectCredentials()).isEmpty();

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertThat(group2).isNotNull();
    assertThat(group2.getSubjectCredentials()).hasSize(1);
    assertThat(group2.getSubjectCredentials()).contains(subjectCredentials.getName());
  }

  @Test
  public void test_delete_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password").build();
    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.delete(subjectCredentials);
    assertThat(subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.PASSWORD))
        .isEmpty();
  }

  @Test
  public void test_delete_application() throws IOException {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.CERTIFICATE).name("app1")
        .certificate(getCertificate()).enabled(true).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentialsService.delete(subjectCredentials);
    assertThat(subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.CERTIFICATE))
        .isEmpty();

    OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
    assertThat(keyStore.aliasExists(subjectCredentials.getName())).isFalse();
  }

  @Test
  public void test_delete_user_with_groups() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password")
        .groups(Sets.newHashSet("group1", "group2")).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentialsService.delete(subjectCredentials);

    assertThat(subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.PASSWORD))
        .isEmpty();

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertThat(group1.getSubjectCredentials()).isEmpty();

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertThat(group2.getSubjectCredentials()).isEmpty();
  }

  @Test
  public void test_delete_group() {
    Group group = Group.Builder.create().name("group1").build();
    subjectCredentialsService.createGroup(group.getName());
    subjectCredentialsService.delete(group);
    assertThat(subjectCredentialsService.getGroups()).isEmpty();
  }

  @Test
  public void test_delete_group_with_subjects() {
    // TODO
  }

  @Test(expected = SubjectPrincipalNotFoundException.class)
  public void test_change_password_with_wrong_principal() {
    subjectCredentialsService.changePassword("kuser", "password", "password");
  }

  @Test(expected = PasswordTooShortException.class)
  public void test_change_password_with_short_password() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1")
        .password(subjectCredentialsService.hashPassword("P@ssw0rd")).build();

    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.changePassword("user1", "P@ssw0rd", "pass");
  }

  @Test(expected = PasswordTooLongException.class)
  public void test_change_password_with_long_password() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1")
        .password(subjectCredentialsService.hashPassword("P@ssw0rd")).build();

    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.changePassword("user1", "P@ssw0rd", "passP@ssw0rdP@ssw0rdP@ssw0rdP@ssw0rdP@ssw0rdP@ssw0rdP@ssw0rdP@ssw0rd");
  }

  @Test(expected = PasswordTooWeakException.class)
  public void test_change_password_with_weak_password() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1")
        .password(subjectCredentialsService.hashPassword("P@ssw0rd")).build();

    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.changePassword("user1", "P@ssw0rd", "password");
  }

  @Test(expected = OldPasswordMismatchException.class)
  public void test_change_password_with_old_password_mismatch() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1")
        .password(subjectCredentialsService.hashPassword("P@ssw0rd")).build();

    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.changePassword("user1", "P@ssw0rd0", "P@ssw0rd1");
  }

  @Test(expected = PasswordNotChangedException.class)
  public void test_change_password_with_password_unchanged() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1")
        .password(subjectCredentialsService.hashPassword("P@ssw0rd")).build();

    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.changePassword("user1", "P@ssw0rd", "P@ssw0rd");
  }

  @Test
  public void test_change_password_with_password_changed() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1")
        .password(subjectCredentialsService.hashPassword("P@ssw0rd")).build();

    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.changePassword("user1", "P@ssw0rd", "P@ssw0rd1");
    SubjectCredentials subjectCredentials1 = subjectCredentialsService.getSubjectCredentials("user1");
    assertThat(subjectCredentials1.getPassword().equals(subjectCredentialsService.hashPassword("P@ssw0rd1"))).isTrue();
  }

  private void assertSubjectEquals(SubjectCredentials expected, SubjectCredentials found) {
    assertThat(found).isNotNull();
    assertThat(found).isEqualTo(expected);
    assertThat(found.getName()).isEqualTo(expected.getName());
    assertThat(found.getAuthenticationType()).isEqualTo(expected.getAuthenticationType());
    assertThat(found.getPassword()).isEqualTo(expected.getPassword());
    assertThat(found.isEnabled()).isEqualTo(expected.isEnabled());

    assertThat(expected.getGroups()).isEqualTo(found.getGroups());
    Asserts.assertCreatedTimestamps(expected, found);
  }

  private void assertGroupEquals(Group expected, Group found) {
    assertThat(found).isNotNull();
    assertThat(found).isEqualTo(expected);
    assertThat(found.getName()).isEqualTo(expected.getName());
    assertThat(expected.getSubjectCredentials()).isEqualTo(found.getSubjectCredentials());
    Asserts.assertCreatedTimestamps(expected, found);
  }

  @Configuration
  @PropertySource("classpath:org/obiba/opal/core/service/security/SubjectCredentialsServiceImplTest.properties")
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public EventBus eventBus() {
      return new EventBus();
    }

    @Bean
    public SubjectCredentialsService subjectCredentialsService() {
      return new SubjectCredentialsServiceImpl();
    }

    @Bean
    public CredentialsKeyStoreService credentialsKeyStoreService() {
      return new CredentialsKeyStoreServiceImpl();
    }

    @Bean
    public CallbackHandler callbackHandler() {
      return ProjectsKeyStoreServiceImplTest.createPasswordCallbackHandler();
    }

    @Bean
    public SubjectAclService subjectAclService() {
      return EasyMock.createMock(SubjectAclService.class);
    }

    @Bean
    public SubjectProfileService subjectProfileService() {
      SubjectProfileService subjectProfileService = EasyMock.createMock(SubjectProfileService.class);
      subjectProfileService.afterPropertiesSet();
      expect(subjectProfileService.getProfile("user1")).andReturn(new SubjectProfile("user1", OpalUserRealm.OPAL_REALM))
          .anyTimes();
      expect(subjectProfileService.getProfile("app1"))
          .andReturn(new SubjectProfile("app1", OpalApplicationRealm.APPLICATION_REALM)).anyTimes();
      subjectProfileService.ensureProfile("user1", OpalUserRealm.OPAL_REALM);
      expectLastCall().anyTimes();
      subjectProfileService.ensureProfile("app1", OpalApplicationRealm.APPLICATION_REALM);
      expectLastCall().anyTimes();
      subjectProfileService.deleteProfile("user1");
      expectLastCall().anyTimes();
      subjectProfileService.deleteProfile("app1");
      expectLastCall().anyTimes();
      replay(subjectProfileService);
      return subjectProfileService;
    }
  }

}
