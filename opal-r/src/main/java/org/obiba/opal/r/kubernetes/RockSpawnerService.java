package org.obiba.opal.r.kubernetes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import jakarta.ws.rs.NotSupportedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.core.cfg.AppsService;
import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.rock.RockServerException;
import org.obiba.opal.r.rock.RockServerStatus;
import org.obiba.opal.r.rock.RockState;
import org.obiba.opal.r.rock.RockStringMatrix;
import org.obiba.opal.r.service.RServerProfile;
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
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * R server service built over a Rock spawner application that got registered.
 */
@Component("rockSpawnerRService")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RockSpawnerService implements RServerService {

  private static final Logger log = LoggerFactory.getLogger(RockSpawnerService.class);

  private final TransactionalThreadFactory transactionalThreadFactory;

  private final EventBus eventBus;

  private final AppsService appsService;

  private String clusterName;

  private App app;

  private Map<String, RockPodSession> sessions = Maps.newHashMap();

  @Autowired
  public RockSpawnerService(TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus, AppsService appsService) {
    this.transactionalThreadFactory = transactionalThreadFactory;
    this.eventBus = eventBus;
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
    if (sessions.isEmpty()) return;
    // clean up associated pods
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(getRServerResourceUrl("/pod/"));
    } catch (RestClientException e) {
      log.error("Error when reading R server state", e);
    }
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
          restTemplate.exchange(getRServerResourceUrl("/rserver/"), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockServerStatus.class);
      return new RockState(response.getBody(), getName());
    } catch (RestClientException e) {
      log.error("Error when reading R server state", e);
      throw new RockServerException("R server state not accessible", e);
    }
  }

  @Override
  public RServerSession newRServerSession(String user) throws RServerException {
    try {
      HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
      factory.setConnectTimeout(30000);
      factory.setReadTimeout(30000);
      RestTemplate restTemplate = new RestTemplate(factory);
      ResponseEntity<PodRef> response =
          restTemplate.exchange(getRServerResourceUrl("/pod/"), HttpMethod.POST, new HttpEntity<>(createHeaders()), PodRef.class);
      PodRef pod = response.getBody();
      if (pod == null) {
        throw new RuntimeException("Unable to create Rock pod session: null response from Rock spawner");
      }
      RockPodSession session = new RockPodSession(getName(), pod, app, new AppCredentials("administrator", "password"), user, transactionalThreadFactory, eventBus);
      session.setProfile(new RServerProfile() {
        @Override
        public String getName() {
          return app.getCluster();
        }

        @Override
        public String getCluster() {
          return app.getCluster();
        }
      });
      sessions.put(pod.getName(), session);
      return session;
    } catch (RestClientException e) {
      log.error("Error when creating Rock pod session", e);
      throw new RockServerException("Rock pod session creation failed", e);
    }
  }

  @Override
  public void execute(ROperation rop) throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
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
          restTemplate.exchange(getRServerResourceUrl("/rserver/packages/"), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockStringMatrix.class);
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
    return getInstalledPackagesDtos().stream().filter(pkg -> pkg.getName().equals(name)).collect(Collectors.toList());
  }

  @Override
  public Map<String, List<Opal.EntryDto>> getDataShieldPackagesProperties() {
    Map<String, List<Opal.EntryDto>> dsPackages = Maps.newHashMap();
    try {
      HttpHeaders headers = createHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response =
          restTemplate.exchange(getRServerResourceUrl("/rserver/packages/_datashield/"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
      String jsonSource = response.getBody();
      if (response.getStatusCode().is2xxSuccessful() && jsonSource != null && !jsonSource.isEmpty()) {
        JSONObject json = new JSONObject(jsonSource);
        json.keySet().forEach(key -> {
          JSONObject dsPkgProperties = json.getJSONObject(key);
          List<Opal.EntryDto> properties = Lists.newArrayList();
          dsPkgProperties.keySet().forEach(propKey -> {
            JSONArray props = dsPkgProperties.optJSONArray(propKey);
            if (props != null) {
              Opal.EntryDto.Builder builder = Opal.EntryDto.newBuilder()
                  .setKey(propKey)
                  .setValue(StreamSupport.stream(props.spliterator(), false).map(Object::toString).collect(Collectors.joining(", ")));
              properties.add(builder.build());
            }
          });
          dsPackages.put(key, properties);
        });
      }
    } catch (Exception e) {
      log.error("Error when reading installed packages", e);
    }
    return dsPackages;
  }

  @Override
  public void removePackage(String name) throws RServerException {
    throw new NotSupportedException("Rock spawner is readonly");
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
    throw new NotSupportedException("Rock spawner does not have access to spawned R server logs");
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
    return new HttpHeaders();
  }
}
