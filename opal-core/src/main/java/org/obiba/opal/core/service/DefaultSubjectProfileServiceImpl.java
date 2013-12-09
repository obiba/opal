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

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultSubjectProfileServiceImpl implements SubjectProfileService {

  private static final Logger log = LoggerFactory.getLogger(DefaultSubjectProfileServiceImpl.class);

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(SubjectProfile.class);
  }

  @Override
  public void stop() {

  }

  @Override
  public void ensureProfile(@NotNull String principal, @NotNull String realm) {
    log.debug("ensure profile of user {} from realm: {}", principal, realm);

    SubjectProfile profile = getProfile(principal);
    if(profile == null) {
      SubjectProfile p = new SubjectProfile(principal, realm);
      orientDbService.save(p, p);
    } else if(!profile.getRealm().equals(realm)) {
      throw new AuthenticationException(
          "Wrong realm for subject '" + principal + "': " + realm + " (" + profile.getRealm() +
              " expected). Make sure the same subject is not defined in several realms.");
    }
  }

  @Override
  public void ensureProfile(@NotNull Subject subject) {
    String principal = subject.getPrincipal().toString();
    String realm = subject.getPrincipals().getRealmNames().iterator().next();
    ensureProfile(principal, realm);
  }

  @Override
  public void deleteProfile(@NotNull String principal) {
    SubjectProfile profile = getProfile(principal);
    if(profile != null) {
      orientDbService.delete(profile);
    }
  }

  @Nullable
  @Override
  public SubjectProfile getProfile(@NotNull String principal) {
    return orientDbService.findUnique(SubjectProfile.Builder.create(principal).build());
  }
}
