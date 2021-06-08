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
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.r.service.RServerManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DatashieldProfileService implements SystemService {

  private static final Logger log = LoggerFactory.getLogger(DatashieldProfileService.class);

  private final OrientDbService orientDbService;

  private final Lock lock = new ReentrantLock();

  public DatashieldProfileService(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  /**
   * List all saved profiles.
   *
   * @return
   */
  public Iterable<DatashieldProfile> getProfiles() {
    return orientDbService.list(DatashieldProfile.class);
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
