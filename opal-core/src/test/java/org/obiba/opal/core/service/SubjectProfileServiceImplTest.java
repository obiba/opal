/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.core.domain.security.Bookmark;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.service.security.TotpService;
import org.obiba.opal.core.service.security.realm.OpalUserRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.Properties;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

@ContextConfiguration(classes = SubjectProfileServiceImplTest.Config.class)
public class SubjectProfileServiceImplTest extends AbstractOrientdbServiceTest {

  private static final String PRINCIPAL = "principal";

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  public void startDB() throws Exception {
    super.startDB();
    orientDbService.deleteAll(SubjectProfile.class);
  }

  @Test
  public void test_ensure_and_get_profile_with_principal_and_realm() {

    subjectProfileService.ensureProfile(PRINCIPAL, OpalUserRealm.OPAL_REALM);

    SubjectProfile profile = subjectProfileService.getProfile(PRINCIPAL);
    assertThat(profile).isNotNull();
    assertThat(profile.getPrincipal()).isEqualTo(PRINCIPAL);
    assertThat(profile.getRealm()).isEqualTo(OpalUserRealm.OPAL_REALM);

    Iterable<SubjectProfile> profiles = subjectProfileService.getProfiles();
    assertThat(profiles).isNotNull();
    assertThat(profiles).hasSize(1);
  }

  @Test
  public void test_ensure_profile_with_principal_collection() throws Exception {
    subjectProfileService.ensureProfile(new SimplePrincipalCollection(PRINCIPAL, OpalUserRealm.OPAL_REALM));

    SubjectProfile profile = subjectProfileService.getProfile(PRINCIPAL);
    assertThat(profile).isNotNull();
    assertThat(profile.getPrincipal()).isEqualTo(PRINCIPAL);
    assertThat(profile.getRealm()).isEqualTo(OpalUserRealm.OPAL_REALM);

    Iterable<SubjectProfile> profiles = subjectProfileService.getProfiles();
    assertThat(profiles).isNotNull();
    assertThat(profiles).hasSize(1);
  }

  @Test
  public void test_delete_profile() throws Exception {
    subjectProfileService.ensureProfile(PRINCIPAL, OpalUserRealm.OPAL_REALM);
    subjectProfileService.deleteProfile(PRINCIPAL);

    try {
      subjectProfileService.getProfile(PRINCIPAL);
      Assert.fail("Should throw SubjectProfileNotFoundException");
    } catch(NoSuchSubjectProfileException ignored) {
    }

    assertThat(subjectProfileService.getProfiles()).isEmpty();
  }

  @Test
  public void test_delete_null_profile() {
    subjectProfileService.deleteProfile("no profile");
  }

  @Test(expected = NoSuchSubjectProfileException.class)
  public void test_get_null_profile() throws Exception {
    subjectProfileService.getProfile("no profile");
  }

  @Test
  public void test_update_profile() throws Exception {
    subjectProfileService.ensureProfile(PRINCIPAL, OpalUserRealm.OPAL_REALM);

    SubjectProfile profile = subjectProfileService.getProfile(PRINCIPAL);

    Thread.sleep(1000);

    subjectProfileService.updateProfile(PRINCIPAL);
    SubjectProfile profileUpdated = subjectProfileService.getProfile(PRINCIPAL);

    assertThat(profile).isEqualTo(profileUpdated);
    assertThat(profile.getCreated()).isEqualTo(profileUpdated.getCreated());
    assertThat(profile.getUpdated()).isBefore(profileUpdated.getUpdated());
  }

  @Test
  public void test_add_bookmarks() throws Exception {
    subjectProfileService.ensureProfile(PRINCIPAL, OpalUserRealm.OPAL_REALM);
    subjectProfileService.addBookmarks(PRINCIPAL, Lists.newArrayList("bookmark 1", "bookmark 2"));

    SubjectProfile profile = subjectProfileService.getProfile(PRINCIPAL);
    Set<Bookmark> bookmarks = profile.getBookmarks();
    assertThat(bookmarks).hasSize(2);
    assertThat(profile.hasBookmark("bookmark 1")).isTrue();
    assertThat(profile.hasBookmark("bookmark 2")).isTrue();
  }

  @Test
  public void test_delete_bookmark() throws Exception {
    subjectProfileService.ensureProfile(PRINCIPAL, OpalUserRealm.OPAL_REALM);
    subjectProfileService.addBookmarks(PRINCIPAL, Lists.newArrayList("bookmark 1", "bookmark 2"));
    subjectProfileService.deleteBookmark(PRINCIPAL, "bookmark 2");

    SubjectProfile profile = subjectProfileService.getProfile(PRINCIPAL);
    Set<Bookmark> bookmarks = profile.getBookmarks();
    assertThat(bookmarks).hasSize(1);
    assertThat(profile.hasBookmark("bookmark 1")).isTrue();
    assertThat(profile.hasBookmark("bookmark 2")).isFalse();
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public EventBus eventBus() {
      return new EventBus();
    }

    @Bean
    public TotpService totpService() {
      return new TotpService() {
        @Override
        public String generateSecret() {
          return "ABCD";
        }

        @Override
        public String getQrImageDataUri(String label, String secret) {
          return null;
        }

        @Override
        public boolean validateCode(String code, String secret) {
          return false;
        }
      };
    }

    @Bean
    public SubjectProfileService subjectProfileService() {
      return new SubjectProfileServiceImpl();
    }

    @Bean
    public SubjectAclService subjectAclService() {
      SubjectAclService mock = EasyMock.createMock(SubjectAclService.class);
      mock.afterPropertiesSet();
      SubjectAclService.Permissions permsMock = EasyMock.createMock(SubjectAclService.Permissions.class);
      EasyMock.expect(permsMock.getPermissions()).andReturn(Sets.newHashSet(SubjectProfileServiceImpl.FILES_SHARE_PERM))
          .anyTimes();
      EasyMock.expect(mock.getSubjectNodePermissions("opal", "/files/home/principal", new SubjectAcl.Subject(PRINCIPAL, SubjectAcl.SubjectType.USER)))
          .andReturn(permsMock).anyTimes();
      EasyMock.expect(mock.getSubjectNodePermissions("opal", "/files/tmp", new SubjectAcl.Subject(PRINCIPAL, SubjectAcl.SubjectType.USER)))
          .andReturn(permsMock).anyTimes();
      EasyMock.replay(mock, permsMock);
      return mock;
    }

    @Bean
    public OpalRuntime opalRuntime() {
      OpalRuntime mock = EasyMock.createMock(OpalRuntime.class);
      mock.afterPropertiesSet();
      EasyMock.replay(mock);
      return mock;
    }

    @Bean
    public OpalFileSystemService opalFileSystemService() {
      OpalFileSystemService mock = EasyMock.createMock(OpalFileSystemService.class);
      mock.afterPropertiesSet();
      EasyMock.expect(mock.hasFileSystem()).andReturn(false).anyTimes();
      EasyMock.replay(mock);
      return mock;
    }

    @Override
    protected void appendProperties(Properties properties) {
      properties.setProperty("org.obiba.opal.security.multiProfile", "true");
    }
  }
}
