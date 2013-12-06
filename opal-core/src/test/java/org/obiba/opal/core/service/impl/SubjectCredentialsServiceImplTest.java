package org.obiba.opal.core.service.impl;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.SubjectCredentials;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.SubjectCredentialsService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = SubjectCredentialsServiceImplTest.Config.class)
public class SubjectCredentialsServiceImplTest extends AbstractJUnit4SpringContextTests {

//  private static final Logger log = LoggerFactory.getLogger(SubjectCredentialsServiceImplTest.class);

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @Autowired
  private OrientDbService orientDbService;

  @Before
  public void clear() throws Exception {
    orientDbService.deleteAll(SubjectCredentials.class);
    orientDbService.deleteAll(Group.class);
  }

  @Test
  public void test_create_new_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create().name("user1").password("password")
        .enabled(true).build();
    subjectCredentialsService.save(subjectCredentials);

    List<SubjectCredentials> subjectCredentialses = newArrayList(subjectCredentialsService.getSubjectCredentials());
    assertEquals(1, subjectCredentialses.size());
    assertUserEquals(subjectCredentials, subjectCredentialses.get(0));

    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    assertUserEquals(subjectCredentials, found);
  }

  @Test
  public void test_update_user() {

    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create().name("user1").password("password")
        .enabled(true).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentials.setPassword("new password");
    subjectCredentialsService.save(subjectCredentials);

    List<SubjectCredentials> subjectCredentialses = newArrayList(subjectCredentialsService.getSubjectCredentials());
    assertEquals(1, subjectCredentialses.size());
    assertUserEquals(subjectCredentials, subjectCredentialses.get(0));

    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    assertUserEquals(subjectCredentials, found);
    Asserts.assertUpdatedTimestamps(subjectCredentials, found);
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

    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create().name("user1").password("password")
        .groups(Sets.newHashSet("group1", "group2")).build();
    subjectCredentialsService.save(subjectCredentials);

    assertEquals(2, size(subjectCredentialsService.getGroups()));

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertNotNull(group1);
    assertEquals(1, group1.getUsers().size());
    assertTrue(group1.getUsers().contains(subjectCredentials.getName()));

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertNotNull(group2);
    assertEquals(1, group2.getUsers().size());
    assertTrue(group2.getUsers().contains(subjectCredentials.getName()));
  }

  @Test
  public void test_remove_groups_from_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create().name("user1").password("password")
        .groups(Sets.newHashSet("group1", "group2")).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentials.removeGroup("group1");
    subjectCredentialsService.save(subjectCredentials);

    SubjectCredentials found = subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName());
    assertUserEquals(subjectCredentials, found);

    assertEquals(2, size(subjectCredentialsService.getGroups()));

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertNotNull(group1);
    assertEquals(0, group1.getUsers().size());

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertNotNull(group2);
    assertEquals(1, group2.getUsers().size());
    assertTrue(group2.getUsers().contains(subjectCredentials.getName()));
  }

  @Test
  public void test_delete_user() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create().name("user1").password("password")
        .build();
    subjectCredentialsService.save(subjectCredentials);
    subjectCredentialsService.delete(subjectCredentials);
    assertEquals(0, size(subjectCredentialsService.getSubjectCredentials()));
  }

  @Test
  public void test_delete_user_with_groups() {
    SubjectCredentials subjectCredentials = SubjectCredentials.Builder.create().name("user1").password("password")
        .groups(Sets.newHashSet("group1", "group2")).build();
    subjectCredentialsService.save(subjectCredentials);

    subjectCredentialsService.delete(subjectCredentials);

    assertEquals(0, size(subjectCredentialsService.getSubjectCredentials()));

    Group group1 = subjectCredentialsService.getGroup("group1");
    assertEquals(0, group1.getUsers().size());

    Group group2 = subjectCredentialsService.getGroup("group2");
    assertEquals(0, group2.getUsers().size());
  }

  @Test
  public void test_delete_group() {
    Group group = Group.Builder.create().name("group1").build();
    subjectCredentialsService.createGroup(group.getName());
    subjectCredentialsService.delete(group);
    assertEquals(0, size(subjectCredentialsService.getGroups()));
  }

  @Test
  public void test_delete_group_with_users() {
    // TODO
  }

  private void assertUserEquals(SubjectCredentials expected, SubjectCredentials found) {
    assertNotNull(found);
    assertEquals(expected, found);
    assertEquals(expected.getName(), found.getName());
    assertEquals(expected.getPassword(), found.getPassword());
    assertEquals(expected.isEnabled(), found.isEnabled());
    assertTrue(Iterables.elementsEqual(expected.getGroups(), found.getGroups()));
    Asserts.assertCreatedTimestamps(expected, found);
  }

  private void assertGroupEquals(Group expected, Group found) {
    assertNotNull(found);
    assertEquals(expected, found);
    assertEquals(expected.getName(), found.getName());
    assertTrue(Iterables.elementsEqual(expected.getUsers(), found.getUsers()));
    Asserts.assertCreatedTimestamps(expected, found);
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    SubjectProfileService subjectProfileService;

    @Bean
    public SubjectCredentialsService userService() {
      return new SubjectCredentialsServiceImpl();
    }

    @Bean
    public SubjectAclService subjectAclService() {
      return EasyMock.createMock(SubjectAclService.class);
    }

    @Bean
    public SubjectProfileService subjectProfileService() {
      if(subjectProfileService == null) {
        subjectProfileService = EasyMock.createMock(SubjectProfileService.class);
        expect(subjectProfileService.getProfile("user1")) //
            .andReturn(null) //
            .anyTimes();
        subjectProfileService.ensureProfile("user1", "opal-realm");
        subjectProfileService.ensureProfile("user1", "opal-realm");
        subjectProfileService.ensureProfile("user1", "opal-realm");
        subjectProfileService.ensureProfile("user1", "opal-realm");
        subjectProfileService.ensureProfile("user1", "opal-realm");
        subjectProfileService.ensureProfile("user1", "opal-realm");
        subjectProfileService.deleteProfile("user1");
        subjectProfileService.deleteProfile("user1");
        replay(subjectProfileService);
      }
      return subjectProfileService;
    }

  }

}
