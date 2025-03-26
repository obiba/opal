package org.obiba.opal.r.spawner;

import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.r.service.RServerState;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.RServerException;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * R server service built over a Rock spawner application that got registered.
 */
@Component("rockSpawnerRService")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RockSpawnerService implements RServerService {

  @Override
  public String getName() {
    return "";
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public RServerState getState() throws RServerException {
    return null;
  }

  @Override
  public RServerSession newRServerSession(String user) throws RServerException {
    return null;
  }

  @Override
  public void execute(ROperation rop) throws RServerException {

  }

  @Override
  public App getApp() {
    return null;
  }

  @Override
  public boolean isFor(App app) {
    return false;
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    return List.of();
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackageDto(String name) {
    return List.of();
  }

  @Override
  public void removePackage(String name) throws RServerException {

  }

  @Override
  public Map<String, List<Opal.EntryDto>> getDataShieldPackagesProperties() {
    return Map.of();
  }

  @Override
  public void ensureCRANPackage(String name) throws RServerException {

  }

  @Override
  public void installCRANPackage(String name) throws RServerException {

  }

  @Override
  public void installGitHubPackage(String name, String ref) throws RServerException {

  }

  @Override
  public void installBioconductorPackage(String name) throws RServerException {

  }

  @Override
  public void installLocalPackage(String path) throws RServerException {

  }

  @Override
  public void updateAllCRANPackages() throws RServerException {

  }

  @Override
  public String[] getLog(Integer nbLines) {
    return new String[0];
  }
}
