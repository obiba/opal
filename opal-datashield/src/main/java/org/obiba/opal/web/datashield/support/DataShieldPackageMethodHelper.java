/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.obiba.datashield.core.impl.PackagedFunctionDSMethod;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.r.service.RServerService;
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

@Component
public class DataShieldPackageMethodHelper {

  private static final Logger log = LoggerFactory.getLogger(DataShieldPackageMethodHelper.class);

  public static final String VERSION = "Version";

  public static final String AGGREGATE_METHODS = "AggregateMethods";

  public static final String ASSIGN_METHODS = "AssignMethods";

  public static final String OPTIONS = "Options";

  @Autowired
  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  private DataShieldMethodConverterRegistry methodConverterRegistry;

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  protected RServerManagerService rServerManagerService;

  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    RServerService server = rServerManagerService.getDefaultRServer();
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

  public List<OpalR.RPackageDto> getPackage(String name) {
    return getDatashieldPackage(name);
  }

  public DataShield.DataShieldPackageMethodsDto getPackageMethods(String name) {
    return getPackageMethods(getDatashieldPackage(name));
  }

  public DataShield.DataShieldPackageMethodsDto publish(String name) {
    List<OpalR.RPackageDto> packageDtos = getDatashieldPackage(name);

    final DataShield.DataShieldPackageMethodsDto methods = getPackageMethods(packageDtos);
    final List<DataShield.DataShieldROptionDto> roptions = getPackageROptions(packageDtos);
    configurationSupplier
        .modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<DatashieldConfiguration>() {

          @Override
          public void doWithConfig(DatashieldConfiguration config) {
            addMethods(configurationSupplier.get().getEnvironment(DSMethodType.AGGREGATE), methods.getAggregateList());
            addMethods(configurationSupplier.get().getEnvironment(DSMethodType.ASSIGN), methods.getAssignList());
            config.addOptions(roptions);
          }

          private void addMethods(DSEnvironment env, Iterable<DataShield.DataShieldMethodDto> envMethods) {
            for (DataShield.DataShieldMethodDto method : envMethods) {
              if (env.hasMethod(method.getName())) {
                env.removeMethod(method.getName());
              }
              env.addOrUpdate(methodConverterRegistry.parse(method));
            }
          }
        });

    return methods;
  }

  public List<OpalR.RPackageDto> getDatashieldPackage(final String name) {
    return getInstalledPackagesDtos().stream()
        .filter(dto -> name.equals(dto.getName()))
        .collect(Collectors.toList());
  }

  public void deletePackage(String name) {
    getDatashieldPackage(name).forEach(this::deletePackage);
  }

  public void deletePackage(OpalR.RPackageDto pkg) {
    try {
      deletePackageMethods(pkg.getName(), DSMethodType.AGGREGATE);
      deletePackageMethods(pkg.getName(), DSMethodType.ASSIGN);
      getPackageROptions(pkg).forEach(opt -> configurationSupplier.get().removeOption(opt.getName()));
      rPackageHelper.removePackage(rServerManagerService.getDefaultRServer(), pkg.getName());
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

  public void installDatashieldPackage(String name, String ref) {
    if (Strings.isNullOrEmpty(ref))
      rPackageHelper.installCRANPackage(rServerManagerService.getDefaultRServer(), name);
    else
      rPackageHelper.installGitHubPackage(rServerManagerService.getDefaultRServer(), name, ref);
  }

  //
  // Private methods
  //

  // delete package methods as they are configured, not as they are declared (to handle overlaps)
  private void deletePackageMethods(String name, DSMethodType type) {
    List<DSMethod> configuredPkgMethods = configurationSupplier.get().getEnvironment(type).getMethods()
        .stream().filter(m -> m instanceof PackagedFunctionDSMethod)
        .filter(m -> name.equals(((PackagedFunctionDSMethod) m).getPackage()))
        .collect(Collectors.toList());

    for (DSMethod method : configuredPkgMethods) {
      final String methodName = method.getName();
      try {
        configurationSupplier.modify(config -> config.getEnvironment(type).removeMethod(methodName));
        DataShieldLog.adminLog("deleted method '{}' from environment {}.", methodName, type);
      } catch (NoSuchDSMethodException nothing) {
        // nothing, the method may have been deleted manually
      }
    }
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
