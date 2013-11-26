/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield.support;

import java.util.Collection;
import java.util.List;

import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.datashield.DataShieldEnvironment;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.NoSuchDataShieldMethodException;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.web.datashield.RPackageResource;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class DataShieldPackageMethodImpl extends RPackageResource {

  @Autowired
  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  private DataShieldMethodConverterRegistry methodConverterRegistry;

  private String name;

  public void setName(String value) {
    name = value;
  }

  public OpalR.RPackageDto getPackage(String name) throws REXPMismatchException {
    setName(name);
    return getDatashieldPackage(name);
  }

  public DataShield.DataShieldPackageMethodsDto getPackageMethods(String name) throws REXPMismatchException {
    setName(name);
    return getPackageMethods();
  }

  public DataShield.DataShieldPackageMethodsDto publish(String name) throws REXPMismatchException {
    setName(name);
    OpalR.RPackageDto packageDto = getDatashieldPackage(name);

    final DataShield.DataShieldPackageMethodsDto methods = getPackageMethods(packageDto);

    configurationSupplier
        .modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<DatashieldConfiguration>() {

          @Override
          public void doWithConfig(DatashieldConfiguration config) {
            addMethods(configurationSupplier.get().getAggregateEnvironment(), methods.getAggregateList());
            addMethods(configurationSupplier.get().getAssignEnvironment(), methods.getAssignList());
          }

          private void addMethods(DataShieldEnvironment env, Iterable<DataShield.DataShieldMethodDto> envMethods) {
            for(DataShield.DataShieldMethodDto method : envMethods) {
              if(env.hasMethod(method.getName())) {
                env.removeMethod(method.getName());
              }
              env.addMethod(methodConverterRegistry.parse(method));
            }
          }
        });

    return methods;
  }

  public void deletePackage(String name) throws REXPMismatchException {
    setName(name);
    DataShield.DataShieldPackageMethodsDto methods = getPackageMethods(getDatashieldPackage(name));

    // Aggregate
    for(int i = 0; i < methods.getAggregateCount(); i++) {
      final String methodName = methods.getAggregate(i).getName();

      try {
        configurationSupplier
            .modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<DatashieldConfiguration>() {

              @Override
              public void doWithConfig(DatashieldConfiguration config) {
                config.getEnvironment(DatashieldConfiguration.Environment.AGGREGATE).removeMethod(methodName);
              }
            });
        DataShieldLog.adminLog("deleted method '{}' from environment {}.", methodName,
            DatashieldConfiguration.Environment.AGGREGATE);
      } catch(NoSuchDataShieldMethodException nothing) {
        // nothing, the method may have been deleted manually
      }
    }

    // Assign
    for(int i = 0; i < methods.getAssignCount(); i++) {
      final String methodName = methods.getAssign(i).getName();

      try {
        configurationSupplier
            .modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<DatashieldConfiguration>() {

              @Override
              public void doWithConfig(DatashieldConfiguration config) {
                config.getEnvironment(DatashieldConfiguration.Environment.ASSIGN).removeMethod(methodName);
              }
            });
        DataShieldLog.adminLog("deleted method '{}' from environment {}.", methodName,
            DatashieldConfiguration.Environment.ASSIGN);
      } catch(NoSuchDataShieldMethodException nothing) {
        // nothing, the method may have been deleted manually
      }
    }
    removePackage(name);
  }

  private OpalR.RPackageDto getPackage() throws REXPMismatchException {
    return getDatashieldPackage(name);
  }

  private DataShield.DataShieldPackageMethodsDto getPackageMethods() throws REXPMismatchException {
    return getPackageMethods(getPackage());
  }

  private DataShield.DataShieldPackageMethodsDto getPackageMethods(OpalR.RPackageDto packageDto)
      throws REXPMismatchException {
    String version = getPackageVersion(packageDto);

    List<DataShield.DataShieldMethodDto> aggregateMethodDtos = Lists.newArrayList();
    List<DataShield.DataShieldMethodDto> assignMethodDtos = Lists.newArrayList();
    for(Opal.EntryDto entry : packageDto.getDescriptionList()) {
      if(entry.getKey().equals(AGGREGATE_METHODS)) {
        aggregateMethodDtos.addAll(parsePackageMethods(packageDto.getName(), version, entry.getValue()));
      } else if(entry.getKey().equals(ASSIGN_METHODS)) {
        assignMethodDtos.addAll(parsePackageMethods(packageDto.getName(), version, entry.getValue()));
      }
    }
    return DataShield.DataShieldPackageMethodsDto.newBuilder().setName(name).addAllAggregate(aggregateMethodDtos)
        .addAllAssign(assignMethodDtos).build();
  }

  private String getPackageVersion(OpalR.RPackageDto packageDto) {
    for(Opal.EntryDto entry : packageDto.getDescriptionList()) {
      if(entry.getKey().equals(VERSION)) {
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
    for(String map : normalized.split(",")) {
      String[] entry = map.split("=");
      DataShield.RFunctionDataShieldMethodDto methodDto = DataShield.RFunctionDataShieldMethodDto.newBuilder()
          .setFunc(entry.length == 1 ? name + "::" + entry[0] : entry[1]).setRPackage(packageName)
          .setVersion(packageVersion).build();
      DataShield.DataShieldMethodDto.Builder builder = DataShield.DataShieldMethodDto.newBuilder();
      builder.setName(entry[0]);
      builder.setExtension(DataShield.RFunctionDataShieldMethodDto.method, methodDto);
      methodDtos.add(builder.build());
    }
    return methodDtos;
  }

}
