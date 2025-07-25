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
import com.google.common.eventbus.Subscribe;
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.datashield.CustomRScriptMethod;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.RFunctionDataShieldMethod;
import org.obiba.opal.r.service.RServerClusterService;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.event.RServerServiceStartedEvent;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.NoSuchRPackageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
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
      if (profileOpt.isEmpty()) {
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

  public boolean hasEmptyProfile(String name) {
    DataShieldProfile profile = findProfile(name);
    if (profile == null) return true;
    return Lists.newArrayList(profile.getOptions()).isEmpty()
        && profile.getEnvironment(DSMethodType.AGGREGATE).getMethods().isEmpty()
        && profile.getEnvironment(DSMethodType.ASSIGN).getMethods().isEmpty();
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
  
  @Subscribe
  public synchronized void onRServiceStarted(RServerServiceStartedEvent event) {
    try {
      rServerManagerService.getRServerClusters().forEach(cluster -> {
        log.debug("Checking R cluster Datashield profile {}: running={} hasEmptyProfile={}", cluster.getName(), cluster.isRunning(), hasEmptyProfile(cluster.getName()));
        if (cluster.isRunning() && hasEmptyProfile(cluster.getName())) {
          log.info("Initializing datashield profile {}", cluster.getName());
          boolean hasProfile = hasProfile(cluster.getName());
          DataShieldProfile profile = hasProfile ? findProfile(cluster.getName()) : new DataShieldProfile(cluster.getName());
          cluster.getDataShieldPackagesProperties().keySet().stream().distinct().forEach(name -> publish(profile, name));
          // do not change enabled if it is not a new profile
          if (!hasProfile) profile.setEnabled(true);
          saveProfile(profile);
        }
      });
    } catch (Exception e) {
      log.error("Cannot read R/DataSHIELD packages to initialize profile", e);
    }
  }

  //
  // Publication
  //

  /**
   * Get the package settings and push them to the profile's config.
   *
   * @param profile R server profile
   * @param name    Package name
   */
  public void publish(RServerProfile profile, String name) {
    List<OpalR.RPackageDto> packages = getDatashieldPackage(profile, name);
    publish(profile, packages);
  }

  public void publish(RServerProfile profile, List<OpalR.RPackageDto> packages) {
    DatashieldPackagesHandler handler = new DatashieldPackagesHandler(packages);
    DataShieldProfile config = (DataShieldProfile) profile;
    handler.publish(config);
    saveProfile(config);
    String name = packages.getFirst().getName();
    DataShieldLog.adminLog("Package '{}' config added.", handler.getName());
  }

  public void unpublish(RServerProfile profile, List<OpalR.RPackageDto> packages) {
    DatashieldPackagesHandler handler = new DatashieldPackagesHandler(packages);
    DataShieldProfile config = (DataShieldProfile) profile;
    handler.unpublish(config);
    saveProfile(config);
    DataShieldLog.adminLog("Package '{}' config removed.", handler.getName());
  }

  public void deletePackage(DataShieldProfile config, OpalR.RPackageDto pkg) {
    DatashieldPackagesHandler handler = new DatashieldPackagesHandler(Lists.newArrayList(pkg));
    handler.deletePackage(config);
    saveProfile(config);
    DataShieldLog.adminLog("Package '{}' removed.", handler.getName());
  }

  public List<OpalR.RPackageDto> getDatashieldPackage(RServerProfile profile, final String name) {
    List<OpalR.RPackageDto> pkgs = getInstalledPackagesDtos(profile).stream()
        .filter(dto -> name.equals(dto.getName()))
        .collect(Collectors.toList());
    if (pkgs.isEmpty()) throw new NoSuchRPackageException(name);
    return pkgs;
  }

  public List<OpalR.RPackageDto> getInstalledPackagesDtos(RServerProfile profile) {
    RServerService server = rServerManagerService.getRServer(profile.getCluster());
    Map<String, List<Opal.EntryDto>> dsPackages = server.getDataShieldPackagesProperties();
    Set<String> dsNames = dsPackages.keySet();
    return server.getInstalledPackagesDtos().stream()
        .filter(dto -> dsNames.contains(dto.getName()))
        .map(dto -> {
          OpalR.RPackageDto.Builder builder = dto.toBuilder();
          for (Opal.EntryDto dsEntry : dsPackages.get(dto.getName())) {
            boolean found = false;
            for (Opal.EntryDto entry : builder.getDescriptionList()) {
              if (entry.getKey().equals(dsEntry.getKey())) {
                found = true;
                break;
              }
            }
            if (!found)
              builder.addDescription(dsEntry);
          }
          return builder.build();
        })
        .collect(Collectors.toList());
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
        .map(RServerClusterService::getName)
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
        datashieldConfigurationSupplier.modify(config -> {
          // empty legacy config
          final DSEnvironment aggs = config.getEnvironment(DSMethodType.AGGREGATE);
          Lists.newArrayList(aggs.getMethods()).forEach(m -> aggs.removeMethod(m.getName()));
          final DSEnvironment asss = config.getEnvironment(DSMethodType.ASSIGN);
          Lists.newArrayList(asss.getMethods()).forEach(m -> asss.removeMethod(m.getName()));
          config.getOptions().forEach(opt -> config.removeOption(opt.getName()));
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
