package org.obiba.opal.datashield.cfg;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.web.datashield.Dtos;
import org.obiba.opal.web.datashield.support.DataShieldROptionsParser;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DatashieldPackagesHandler {

  public static final String VERSION = "Version";

  public static final String AGGREGATE_METHODS = "AggregateMethods";

  public static final String ASSIGN_METHODS = "AssignMethods";

  public static final String OPTIONS = "Options";

  private final String name;

  private final List<OpalR.RPackageDto> packages;

  /**
   * Datashield configuration handler for an R package (may have several representations).
   *
   * @param packages
   */
  public DatashieldPackagesHandler(List<OpalR.RPackageDto> packages) {
    this.name = packages.getFirst().getName();
    this.packages = packages;
  }

  /**
   * The R package name.
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Publish package's Datashield settings.
   *
   * @param config Datashield configuration
   */
  public void publish(DataShieldProfile config) {
    final DataShield.DataShieldPackageMethodsDto methods = getPackageMethods();
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
    addOptions(config, getPackageROptions());
  }

  /**
   * Unpublish package's Datashield settings.
   *
   * @param config Datashield configuration
   */
  public void unpublish(DataShieldProfile config) {
    removeMethods(config, DSMethodType.AGGREGATE, config.getEnvironment(DSMethodType.AGGREGATE).getMethods().stream()
        .filter(m -> m.hasPackage() && ((DefaultDSMethod) m).getPackage().equals(name))
        .map(DSMethod::getName)
        .collect(Collectors.toList()));
    removeMethods(config, DSMethodType.ASSIGN, config.getEnvironment(DSMethodType.ASSIGN).getMethods().stream()
        .filter(m -> m.hasPackage() && ((DefaultDSMethod) m).getPackage().equals(name))
        .map(DSMethod::getName)
        .collect(Collectors.toList()));
    // cannot identify the R options by the package where they are defined
    if (packages != null)
      removeOptions(config, getPackageROptions(packages.getFirst()));
  }

  public void deletePackage(DataShieldProfile profile) {
    OpalR.RPackageDto pkg = packages.getFirst();
    DataShield.DataShieldPackageMethodsDto methods = getPackageMethods(pkg);
    removeMethods(profile, DSMethodType.AGGREGATE, methods.getAggregateList().stream()
        .map(DataShield.DataShieldMethodDto::getName).collect(Collectors.toList()));
    removeMethods(profile, DSMethodType.ASSIGN, methods.getAssignList().stream()
        .map(DataShield.DataShieldMethodDto::getName).collect(Collectors.toList()));
    removeOptions(profile, getPackageROptions(pkg));
  }

  public DataShield.DataShieldPackageMethodsDto getPackageMethods() {
    // TODO merge methods
    return getPackageMethods(packages.getFirst());
  }

  //
  // Private methods
  //

  private void removeMethods(DataShieldProfile config, DSMethodType type, Iterable<String> envMethods) {
    DSEnvironment env = config.getEnvironment(type);
    envMethods.forEach(env::removeMethod);
  }

  private void removeOptions(DataShieldProfile config, List<DataShield.DataShieldROptionDto> rOptions) {
    rOptions.forEach(opt -> config.removeOption(opt.getName()));
  }

  private void addMethods(DataShieldProfile config, DSMethodType type, Iterable<DataShield.DataShieldMethodDto> envMethods) {
    config.addOrUpdateMethods(type, StreamSupport.stream(envMethods.spliterator(), false)
        .map(m -> (DSMethod) Dtos.fromDto(m))
        .collect(Collectors.toList()));
  }

  private void addOptions(DataShieldProfile config, List<DataShield.DataShieldROptionDto> rOptions) {
    rOptions.forEach(opt -> config.addOrUpdateOption(opt.getName(), opt.getValue()));
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

  private  String getPackageVersion(OpalR.RPackageDto packageDto) {
    for (Opal.EntryDto entry : packageDto.getDescriptionList()) {
      if (entry.getKey().equals(VERSION)) {
        return entry.getValue();
      }
    }
    // will not happen in R
    return null;
  }

  private List<DataShield.DataShieldROptionDto> getPackageROptions() {
    // TODO merge options
    OpalR.RPackageDto packageDto = packages.getFirst();
    return getPackageROptions(packageDto);
  }

  private List<DataShield.DataShieldROptionDto> getPackageROptions(OpalR.RPackageDto packageDto) {
    List<DataShield.DataShieldROptionDto> options = Lists.newArrayList();
    for (Opal.EntryDto entry : packageDto.getDescriptionList()) {
      String key = entry.getKey();
      if (OPTIONS.equals(key)) {
        options.addAll(new DataShieldROptionsParser().parse(entry.getValue()));
      }
    }
    return options;
  }

  @VisibleForTesting
  public static  Collection<DataShield.DataShieldMethodDto> parsePackageMethods(String packageName, String packageVersion,
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
}
