/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_2_x;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.security.CredentialsKeyStoreService;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@SuppressWarnings({ "SpringJavaAutowiringInspection", "MethodOnlyUsedFromInnerClass" })
public class SubjectCredentialsCertificateAliasUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(SubjectCredentialsCertificateAliasUpgradeStep.class);

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @Override
  public void execute(Version currentVersion) {
    OpalKeyStore keyStore = credentialsKeyStoreService.getKeyStore();
    Set<String> aliases = keyStore.listAliases();
    // alias are sorted in reverse order to have latest aliases when there are duplicates
    List<String> sortedAliases = Lists.newArrayList(aliases);
    Collections.sort(sortedAliases, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.compareTo(o2) * -1;
      }
    });

    for(SubjectCredentials subjectCredentials : subjectCredentialsService
        .getSubjectCredentials(SubjectCredentials.AuthenticationType.CERTIFICATE)) {
      if(Strings.isNullOrEmpty(subjectCredentials.getCertificateAlias()) &&
          !updateCertificateAlias(subjectCredentials, sortedAliases)) {
        log.warn("Cannot restore certificate alias of subject {}", subjectCredentials.getName());
      }
    }
  }

  private boolean updateCertificateAlias(SubjectCredentials subjectCredentials, List<String> aliases) {
    boolean found = false;
    String aliasPrefix = subjectCredentials.getName().toLowerCase().replaceAll("[^A-Za-z0-9]", "") + "-";
    for(String alias : aliases) {
      if(alias.startsWith(aliasPrefix)) {
        if (found) {
          // delete duplicate
          credentialsKeyStoreService.deleteKeyStore(alias);
        } else {
          // update subject with latest alias
          subjectCredentials.setCertificateAlias(alias);
          subjectCredentialsService.save(subjectCredentials);
          found = true;
        }
      }
    }
    return false;
  }

}
