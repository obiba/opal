/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.impl;

import javax.annotation.PostConstruct;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectProfileService;
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
  public void ensureProfile(Subject subject) {
    String principal = subject.getPrincipal().toString();
    String realm = subject.getPrincipals().getRealmNames().iterator().next();
    log.info("ensure profile of user {} from realm: {}", principal, realm);

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
  public void deleteProfile(String name) {

  }

  public SubjectProfile getProfile(String principal) {
    SubjectProfile profile = orientDbService.findUnique(SubjectProfile.Builder.create(principal).build());
    return profile;
  }
}
