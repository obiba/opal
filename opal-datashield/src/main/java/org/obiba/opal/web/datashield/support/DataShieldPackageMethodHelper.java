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
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.NoSuchDSMethodException;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.datashield.core.impl.PackagedFunctionDSMethod;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.web.datashield.Dtos;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.NoSuchRPackageException;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class DataShieldPackageMethodHelper {

  private static final Logger log = LoggerFactory.getLogger(DataShieldPackageMethodHelper.class);

  public static final String VERSION = "Version";

  public static final String AGGREGATE_METHODS = "AggregateMethods";

  public static final String ASSIGN_METHODS = "AssignMethods";

  public static final String OPTIONS = "Options";

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  protected RServerManagerService rServerManagerService;

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

  public List<OpalR.RPackageDto> getPackage(RServerProfile profile, String name) {
    return getDatashieldPackage(profile, name);
  }

  public DataShield.DataShieldPackageMethodsDto getPackageMethods(RServerProfile profile, String name) {
    return getPackageMethods(getDatashieldPackage(profile, name));
  }

  /**
   * Get the package settings and push them to the profiles config.
   *
   * @param profiles Profiles expected to be from the same R cluster
   * @param name     Package name
   * @return
   */
  public DataShield.DataShieldPackageMethodsDto publish(List<RServerProfile> profiles, String name) {
    List<OpalR.RPackageDto> packageDtos = getDatashieldPackage(profiles.get(0), name);
    DataShield.DataShieldPackageMethodsDto rval = DataShield.DataShieldPackageMethodsDto.newBuilder().setName(name).build();
    for (RServerProfile profile : profiles)
      rval = publish(profile, packageDtos);
    return rval;
  }

  /**
   * Get the package settings and push them to the profile's config.
   *
   * @param profile
   * @param name    Package name
   * @return
   */
  public DataShield.DataShieldPackageMethodsDto publish(RServerProfile profile, String name) {
    List<OpalR.RPackageDto> packageDtos = getDatashieldPackage(profile, name);
    return publish(profile, packageDtos);
  }

  public DataShield.DataShieldPackageMethodsDto publish(RServerProfile profile, OpalR.RPackageDto packageDto) {
    return publish(profile, Lists.newArrayList(packageDto));
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
      packageDtos = getDatashieldPackage(profiles.get(0), name);
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

  private void unpublish(RServerProfile profile, List<OpalR.RPackageDto> packageDtos) {
    String name = packageDtos.get(0).getName();
    DataShieldProfile config = (DataShieldProfile) profile;
    removeMethods(config, DSMethodType.AGGREGATE, config.getEnvironment(DSMethodType.AGGREGATE).getMethods().stream()
        .filter(m -> m.hasPackage() && ((DefaultDSMethod) m).getPackage().equals(name))
        .map(DSMethod::getName)
        .collect(Collectors.toList()));
    removeMethods(config, DSMethodType.ASSIGN, config.getEnvironment(DSMethodType.ASSIGN).getMethods().stream()
        .filter(m -> m.hasPackage() && ((DefaultDSMethod) m).getPackage().equals(name))
        .map(DSMethod::getName)
        .collect(Collectors.toList()));
    // cannot identify the R options by the package where they are defined
    if (packageDtos != null)
      removeOptions(config, getPackageROptions(packageDtos));
    datashieldProfileService.saveProfile(config);
  }

  public List<OpalR.RPackageDto> getDatashieldPackage(RServerProfile profile, final String name) {
    List<OpalR.RPackageDto> pkgs = getInstalledPackagesDtos(profile).stream()
        .filter(dto -> name.equals(dto.getName()))
        .collect(Collectors.toList());
    if (pkgs.isEmpty()) throw new NoSuchRPackageException(name);
    return pkgs;
  }

  public void deletePackage(RServerProfile profile, String name) {
    getDatashieldPackage(profile, name).forEach(pkg -> deletePackage((DataShieldProfile) profile, pkg));
  }

  public void deletePackage(DataShieldProfile profile, OpalR.RPackageDto pkg) {
    try {
      DataShield.DataShieldPackageMethodsDto methods = getPackageMethods(pkg);
      removeMethods(profile, DSMethodType.AGGREGATE, methods.getAggregateList().stream()
          .map(DataShield.DataShieldMethodDto::getName).collect(Collectors.toList()));
      removeMethods(profile, DSMethodType.ASSIGN, methods.getAssignList().stream()
          .map(DataShield.DataShieldMethodDto::getName).collect(Collectors.toList()));
      removeOptions(profile, getPackageROptions(pkg));
      datashieldProfileService.saveProfile(profile);

      rPackageHelper.removePackage(rServerManagerService.getRServer(profile.getCluster()), pkg.getName());
    } catch (Exception e) {
      log.warn("Failed to delete an R package: {}", pkg.getName());
    }
  }

  public static Collection<DataShield.DataShieldMethodDto> parsePackageMethods(String packageName, String packageVersion,
                                                                               String value) {
    String normalized = value.replaceAll("[\n\r\t ]", "");
    List<DataShield.DataShieldMethodDto> methodDtos = Lists.newArrayList();
    for (String map : normalized.split(",")) {
      String[] entry = map.split("=");
      if (!Strings.isNullOrEmpty(entry[0])) {
        DataShield.RFunctionDataShieldMethodDto methodDto = DataShield.RFunctionDataShieldMethodDto.newBuilder()
            .setFunc(entry.length == 1 ? packageName + "::" + entry[0] : entry[1])
            .setRPackage(packageName)
            .setVersion(packageVersion).build();
        DataShield.DataShieldMethodDto.Builder builder = DataShield.DataShieldMethodDto.newBuilder();
        builder.setName(entry[0]);
        builder.setExtension(DataShield.RFunctionDataShieldMethodDto.method, methodDto);
        methodDtos.add(builder.build());
      }
    }
    return methodDtos;
  }

  public void installDatashieldPackage(RServerProfile profile, String name, String ref, String manager) {
    if (Strings.isNullOrEmpty(manager) || "cran".equalsIgnoreCase(manager))
      rPackageHelper.installCRANPackage(rServerManagerService.getRServer(profile.getCluster()), name);
    else if ("gh".equalsIgnoreCase(manager) || "github".equalsIgnoreCase(manager))
      rPackageHelper.installGitHubPackage(rServerManagerService.getRServer(profile.getCluster()), name, ref);
    else if ("local".equalsIgnoreCase(manager))
      rPackageHelper.installLocalPackage(rServerManagerService.getRServer(profile.getCluster()), name);
  }

  //
  // Private methods
  //

  private DataShield.DataShieldPackageMethodsDto publish(RServerProfile profile, List<OpalR.RPackageDto> packageDtos) {
    String name = packageDtos.get(0).getName();
    final DataShield.DataShieldPackageMethodsDto methods = getPackageMethods(packageDtos);

    DataShieldProfile config = (DataShieldProfile) profile;
    removeMethods(config, DSMethodType.AGGREGATE, config.getEnvironment(DSMethodType.AGGREGATE).getMethods().stream()
        .filter(m -> m.hasPackage() && ((DefaultDSMethod) m).getPackage().equals(name))
        .map(DSMethod::getName)
        .collect(Collectors.toList()));
    addMethods(config, DSMethodType.AGGREGATE, methods.getAggregateList());
    removeMethods(config, DSMethodType.ASSIGN, config.getEnvironment(DSMethodType.ASSIGN).getMethods().stream()
        .filter(m -> m.hasPackage() && ((DefaultDSMethod) m).getPackage().equals(name))
        .map(DSMethod::getName)
        .collect(Collectors.toList()));
    addMethods(config, DSMethodType.ASSIGN, methods.getAssignList());
    addOptions(config, getPackageROptions(packageDtos));
    datashieldProfileService.saveProfile(config);

    DataShieldLog.adminLog("Package '{}' config added.", name);

    return methods;
  }

  private void removeMethods(DataShieldProfile config, DSMethodType type, Iterable<String> envMethods) {
    DSEnvironment env = config.getEnvironment(type);
    envMethods.forEach(env::removeMethod);
  }

  private void removeOptions(DataShieldProfile config, List<DataShield.DataShieldROptionDto> roptions) {
    roptions.forEach(opt -> config.removeOption(opt.getName()));
  }

  private void addMethods(DataShieldProfile config, DSMethodType type, Iterable<DataShield.DataShieldMethodDto> envMethods) {
    config.addOrUpdateMethods(type, StreamSupport.stream(envMethods.spliterator(), false)
        .map(m -> (DSMethod) Dtos.fromDto(m))
        .collect(Collectors.toList()));
  }

  private void addOptions(DataShieldProfile config, List<DataShield.DataShieldROptionDto> roptions) {
    roptions.forEach(opt -> config.addOrUpdateOption(opt.getName(), opt.getValue()));
  }

  // delete package methods as they are configured, not as they are declared (to handle overlaps)
  private void deletePackageMethods(String profile, String name, DSMethodType type) {
    DataShieldProfile config = datashieldProfileService.getProfile(profile);

    List<DSMethod> configuredPkgMethods = config.getEnvironment(type).getMethods()
        .stream().filter(m -> m instanceof PackagedFunctionDSMethod)
        .filter(m -> name.equals(((PackagedFunctionDSMethod) m).getPackage()))
        .collect(Collectors.toList());

    for (DSMethod method : configuredPkgMethods) {
      final String methodName = method.getName();
      try {
        config.getEnvironment(type).removeMethod(methodName);
        DataShieldLog.adminLog("deleted method '{}' from environment {}.", methodName, type);
      } catch (NoSuchDSMethodException nothing) {
        // nothing, the method may have been deleted manually
      }
    }

    datashieldProfileService.saveProfile(config);
  }

  private List<DataShield.DataShieldROptionDto> getPackageROptions(List<OpalR.RPackageDto> packageDtos) {
    // TODO merge options
    OpalR.RPackageDto packageDto = packageDtos.get(0);
    return getPackageROptions(packageDto);
  }

  private List<DataShield.DataShieldROptionDto> getPackageROptions(OpalR.RPackageDto packageDto) {
    List<DataShield.DataShieldROptionDto> optionDtos = Lists.newArrayList();
    for (Opal.EntryDto entry : packageDto.getDescriptionList()) {
      String key = entry.getKey();
      if (OPTIONS.equals(key)) {
        optionDtos.addAll(new DataShieldROptionsParser().parse(entry.getValue()));
      }
    }
    return optionDtos;
  }

  private DataShield.DataShieldPackageMethodsDto getPackageMethods(List<OpalR.RPackageDto> packageDtos) {
    // TODO merge methods
    return getPackageMethods(packageDtos.get(0));
  }

  private DataShield.DataShieldPackageMethodsDto getPackageMethods(OpalR.RPackageDto packageDto) {
    List<DataShield.DataShieldMethodDto> aggregateMethodDtos = Lists.newArrayList();
    List<DataShield.DataShieldMethodDto> assignMethodDtos = Lists.newArrayList();

    String version = getPackageVersion(packageDto);
    for (Opal.EntryDto entry : packageDto.getDescriptionList()) {
      String key = entry.getKey();
      if (AGGREGATE_METHODS.equals(key)) {
        aggregateMethodDtos.addAll(parsePackageMethods(packageDto.getName(), version, entry.getValue()));
      } else if (ASSIGN_METHODS.equals(key)) {
        assignMethodDtos.addAll(parsePackageMethods(packageDto.getName(), version, entry.getValue()));
      }
    }
    return DataShield.DataShieldPackageMethodsDto.newBuilder().setName(packageDto.getName())
        .addAllAggregate(aggregateMethodDtos)
        .addAllAssign(assignMethodDtos).build();
  }

  private String getPackageVersion(OpalR.RPackageDto packageDto) {
    for (Opal.EntryDto entry : packageDto.getDescriptionList()) {
      if (entry.getKey().equals(VERSION)) {
        return entry.getValue();
      }
    }
    // will not happen in R
    return null;
  }

}
