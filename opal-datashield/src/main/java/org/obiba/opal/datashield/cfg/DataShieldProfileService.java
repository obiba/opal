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
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.datashield.CustomRScriptMethod;
import org.obiba.opal.datashield.RFunctionDataShieldMethod;
import org.obiba.opal.r.cluster.RServerCluster;
import org.obiba.opal.r.service.RServerManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

  @Value("${datashield.r.parser}")
  private String defaultRParserVersion;

  private final RServerManagerService rServerManagerService;

  private final OrientDbService orientDbService;

  private final SubjectAclService subjectAclService;

  private final Lock lock = new ReentrantLock();

  private final DatashieldConfigurationSupplier datashieldConfigurationSupplier;

  @Autowired
  public DataShieldProfileService(RServerManagerService rServerManagerService, OrientDbService orientDbService, SubjectAclService subjectAclService, DatashieldConfigurationSupplier datashieldConfigurationSupplier) {
    this.rServerManagerService = rServerManagerService;
    this.orientDbService = orientDbService;
    this.subjectAclService = subjectAclService;
    this.datashieldConfigurationSupplier = datashieldConfigurationSupplier;
  }

  public String getRParserVersionOrDefault(DataShieldProfile profile) {
    if (profile.hasRParserVersion()) return profile.getRParserVersion();
    return Strings.isNullOrEmpty(defaultRParserVersion) ? "v1" : defaultRParserVersion;
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
  public void start() {
    orientDbService.createUniqueIndex(DataShieldProfile.class);
    upgradeDefaultDataShieldConfig();
  }

  @Override
  public void stop() {

  }

  //
  // Private methods
  //

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

  private void upgradeDefaultDataShieldConfig() {
    if (orientDbService.count(DataShieldProfile.class) == 0) {
      log.info("Upgrading DataSHIELD configuration...");
      try {
        DatashieldConfiguration datashieldConfiguration = datashieldConfigurationSupplier.get();
        DataShieldProfile profile = new DataShieldProfile(rServerManagerService.getDefaultClusterName());
        for (DSMethodType type : DSMethodType.values())
          upgradeDSEnvironment(datashieldConfiguration.getEnvironment(type), profile.getEnvironment(type));
        datashieldConfiguration.getOptions().forEach(opt -> profile.addOrUpdateOption(opt.getName(), opt.getValue()));
        profile.setEnabled(true);
        saveProfile(profile);
        datashieldConfigurationSupplier.modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<DatashieldConfiguration>() {
          @Override
          public void doWithConfig(DatashieldConfiguration config) {
            // empty legacy config
            final DSEnvironment aggs = config.getEnvironment(DSMethodType.AGGREGATE);
            Lists.newArrayList(aggs.getMethods()).forEach(m -> aggs.removeMethod(m.getName()));
            final DSEnvironment asss = config.getEnvironment(DSMethodType.ASSIGN);
            Lists.newArrayList(asss.getMethods()).forEach(m -> asss.removeMethod(m.getName()));
            config.getOptions().forEach(opt -> config.removeOption(opt.getName()));
          }
        });
      } catch (Exception e) {
        log.warn("DataSHIELD configuration upgrade failed", e);
      }
    }
  }

  private void upgradeDSEnvironment(DSEnvironment oldEnv, DSEnvironment newEnv) {
    if (oldEnv != null) {
      oldEnv.getMethods().stream()
          .map(this::upgradeDSMethod)
          .forEach(newEnv::addOrUpdate);
    }
  }

  private DSMethod upgradeDSMethod(DSMethod oldMethod) {
    if (oldMethod instanceof RFunctionDataShieldMethod) {
      RFunctionDataShieldMethod m = (RFunctionDataShieldMethod)oldMethod;
      return new DefaultDSMethod(m.getName(), m.getFunction(), m.getPackage(), m.getVersion());
    }
    if (oldMethod instanceof CustomRScriptMethod) {
      CustomRScriptMethod m = (CustomRScriptMethod)oldMethod;
      return new DefaultDSMethod(m.getName(), m.getScript());
    }
    // not suposed to be here
    return new DefaultDSMethod(oldMethod.getName(), oldMethod.getName());
  }
}
