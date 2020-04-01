/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.NoSuchDSMethodException;
import org.obiba.datashield.core.impl.PackagedFunctionDSMethod;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.spi.r.AbstractROperationWithResult;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RStringMatrix;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.NoSuchRPackageException;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataShieldPackageMethodHelper {

  private static final Logger log = LoggerFactory.getLogger(DataShieldPackageMethodHelper.class);

  /**
   * The DataShield's description file to be read from DataShield compliant packages.
   */
  private static final String DATASHIELD_DESCRIPTION_FILE = "DATASHIELD";

  @Autowired
  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  private DataShieldMethodConverterRegistry methodConverterRegistry;

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    Map<String, List<Opal.EntryDto>> dsPackages = getDataShieldPackagesProperties();
    return rPackageHelper.getInstalledPackagesDtos().stream()
        .map(dto -> {
          if (dsPackages.containsKey(dto.getName())) {
            return dto.toBuilder().addAllDescription(dsPackages.get(dto.getName())).build();
          } else {
            return dto;
          }
        })
        .filter(this::isDataShieldPackage)
        .collect(Collectors.toList());
  }

  public OpalR.RPackageDto getPackage(String name) {
    return getDatashieldPackage(name);
  }

  public DataShield.DataShieldPackageMethodsDto getPackageMethods(String name) {
    return getPackageMethods(getDatashieldPackage(name));
  }

  public DataShield.DataShieldPackageMethodsDto publish(String name) {
    OpalR.RPackageDto packageDto = getDatashieldPackage(name);

    final DataShield.DataShieldPackageMethodsDto methods = getPackageMethods(packageDto);
    final List<DataShield.DataShieldROptionDto> roptions = getPackageROptions(packageDto);
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

  public OpalR.RPackageDto getDatashieldPackage(final String name) {

    return rPackageHelper.getInstalledPackagesDtos().stream()
        .filter(dto -> dto != null && dto.getName().equals(name))
        .map(dto -> {
          DataShieldPackageROperation rop = new DataShieldPackageROperation(name);
          REXP rexp = rPackageHelper.execute(rop).getResult();
          return dto.toBuilder().addAllDescription(getDataShieldPackagePropertiesDtos(rexp)).build();
        })
        .filter(this::isDataShieldPackage)
        .findFirst().orElseThrow(() -> new NoSuchRPackageException(name));
  }

  public void deletePackage(String name) {
    deletePackage(getDatashieldPackage(name));
  }

  public void deletePackage(OpalR.RPackageDto pkg) {
    deletePackageMethods(pkg.getName(), DSMethodType.AGGREGATE);
    deletePackageMethods(pkg.getName(), DSMethodType.ASSIGN);
    getPackageROptions(pkg).forEach(opt -> configurationSupplier.get().removeOption(opt.getName()));
    rPackageHelper.removePackage(pkg.getName());
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

  private List<DataShield.DataShieldROptionDto> getPackageROptions(OpalR.RPackageDto packageDto) {
    List<DataShield.DataShieldROptionDto> optionDtos = Lists.newArrayList();
    for (Opal.EntryDto entry : packageDto.getDescriptionList()) {
      String key = entry.getKey();
      if (RPackageResourceHelper.OPTIONS.equals(key)) {
        optionDtos.addAll(new DataShieldROptionsParser().parse(entry.getValue()));
      }
    }

    return optionDtos;
  }

  private DataShield.DataShieldPackageMethodsDto getPackageMethods(OpalR.RPackageDto packageDto) {
    String version = getPackageVersion(packageDto);

    List<DataShield.DataShieldMethodDto> aggregateMethodDtos = Lists.newArrayList();
    List<DataShield.DataShieldMethodDto> assignMethodDtos = Lists.newArrayList();
    for (Opal.EntryDto entry : packageDto.getDescriptionList()) {
      String key = entry.getKey();
      if (RPackageResourceHelper.AGGREGATE_METHODS.equals(key)) {
        aggregateMethodDtos.addAll(parsePackageMethods(packageDto.getName(), version, entry.getValue()));
      } else if (RPackageResourceHelper.ASSIGN_METHODS.equals(key)) {
        assignMethodDtos.addAll(parsePackageMethods(packageDto.getName(), version, entry.getValue()));
      }
    }
    return DataShield.DataShieldPackageMethodsDto.newBuilder().setName(packageDto.getName()).addAllAggregate(aggregateMethodDtos)
        .addAllAssign(assignMethodDtos).build();
  }

  private String getPackageVersion(OpalR.RPackageDto packageDto) {
    for (Opal.EntryDto entry : packageDto.getDescriptionList()) {
      if (entry.getKey().equals(RPackageResourceHelper.VERSION)) {
        return entry.getValue();
      }
    }
    // will not happen in R
    return null;
  }

  private Collection<DataShield.DataShieldMethodDto> parsePackageMethods(String packageName, String packageVersion,
                                                                         String value) {
    String normalized = value.replaceAll("[\n\r\t ]", "");
    List<DataShield.DataShieldMethodDto> methodDtos = Lists.newArrayList();
    for (String map : normalized.split(",")) {
      String[] entry = map.split("=");
      DataShield.RFunctionDataShieldMethodDto methodDto = DataShield.RFunctionDataShieldMethodDto.newBuilder()
          .setFunc(entry.length == 1 ? packageName + "::" + entry[0] : entry[1]).setRPackage(packageName)
          .setVersion(packageVersion).build();
      DataShield.DataShieldMethodDto.Builder builder = DataShield.DataShieldMethodDto.newBuilder();
      builder.setName(entry[0]);
      builder.setExtension(DataShield.RFunctionDataShieldMethodDto.method, methodDto);
      methodDtos.add(builder.build());
    }
    return methodDtos;
  }

  private Map<String, List<Opal.EntryDto>> getDataShieldPackagesProperties() {
    Map<String, List<Opal.EntryDto>> dsPackages = Maps.newHashMap();
    try {
      DataShieldPackagesROperation rop = new DataShieldPackagesROperation();
      REXP res = rPackageHelper.execute(rop).getResult();
      RList dsList = res.asList();
      if (dsList.isNamed()) {
        for (Object name : dsList.names) {
          REXP rexp = dsList.at(name.toString());
          dsPackages.put(name.toString(), getDataShieldPackagePropertiesDtos(rexp));
        }
      }
    } catch (Exception e) {
      log.error("DataShield packages properties extraction failed", e);
    }
    return dsPackages;
  }

  private List<Opal.EntryDto> getDataShieldPackagePropertiesDtos(REXP rexp) {
    List<Opal.EntryDto> entries = Lists.newArrayList();
    try {
      RStringMatrix dsProperties = new RStringMatrix(rexp);
      String[] colNames = dsProperties.getColumnNames();
      String[] values = dsProperties.iterateRows().iterator().next();
      for (int i = 0; i < colNames.length; i++) {
        Opal.EntryDto.Builder builder = Opal.EntryDto.newBuilder();
        builder.setKey(colNames[i]);
        builder.setValue(values[i]);
        entries.add(builder.build());
      }
    } catch (Exception e) {
      log.error("DataShield package properties extraction failed", e);
    }
    return entries;
  }

  private boolean isDataShieldPackage(@Nullable OpalR.RPackageDto input) {
    if (input == null) return false;
    // DataShield properties are declared in the DESCRIPTION file
    for (Opal.EntryDto entry : input.getDescriptionList()) {
      String key = entry.getKey();
      if (RPackageResourceHelper.AGGREGATE_METHODS.equals(key) || RPackageResourceHelper.ASSIGN_METHODS.equals(key) || RPackageResourceHelper.OPTIONS.equals(key)) {
        return !"NA".equals(entry.getValue());
      }
    }
    return false;
  }

  public ROperationWithResult installDatashieldPackage(String name, String ref) {
    return rPackageHelper.installPackage(name, ref, "datashield");
  }

  /**
   * Result is a named list of matrices, where names are package names and matrices are the corresponding DATASHIELD file content.
   */
  private class DataShieldPackagesROperation extends AbstractROperationWithResult {

    @Override
    protected void doWithConnection() {
      setResult(null);
      eval(String.format("is.null(assign('x', lapply(installed.packages()[,1], function(p) { system.file('%s', package=p) })))", DATASHIELD_DESCRIPTION_FILE), false);
      setResult(eval("lapply(x[lapply(x, nchar)>0], read.dcf)", false));
    }
  }

  /**
   * Result is the package's DATASHIELD file content as a matrix or NA.
   */
  private class DataShieldPackageROperation extends AbstractROperationWithResult {

    private final String name;

    private DataShieldPackageROperation(String name) {
      this.name = name;
    }

    @Override
    protected void doWithConnection() {
      setResult(null);
      eval( String.format("is.null(assign('x', system.file('%s', package='%s')))", DATASHIELD_DESCRIPTION_FILE, name), false);
      setResult(eval("if(nchar(x)>0) read.dcf(x) else NA", false));
    }
  }

}
