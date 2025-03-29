package org.obiba.opal.r.spawner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.ws.rs.NotSupportedException;
import org.apache.commons.codec.binary.Base64;
import org.obiba.opal.core.cfg.AppsService;
import org.obiba.opal.core.domain.RockSpawnerAppConfig;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.rock.RockServerException;
import org.obiba.opal.r.rock.RockServerStatus;
import org.obiba.opal.r.rock.RockState;
import org.obiba.opal.r.rock.RockStringMatrix;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.r.service.RServerState;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.RServerException;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * R server service built over a Rock spawner application that got registered.
 */
@Component("rockSpawnerRService")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RockSpawnerService implements RServerService {

  private static final Logger log = LoggerFactory.getLogger(RockSpawnerService.class);

  private final AppsService appsService;

  private String clusterName;

  private App app;

  @Autowired
  public RockSpawnerService(AppsService appsService) {
    this.appsService = appsService;
  }

  @Override
  public String getName() {
    return app.getName() + "~" + app.getId();
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  public RServerState getState() throws RServerException {
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<RockServerStatus> response =
          restTemplate.exchange(getRServerResourceUrl("/rserver"), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockServerStatus.class);
      return new RockState(response.getBody(), getName());
    } catch (RestClientException e) {
      log.error("Error when reading R server state", e);
      throw new RockServerException("R server state not accessible", e);
    }
  }

  @Override
  public RServerSession newRServerSession(String user) throws RServerException {
    return null;
  }

  @Override
  public void execute(ROperation rop) throws RServerException {
    throw new NotSupportedException();
  }

  @Override
  public App getApp() {
    return app;
  }

  @Override
  public boolean isFor(App app) {
    return this.app.equals(app);
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    List<OpalR.RPackageDto> pkgs = Lists.newArrayList();
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<RockStringMatrix> response =
          restTemplate.exchange(getRServerResourceUrl("/rserver/packages"), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockStringMatrix.class);
      RockStringMatrix matrix = response.getBody();
      pkgs = matrix.iterateRows().stream()
          .map(new RPackageResourceHelper.StringsToRPackageDto(clusterName, getName(), matrix))
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error when reading installed packages", e);
    }
    return pkgs;
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackageDto(String name) {
    return List.of();
  }

  @Override
  public void removePackage(String name) throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
  }

  @Override
  public Map<String, List<Opal.EntryDto>> getDataShieldPackagesProperties() {
    return Map.of();
  }

  @Override
  public void ensureCRANPackage(String name) throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
  }

  @Override
  public void installCRANPackage(String name) throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
  }

  @Override
  public void installGitHubPackage(String name, String ref) throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
  }

  @Override
  public void installBioconductorPackage(String name) throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
  }

  @Override
  public void installLocalPackage(String path) throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
  }

  @Override
  public void updateAllCRANPackages() throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
  }

  @Override
  public String[] getLog(Integer nbLines) {
    return new String[0];
  }

  public void setApp(App app) {
    this.app = app;
  }

  public void setRServerClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  private String getRServerResourceUrl(String path) {
    return app.getServer() + path;
  }

  private HttpHeaders createHeaders() {
    RockSpawnerAppConfig config = appsService.getRockSpawnerAppConfig(app);
    if (config.hasToken()) {
      return new HttpHeaders() {{
        add("X-API-KEY", config.getToken());
      }};
    }
    return new HttpHeaders();
  }
}
