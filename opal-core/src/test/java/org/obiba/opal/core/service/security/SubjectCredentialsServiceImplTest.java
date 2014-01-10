package org.obiba.opal.core.service.security;

import java.io.IOException;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.KeyStoreState;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.AbstractOrientDbTestConfig;
import org.obiba.opal.core.service.Asserts;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.realm.ApplicationRealm;
import org.obiba.opal.core.service.security.realm.OpalUserRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.ResourceUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = SubjectCredentialsServiceImplTest.Config.class)
public class SubjectCredentialsServiceImplTest extends AbstractJUnit4SpringContextTests {

//  private static final Logger log = LoggerFactory.getLogger(SubjectCredentialsServiceImplTest.class);

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

  @Before
  public void clear() throws Exception {
    orientDbService.deleteAll(SubjectCredentials.class);
    orientDbService.deleteAll(Group.class);
    orientDbService.deleteAll(KeyStoreState.class);
  }

  @Test
  public void test_create_new_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password")
        .enabled(true).build();
    subjectCredentialsService.save(subjectCredentials);

    List<SubjectCredentials> list = newArrayList(
        subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.PASSWORD));
    assertEquals(1, list.size());
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
    assertEquals(1, list.size());
    assertSubjectEquals(subjectCredentials, list.get(0));

    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    assertSubjectEquals(subjectCredentials, found);

    OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
    assertThat(keyStore.aliasExists(subjectCredentials.getCertificateAlias()), is(true));
    assertThat(keyStore.getKeyType(subjectCredentials.getCertificateAlias()), is(OpalKeyStore.KeyType.CERTIFICATE));
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
    assertEquals(1, list.size());
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
    assertEquals(1, groups.size());
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

    assertEquals(2, size(subjectCredentialsService.getGroups()));

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertNotNull(group1);
    assertEquals(1, group1.getSubjectCredentials().size());
    assertTrue(group1.getSubjectCredentials().contains(subjectCredentials.getName()));

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertNotNull(group2);
    assertEquals(1, group2.getSubjectCredentials().size());
    assertTrue(group2.getSubjectCredentials().contains(subjectCredentials.getName()));
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

    assertEquals(2, size(subjectCredentialsService.getGroups()));

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertNotNull(group1);
    assertEquals(0, group1.getSubjectCredentials().size());

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertNotNull(group2);
    assertEquals(1, group2.getSubjectCredentials().size());
    assertTrue(group2.getSubjectCredentials().contains(subjectCredentials.getName()));
  }

  @Test
  public void test_delete_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password").build();
    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.delete(subjectCredentials);
    assertEquals(0,
        size(subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.PASSWORD)));
  }

  @Test
  public void test_delete_application() throws IOException {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.CERTIFICATE).name("app1")
        .certificate(getCertificate()).enabled(true).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentialsService.delete(subjectCredentials);
    assertEquals(0,
        size(subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.CERTIFICATE)));

    OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
    assertThat(keyStore.aliasExists(subjectCredentials.getName()), is(false));
  }

  @Test
  public void test_delete_user_with_groups() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create()
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD).name("user1").password("password")
        .groups(Sets.newHashSet("group1", "group2")).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentialsService.delete(subjectCredentials);

    assertEquals(0,
        size(subjectCredentialsService.getSubjectCredentials(SubjectCredentials.AuthenticationType.PASSWORD)));

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertEquals(0, group1.getSubjectCredentials().size());

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertEquals(0, group2.getSubjectCredentials().size());
  }

  @Test
  public void test_delete_group() {
    Group group = Group.Builder.create().name("group1").build();
    subjectCredentialsService.createGroup(group.getName());
    subjectCredentialsService.delete(group);
    assertEquals(0, size(subjectCredentialsService.getGroups()));
  }

  @Test
  public void test_delete_group_with_subjects() {
    // TODO
  }

  private void assertSubjectEquals(SubjectCredentials expected, SubjectCredentials found) {
    assertThat(found, notNullValue());
    assertThat(found, is(expected));
    assertThat(found.getName(), is(expected.getName()));
    assertThat(found.getAuthenticationType(), is(expected.getAuthenticationType()));
    assertThat(found.getPassword(), is(expected.getPassword()));
    assertThat(found.isEnabled(), is(expected.isEnabled()));

    assertTrue(Iterables.elementsEqual(expected.getGroups(), found.getGroups()));
    Asserts.assertCreatedTimestamps(expected, found);
  }

  private void assertGroupEquals(Group expected, Group found) {
    assertThat(found, notNullValue());
    assertThat(found, is(expected));
    assertThat(found.getName(), is(expected.getName()));
    assertTrue(Iterables.elementsEqual(expected.getSubjectCredentials(), found.getSubjectCredentials()));
    Asserts.assertCreatedTimestamps(expected, found);
  }

  @Configuration
  @PropertySource("classpath:org/obiba/opal/core/service/security/SubjectCredentialsServiceImplTest.properties")
  public static class Config extends AbstractOrientDbTestConfig {

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
      expect(subjectProfileService.getProfile("user1")).andReturn(null).anyTimes();
      expect(subjectProfileService.getProfile("app1")).andReturn(null).anyTimes();
      subjectProfileService.ensureProfile("user1", OpalUserRealm.OPAL_REALM);
      expectLastCall().anyTimes();
      subjectProfileService.ensureProfile("app1", ApplicationRealm.APPLICATION_REALM);
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
