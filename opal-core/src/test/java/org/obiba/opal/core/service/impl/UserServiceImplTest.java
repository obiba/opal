package org.obiba.opal.core.service.impl;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.UserService;
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

@ContextConfiguration(classes = UserServiceImplTest.Config.class)
public class UserServiceImplTest extends AbstractJUnit4SpringContextTests {

//  private static final Logger log = LoggerFactory.getLogger(UserServiceImplTest.class);

  @Autowired
  private UserService userService;

  @Autowired
  private OrientDbService orientDbService;

  @Before
  public void clear() throws Exception {
    orientDbService.deleteAll(User.class);
    orientDbService.deleteAll(Group.class);
  }

  @Test
  public void test_create_new_user() {
    User user = User.Builder.create().name("user1").password("password").enabled(true).build();
    userService.save(user);

    List<User> users = newArrayList(userService.getUsers());
    assertEquals(1, users.size());
    assertUserEquals(user, users.get(0));

    User found = userService.getUser(user.getName());
    assertUserEquals(user, found);
  }

  @Test
  public void test_update_user() {

    User user = User.Builder.create().name("user1").password("password").enabled(true).build();
    userService.save(user);

    user.setPassword("new password");
    userService.save(user);

    List<User> users = newArrayList(userService.getUsers());
    assertEquals(1, users.size());
    assertUserEquals(user, users.get(0));

    User found = userService.getUser(user.getName());
    assertUserEquals(user, found);
    Asserts.assertUpdatedTimestamps(user, found);
  }

  @Test
  public void test_create_new_group() {
    Group group = Group.Builder.create().name("group1").build();
    userService.createGroup(group.getName());

    List<Group> groups = newArrayList(userService.getGroups());
    assertEquals(1, groups.size());
    assertGroupEquals(group, groups.get(0));

    Group found = userService.getGroup(group.getName());
    assertGroupEquals(group, found);
  }

  @Test
  public void test_create_groups_from_user() {

    User user = User.Builder.create().name("user1").password("password").groups(Sets.newHashSet("group1", "group2"))
        .build();
    userService.save(user);

    assertEquals(2, size(userService.getGroups()));

    Group group1 = userService.getGroup("group1");
    assertNotNull(group1);
    assertEquals(1, group1.getUsers().size());
    assertTrue(group1.getUsers().contains(user.getName()));

    Group group2 = userService.getGroup("group2");
    assertNotNull(group2);
    assertEquals(1, group2.getUsers().size());
    assertTrue(group2.getUsers().contains(user.getName()));
  }

  @Test
  public void test_remove_groups_from_user() {
    User user = User.Builder.create().name("user1").password("password").groups(Sets.newHashSet("group1", "group2"))
        .build();
    userService.save(user);

    user.removeGroup("group1");
    userService.save(user);

    User found = userService.getUser(user.getName());
    assertUserEquals(user, found);

    assertEquals(2, size(userService.getGroups()));

    Group group1 = userService.getGroup("group1");
    assertNotNull(group1);
    assertEquals(0, group1.getUsers().size());

    Group group2 = userService.getGroup("group2");
    assertNotNull(group2);
    assertEquals(1, group2.getUsers().size());
    assertTrue(group2.getUsers().contains(user.getName()));
  }

  @Test
  public void test_delete_user() {
    User user = User.Builder.create().name("user1").password("password").build();
    userService.save(user);
    userService.delete(user);
    assertEquals(0, size(userService.getUsers()));
  }

  @Test
  public void test_delete_user_with_groups() {
    User user = User.Builder.create().name("user1").password("password").groups(Sets.newHashSet("group1", "group2"))
        .build();
    userService.save(user);

    userService.delete(user);

    assertEquals(0, size(userService.getUsers()));

    Group group1 = userService.getGroup("group1");
    assertEquals(0, group1.getUsers().size());

    Group group2 = userService.getGroup("group2");
    assertEquals(0, group2.getUsers().size());
  }

  @Test
  public void test_delete_group() {
    Group group = Group.Builder.create().name("group1").build();
    userService.createGroup(group.getName());
    userService.delete(group);
    assertEquals(0, size(userService.getGroups()));
  }

  @Test
  public void test_delete_group_with_users() {
    // TODO
  }

  private void assertUserEquals(User expected, User found) {
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
    public UserService userService() {
      return new UserServiceImpl();
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
