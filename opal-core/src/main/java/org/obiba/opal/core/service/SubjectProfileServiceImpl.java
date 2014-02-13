/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.security.realm.BackgroundJobRealm;
import org.obiba.opal.core.service.security.realm.SudoRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubjectProfileServiceImpl implements SubjectProfileService {

  private static final Logger log = LoggerFactory.getLogger(SubjectProfileServiceImpl.class);

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(SubjectProfile.class);
  }

  @Override
  public void stop() {}

  @Override
  public boolean supportProfile(@Nullable Object principal) {
    return principal != null && //
        !principal.equals(BackgroundJobRealm.SystemPrincipal.INSTANCE) && //
        !principal.equals(SudoRealm.SudoPrincipal.INSTANCE);
  }

  @Override
  public void ensureProfile(@NotNull String principal, @NotNull String realm) {
    log.debug("ensure profile of user {} from realm: {}", principal, realm);

    try {
      SubjectProfile profile = getProfile(principal);
      if(!profile.getRealm().equals(realm)) {
        throw new AuthenticationException(
            "Wrong realm for subject '" + principal + "': " + realm + " (" + profile.getRealm() +
                " expected). Make sure the same subject is not defined in several realms.");
      }
    } catch(SubjectProfileNotFoundException e) {
      HasUniqueProperties newProfile = new SubjectProfile(principal, realm);
      orientDbService.save(newProfile, newProfile);
    }
  }

  @Override
  public void ensureProfile(@NotNull PrincipalCollection principalCollection) {
    String principal = principalCollection.getPrimaryPrincipal().toString();
    String realm = principalCollection.getRealmNames().iterator().next();
    ensureProfile(principal, realm);
  }

  @Override
  public void deleteProfile(@NotNull String principal) {
    try {
      orientDbService.delete(getProfile(principal));
    } catch(SubjectProfileNotFoundException ignored) {
      // ignore
    }
  }

  @NotNull
  @Override
  public SubjectProfile getProfile(@Nullable String principal) throws SubjectProfileNotFoundException {
    if (principal == null) throw new SubjectProfileNotFoundException("");
    SubjectProfile subjectProfile = orientDbService.findUnique(SubjectProfile.Builder.create(principal).build());
    if(subjectProfile == null) {
      throw new SubjectProfileNotFoundException(principal);
    }
    return subjectProfile;
  }

  @Override
  public void updateProfile(@NotNull String principal) throws SubjectProfileNotFoundException {
    SubjectProfile profile = getProfile(principal);
    profile.setUpdated(new Date());
    orientDbService.save(profile, profile);
  }

  @Override
  public Iterable<SubjectProfile> getProfiles() {
    return orientDbService.list(SubjectProfile.class);
  }

  @Override
  public void addBookmarks(String principal, List<String> resources) throws SubjectProfileNotFoundException {
    SubjectProfile profile = getProfile(principal);
    for(String resource : resources) {
      profile.addBookmark(resource);
    }
    orientDbService.save(profile, profile);
  }

  @Override
  public void deleteBookmark(String principal, String path) throws SubjectProfileNotFoundException {
    SubjectProfile profile = getProfile(principal);
    if(profile.removeBookmark(path)) {
      orientDbService.save(profile, profile);
    }
  }
}
