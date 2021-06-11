/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.datashield.cfg;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.r.cluster.RServerCluster;
import org.obiba.opal.r.service.RServerManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class DataShieldProfileService implements SystemService {

  private static final Logger log = LoggerFactory.getLogger(DataShieldProfileService.class);

  private final RServerManagerService rServerManagerService;

  private final OrientDbService orientDbService;

  private final SubjectAclService subjectAclService;

  private final Lock lock = new ReentrantLock();

  @Autowired
  public DataShieldProfileService(RServerManagerService rServerManagerService, OrientDbService orientDbService, SubjectAclService subjectAclService) {
    this.rServerManagerService = rServerManagerService;
    this.orientDbService = orientDbService;
    this.subjectAclService = subjectAclService;
  }

  /**
   * List all saved profiles, make sure there is a profile for each cluster and disable profiles which cluster is missing.
   *
   * @return
   */
  public List<DataShieldProfile> getProfiles() {
    lock.lock();
    try {
      List<DataShieldProfile> profiles = Lists.newArrayList(orientDbService.list(DataShieldProfile.class));
      List<String> clusterNames = getClusterNames();
      Set<String> primaryProfileNames = profiles.stream()
          .filter(p -> p.getName().equals(p.getCluster()))
          .map(DataShieldProfile::getName)
          .collect(Collectors.toSet());
      // disable profiles which cluster does not exist
      profiles.stream().filter(p -> !clusterNames.contains(p.getCluster())).forEach(p -> {
        p.setEnabled(false);
        saveProfile(p);
      });
      // add missing profiles (disabled)
      clusterNames.stream()
          .filter(c -> !primaryProfileNames.contains(c))
          .forEach(c -> {
            DataShieldProfile p = new DataShieldProfile(c);
            saveProfile(p);
            profiles.add(p);
          });

      profiles.sort(Comparator.comparing(DataShieldProfile::getName));

      return profiles;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Get the profile for the given name. If there is no such saved profile,
   * returns a dummy profile (disabled).
   *
   * @param name
   * @return
   */
  public DataShieldProfile getProfile(String name) {
    lock.lock();
    try {
      String pName = Strings.isNullOrEmpty(name) ? rServerManagerService.getDefaultRServerProfile().getName() : name;
      Optional<DataShieldProfile> profileOpt = getProfiles().stream().filter(p -> p.getName().equals(pName)).findFirst();
      if (!profileOpt.isPresent()) {
        DataShieldProfile profile = new DataShieldProfile(pName);
        profile.setEnabled(false);
        return profile;
      }
      return profileOpt.get();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Check whether a profile exists with provided name.
   *
   * @param name
   * @return
   */
  public boolean hasProfile(String name) {
    return findProfile(name) != null;
  }

  /**
   * Find profile by name.
   *
   * @param name
   * @return null if none is found
   */
  public DataShieldProfile findProfile(String name) {
    String p = Strings.isNullOrEmpty(name) ? rServerManagerService.getDefaultClusterName() : name;
    return orientDbService.findUnique(new DataShieldProfile(p));
  }

  /**
   * Save (create or update) profile.
   *
   * @param profile
   */
  public void saveProfile(DataShieldProfile profile) {
    lock.lock();
    try {
      orientDbService.save(profile, profile);
      if (!profile.isRestrictedAccess())
        subjectAclService.deleteNodePermissions("opal", "/datashield/profile/" + profile.getName());
    } finally {
      lock.unlock();
    }
  }

  /**
   * Delete profile.
   *
   * @param profile
   */
  public void deleteProfile(DataShieldProfile profile) {
    lock.lock();
    try {
      orientDbService.delete(profile);
      if (profile.isRestrictedAccess())
        subjectAclService.deleteNodePermissions("opal", "/datashield/profile/" + profile.getName());
    } finally {
      lock.unlock();
    }
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(DataShieldProfile.class);
  }

  @Override
  @PreDestroy
  public void stop() {

  }

  /**
   * Get R server cluster names.
   *
   * @return
   */
  private List<String> getClusterNames() {
    return rServerManagerService.getRServerClusters().stream()
        .map(RServerCluster::getName)
        .collect(Collectors.toList());
  }
}
