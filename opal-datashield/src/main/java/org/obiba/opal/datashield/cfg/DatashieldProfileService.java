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
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class DatashieldProfileService implements SystemService {

  private static final Logger log = LoggerFactory.getLogger(DatashieldProfileService.class);

  private final RServerManagerService rServerManagerService;

  private final OrientDbService orientDbService;

  private final SubjectAclService subjectAclService;

  private final Lock lock = new ReentrantLock();

  @Autowired
  public DatashieldProfileService(RServerManagerService rServerManagerService, OrientDbService orientDbService, SubjectAclService subjectAclService) {
    this.rServerManagerService = rServerManagerService;
    this.orientDbService = orientDbService;
    this.subjectAclService = subjectAclService;
  }

  /**
   * List all saved profiles.
   *
   * @return
   */
  public List<DatashieldProfile> getProfiles() {
    List<DatashieldProfile> profiles = Lists.newArrayList(orientDbService.list(DatashieldProfile.class));
    Set<String> primaryProfileNames = profiles.stream()
        .filter(p -> p.getName().equals(p.getCluster()))
        .map(DatashieldProfile::getName)
        .collect(Collectors.toSet());
    rServerManagerService.getRServerClusters().stream()
        .map(RServerCluster::getName)
        .filter(c -> !primaryProfileNames.contains(c))
        .forEach(c -> {
          DatashieldProfile p = new DatashieldProfile(c);
          saveProfile(p);
          profiles.add(p);
        });

    profiles.sort(Comparator.comparing(DatashieldProfile::getName));

    return profiles;
  }

  /**
   * Get the profile for the given name.
   *
   * @param name
   * @return
   */
  public DatashieldProfile getProfile(String name) {
    lock.lock();
    try {
      String p = Strings.isNullOrEmpty(name) ? RServerManagerService.DEFAULT_CLUSTER_NAME : name;
      DatashieldProfile profile = findProfile(p);
      if (profile == null) {
        profile = new DatashieldProfile(p);
        profile.setEnabled(false);
      }
      return profile;
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
  public DatashieldProfile findProfile(String name) {
    String p = Strings.isNullOrEmpty(name) ? RServerManagerService.DEFAULT_CLUSTER_NAME : name;
    return orientDbService.findUnique(new DatashieldProfile(p));
  }

  /**
   * Save (create or update) profile.
   *
   * @param profile
   */
  public void saveProfile(DatashieldProfile profile) {
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
  public void deleteProfile(DatashieldProfile profile) {
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
    orientDbService.createUniqueIndex(DatashieldProfile.class);
  }

  @Override
  @PreDestroy
  public void stop() {

  }
}
