/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield.support;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.datashield.cfg.DatashieldPackagesHandler;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.NoSuchRPackageException;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatashieldPackageMethodHelper {

  private static final Logger log = LoggerFactory.getLogger(DatashieldPackageMethodHelper.class);

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  protected RServerManagerService rServerManagerService;

  public List<OpalR.RPackageDto> getInstalledPackagesDtos(RServerProfile profile) {
    return datashieldProfileService.getInstalledPackagesDtos(profile);
  }

  public List<OpalR.RPackageDto> getPackage(RServerProfile profile, String name) {
    return getDatashieldPackage(profile, name);
  }

  public DataShield.DataShieldPackageMethodsDto getPackageMethods(RServerProfile profile, String name) {
    DatashieldPackagesHandler handler = new DatashieldPackagesHandler(datashieldProfileService.getDatashieldPackage(profile, name));
    return handler.getPackageMethods();
  }

  /**
   * Get the package settings and push them to the profiles config.
   *
   * @param profiles Profiles expected to be from the same R cluster
   * @param name     Package name
   */
  public void publish(List<RServerProfile> profiles, String name) {
    for (RServerProfile profile : profiles) {
      List<OpalR.RPackageDto> packages = getDatashieldPackage(profiles.getFirst(), name);
      datashieldProfileService.publish(profile, packages);
    }
  }

  /**
   * Get the package settings and push them to the profile's config.
   *
   * @param profile R server profile
   * @param name    Package name
   */
  public void publish(RServerProfile profile, String name) {
    datashieldProfileService.publish(profile, name);
  }

  public void publish(RServerProfile profile, OpalR.RPackageDto packageDto) {
    datashieldProfileService.publish(profile, Lists.newArrayList(packageDto));
  }

  /**
   * Remove all the methods associated to the package from the profiles settings.
   *
   * @param profiles Profiles expected to be from the same R cluster
   * @param name     Package name
   */
  public void unpublish(List<RServerProfile> profiles, String name) {
    List<OpalR.RPackageDto> packageDtos = null;
    try {
      packageDtos = getDatashieldPackage(profiles.getFirst(), name);
    } catch (NoSuchRPackageException e) {
      return;
    }
    for (RServerProfile profile : profiles)
      unpublish(profile, packageDtos);
  }

  /**
   * Remove all the methods associated to the package from the profile's settings.
   *
   * @param profile
   * @param name    Package name
   */
  public void unpublish(RServerProfile profile, String name) {
    List<OpalR.RPackageDto> packageDtos = null;
    try {
      packageDtos = getDatashieldPackage(profile, name);
    } catch (NoSuchRPackageException e) {
      return;
    }
    unpublish(profile, packageDtos);
  }

  private void unpublish(RServerProfile profile, List<OpalR.RPackageDto> packages) {
    datashieldProfileService.unpublish(profile, packages);
  }

  public List<OpalR.RPackageDto> getDatashieldPackage(RServerProfile profile, final String name) {
    return datashieldProfileService.getInstalledPackagesDtos(profile);
  }

  public void deletePackage(RServerProfile profile, String name) {
    getDatashieldPackage(profile, name).forEach(pkg -> deletePackage((DataShieldProfile) profile, pkg));
  }

  public void deletePackage(DataShieldProfile profile, OpalR.RPackageDto pkg) {
    try {
      datashieldProfileService.deletePackage(profile, pkg);
      rPackageHelper.removePackage(rServerManagerService.getRServer(profile.getCluster()), pkg.getName());
    } catch (Exception e) {
      log.warn("Failed to delete an R package: {}", pkg.getName());
    }
  }

  public void installDatashieldPackage(RServerProfile profile, String name, String ref, String manager) {
    if (Strings.isNullOrEmpty(manager) || "cran".equalsIgnoreCase(manager))
      rPackageHelper.installCRANPackage(rServerManagerService.getRServer(profile.getCluster()), name);
    else if ("gh".equalsIgnoreCase(manager) || "github".equalsIgnoreCase(manager))
      rPackageHelper.installGitHubPackage(rServerManagerService.getRServer(profile.getCluster()), name, ref);
    else if ("local".equalsIgnoreCase(manager))
      rPackageHelper.installLocalPackage(rServerManagerService.getRServer(profile.getCluster()), name);
  }

}
