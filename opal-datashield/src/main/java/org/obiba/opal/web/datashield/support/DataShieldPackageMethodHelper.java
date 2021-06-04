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
import org.obiba.opal.datashield.cfg.DatashieldConfig;
import org.obiba.opal.datashield.cfg.DatashieldConfigService;
import org.obiba.opal.r.service.RServerManagerService;
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
  private DatashieldConfigService datashieldConfigService;

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  protected RServerManagerService rServerManagerService;

  public List<OpalR.RPackageDto> getInstalledPackagesDtos(String profile) {
    RServerService server = rServerManagerService.getRServer(profile);
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

  public List<OpalR.RPackageDto> getPackage(String profile, String name) {
    return getDatashieldPackage(profile, name);
  }

  public DataShield.DataShieldPackageMethodsDto getPackageMethods(String profile, String name) {
    return getPackageMethods(getDatashieldPackage(profile, name));
  }

  /**
   * Get the package settings and push them to the config.
   *
   * @param profile
   * @param name Package name
   * @return
   */
  public DataShield.DataShieldPackageMethodsDto publish(String profile, String name) {
    List<OpalR.RPackageDto> packageDtos = getDatashieldPackage(profile, name);
    final DataShield.DataShieldPackageMethodsDto methods = getPackageMethods(packageDtos);

    DatashieldConfig config = datashieldConfigService.getConfiguration(profile);
    addMethods(config, DSMethodType.AGGREGATE, methods.getAggregateList());
    addMethods(config, DSMethodType.ASSIGN, methods.getAssignList());
    addOptions(config, getPackageROptions(packageDtos));
    datashieldConfigService.saveConfiguration(config);

    DataShieldLog.adminLog("Package '{}' config added.", name);

    return methods;
  }

  /**
   * Remove all the methods associated to the package.
   *
   * @param profile
   * @param name Package name
   */
  public void unpublish(String profile, String name) {
    DatashieldConfig config = datashieldConfigService.getConfiguration(profile);
    removeMethods(config, DSMethodType.AGGREGATE, name);
    removeMethods(config, DSMethodType.ASSIGN, name);
    datashieldConfigService.saveConfiguration(config);
  }

  public List<OpalR.RPackageDto> getDatashieldPackage(String profile, final String name) {
    List<OpalR.RPackageDto> pkgs = getInstalledPackagesDtos(profile).stream()
        .filter(dto -> name.equals(dto.getName()))
        .collect(Collectors.toList());
    if (pkgs.isEmpty()) throw new NoSuchRPackageException(name);
    return pkgs;
  }

  public void deletePackage(String profile, String name) {
    getDatashieldPackage(profile, name).forEach(pkg -> deletePackage(profile, pkg));
  }

  public void deletePackage(String profile, OpalR.RPackageDto pkg) {
    try {
      deletePackageMethods(profile, pkg.getName(), DSMethodType.AGGREGATE);
      deletePackageMethods(profile, pkg.getName(), DSMethodType.ASSIGN);

      DatashieldConfig config = datashieldConfigService.getConfiguration(profile);
      getPackageROptions(pkg).forEach(opt -> config.removeOption(opt.getName()));
      datashieldConfigService.saveConfiguration(config);

      rPackageHelper.removePackage(rServerManagerService.getRServer(profile), pkg.getName());
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

  public void installDatashieldPackage(String profile, String name, String ref) {
    if (Strings.isNullOrEmpty(ref))
      rPackageHelper.installCRANPackage(rServerManagerService.getRServer(profile), name);
    else
      rPackageHelper.installGitHubPackage(rServerManagerService.getRServer(profile), name, ref);
  }

  //
  // Private methods
  //

  private void removeMethods(DatashieldConfig config, DSMethodType type, String name) {
    DSEnvironment env = config.getEnvironment(type);
    List<DSMethod> methodsToRemove = env.getMethods().stream()
        .filter(m -> m.hasPackage() && ((DefaultDSMethod)m).getPackage().equals(name))
        .collect(Collectors.toList());
    methodsToRemove.forEach(m -> env.removeMethod(m.getName()));
  }

  private void addMethods(DatashieldConfig config, DSMethodType type, Iterable<DataShield.DataShieldMethodDto> envMethods) {
    config.addOrUpdateMethods(type, StreamSupport.stream(envMethods.spliterator(), false)
        .map(m -> (DSMethod) Dtos.fromDto(m))
        .collect(Collectors.toList()));
  }

  private void addOptions(DatashieldConfig config, List<DataShield.DataShieldROptionDto> roptions) {
    roptions.forEach(opt -> config.addOrUpdateOption(opt.getName(), opt.getValue()));
  }

  // delete package methods as they are configured, not as they are declared (to handle overlaps)
  private void deletePackageMethods(String profile, String name, DSMethodType type) {
    DatashieldConfig config = datashieldConfigService.getConfiguration(profile);

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

    datashieldConfigService.saveConfiguration(config);
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
