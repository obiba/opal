package org.obiba.opal.r.kubernetes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import jakarta.ws.rs.NotSupportedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.core.cfg.PodsService;
import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.cluster.RServerClusterState;
import org.obiba.opal.r.rock.RockServerException;
import org.obiba.opal.r.rock.RockServerStatus;
import org.obiba.opal.r.rock.RockState;
import org.obiba.opal.r.rock.RockStringMatrix;
import org.obiba.opal.r.service.RServerProfile;
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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
public class RockSpawnerService implements RServerPodService {

  private static final Logger log = LoggerFactory.getLogger(RockSpawnerService.class);

  private final TransactionalThreadFactory transactionalThreadFactory;

  private final EventBus eventBus;

  private final PodsService podsService;

  private String clusterName;

  private Map<String, RockPodSession> sessions = Maps.newHashMap();

  private PodSpec podSpec;

  private final List<OpalR.RPackageDto> packages = Lists.newArrayList();

  private final Map<String, List<Opal.EntryDto>> dsPackages = Maps.newHashMap();

  private static final AppCredentials DEFAULT_CREDENTIALS = new AppCredentials("administrator", "password");

  @Autowired
  public RockSpawnerService(TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus, PodsService podsService) {
    this.transactionalThreadFactory = transactionalThreadFactory;
    this.eventBus = eventBus;
    this.podsService = podsService;
  }

  @Override
  public String getName() {
    return podSpec.getId();
  }

  @Override
  public void start() {
    // init as background task
    Runnable task = () -> {
      PodRef pod = null;
      try {
        pod = createRockPod();
        initInstalledPackagesDtos(pod);
        initDataShieldPackagesProperties(pod);
      } catch (Exception e) {
        log.error("Error when reading installed packages and DataSHIELD properties", e);
      } finally {
        if (pod != null) podsService.deletePod(pod);
      }
    };
    Thread thread = new Thread(task);
    thread.start();
  }

  @Override
  public void stop() {
    if (sessions.isEmpty()) return;
    // clean up associated pods
    podsService.deletePods(podSpec);
  }

  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  public RServerState getState() throws RServerException {
    RServerClusterState state = new RServerClusterState(getName());
    state.setRunning(isRunning());
    for (RockPodSession session : sessions.values()) {
      try {
        RServerState podState = getState(session.getPodRef());
        state.setVersion(podState.getVersion());
        state.addTags(podState.getTags());
        state.addRSessionsCount(podState.getRSessionsCount());
        state.addBusyRSessionsCount(podState.getBusyRSessionsCount());
        state.setSystemCores(podState.getSystemCores());
        state.setSystemFreeMemory(podState.getSystemFreeMemory());
      } catch (RestClientException e) {
        log.error("Error when reading R server state", e);
      }
    }
    return state;
  }

  @Override
  public RServerSession newRServerSession(String user) throws RServerException {
    try {
      PodRef pod = createRockPod();
      RockPodSession session = new RockPodSession(getName(), pod, DEFAULT_CREDENTIALS, user, podsService, transactionalThreadFactory, eventBus);
      session.setProfile(new RServerProfile() {
        @Override
        public String getName() {
          return podSpec.getId();
        }

        @Override
        public String getCluster() {
          return podSpec.getId();
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
  public boolean isFor(App app) {
    return false;
  }

  @Override
  public synchronized List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    if (!packages.isEmpty()) return packages;
    PodRef pod = null;
    try {
      pod = createRockPod();
      initInstalledPackagesDtos(pod);
    } catch (Exception e) {
      log.error("Error when reading installed packages", e);
    } finally {
      if (pod != null) podsService.deletePod(pod);
    }
    return packages;
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackageDto(String name) {
    return getInstalledPackagesDtos().stream().filter(pkg -> pkg.getName().equals(name)).collect(Collectors.toList());
  }

  @Override
  public Map<String, List<Opal.EntryDto>> getDataShieldPackagesProperties() {
    if (!dsPackages.isEmpty()) return dsPackages;
    PodRef pod = null;
    try {
      pod = createRockPod();
      initDataShieldPackagesProperties(pod);
    } catch (Exception e) {
      log.error("Error when reading installed packages", e);
    } finally {
      if (pod != null) podsService.deletePod(pod);
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

  public void setRServerClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setPodSpec(PodSpec podSpec) {
    this.podSpec = podSpec;
  }

  @Override
  public PodSpec getPodSpec() {
    return podSpec;
  }

  //
  // Private methods
  //

  private RServerState getState(PodRef pod) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<RockServerStatus> response =
        restTemplate.exchange(getRServerResourceUrl(pod, "/rserver"), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockServerStatus.class);
    return new RockState(response.getBody(), getName());
  }

  private synchronized void initInstalledPackagesDtos(PodRef pod) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<RockStringMatrix> response =
        restTemplate.exchange(getRServerResourceUrl(pod,"/rserver/packages"), HttpMethod.GET, new HttpEntity<>(createHeaders()), RockStringMatrix.class);
    RockStringMatrix matrix = response.getBody();
    if (matrix == null) return;
    packages.addAll(matrix.iterateRows().stream()
        .map(new RPackageResourceHelper.StringsToRPackageDto(clusterName, getName(), matrix))
        .toList());
  }

  private synchronized void initDataShieldPackagesProperties(PodRef pod) {
    HttpHeaders headers = createHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response =
        restTemplate.exchange(getRServerResourceUrl(pod,"/rserver/packages/_datashield"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
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
  }

  /**
   * Create a rock pod that is ready to use (check successful).
   * @return
   */
  private PodRef createRockPod() {
    try {
      Map<String, String> env = Maps.newHashMap();
      env.put("ROCK_CLUSTER", clusterName);
      // TODO make it optional
      env.put("ROCK_SECURITY_ENABLED", "false");
      PodRef pod =  podsService.createPod(podSpec, env);
      boolean ready = false;
      int attempts = 0;
      while (!ready && attempts < 20) {
        RestTemplate restTemplate = new RestTemplate();
        try {
          ResponseEntity<String> response =
              restTemplate.exchange(getRServerResourceUrl(pod, "/_check"), HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class);
          ready = response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
          log.error("Error when checking R server pod {} status", pod.getName(), e);
        }
        if (!ready) {
          log.info("Waiting for R server pod {} to be ready (attempt {})", pod.getName(), attempts);
          attempts++;
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            log.error("Interrupted while waiting for R server pod {} to be ready", pod.getName());
            Thread.currentThread().interrupt();
          }
        }
      }
      return pod;
    } catch (Exception e) {
      log.error("Error when reading installed packages", e);
      throw new RuntimeException(e);
    }
  }

  private String getRServerResourceUrl(PodRef pod, String path) {
    return String.format("http://%s:%s%s", pod.getName(), pod.getPort(), path);
  }

  private HttpHeaders createHeaders() {
    return new HttpHeaders() {{
      String auth = DEFAULT_CREDENTIALS.getUser() + ":" + DEFAULT_CREDENTIALS.getPassword();
      byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
      String authHeader = "Basic " + new String(encodedAuth);
      add("Authorization", authHeader);
    }};
  }
}
