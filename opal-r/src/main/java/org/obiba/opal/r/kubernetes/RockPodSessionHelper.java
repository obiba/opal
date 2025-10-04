package org.obiba.opal.r.kubernetes;

import org.obiba.opal.core.cfg.PodsService;
import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class RockPodSessionHelper {

  private static final Logger log = LoggerFactory.getLogger(RockPodSessionHelper.class);

  public static final AppCredentials DEFAULT_CREDENTIALS = new AppCredentials("administrator", "password");

  @Value("${pods.rock.running.maxAttempts}")
  private int runningMaxAttempts;

  @Value("${pods.rock.running.delay}")
  private int runningDelay;

  @Value("${pods.rock.connection.maxAttempts}")
  private int connectionMaxAttempts;

  @Value("${pods.rock.connection.delay}")
  private int connectionDelay;

  private final PodsService podsService;

  @Autowired
  public RockPodSessionHelper(PodsService podsService) {
    this.podsService = podsService;
  }

  public void deletePod(PodRef pod) {
    podsService.deletePod(pod);
  }

  public PodRef getPod(PodSpec podSpec, String name) {
    return podsService.getPod(podSpec, name);
  }

  public PodRef ensureRunningPod(PodRef pod) {
    if (pod == null) {
      log.error("Pod is null, cannot ensure running pod");
      return null;
    }
    return ensureRunningPod(pod.getPodSpec(), pod.getName(), null);
  }

  /**
   * Pod exists but wait for it to be running, may take a while until docker image is downloaded.
   *
   * @param spec
   * @param podName
   * @param callback
   * @return
   */
  public PodRef ensureRunningPod(PodSpec spec, String podName, PodsService.PodRefCallback callback) {
    // When pod is ContainerCreating state (for instance docker image is being downloaded),
    // it is still not Running, then wait
    int retries = 0;
    while (retries < runningMaxAttempts) {
      PodRef pod = podsService.getPod(spec, podName);
      if (callback != null) {
        callback.onPodRef(pod);
      }
      if ("Running".equals(pod.getStatus())) {
        return pod;
      }
      try {
        Thread.sleep(runningDelay);
      } catch (InterruptedException ignored) {
      }
      retries++;
    }
    throw new RuntimeException("Pod " + podName + " did not become Running in time");
  }

  /**
   * Pod is running but wait for Rock app to be ready.
   *
   * @param pod
   */
  public void ensureRServerReady(PodRef pod) {
    if (pod == null) {
      log.error("Pod is null, cannot ensure R server ready");
      return;
    }
    boolean ready = false;
    int attempts = 1;
    while (!ready && attempts <= connectionMaxAttempts) {
      RestTemplate restTemplate = new RestTemplate();
      try {
        ResponseEntity<String> response =
            restTemplate.exchange(getRServerResourceUrl(pod, "/_check"), HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class);
        ready = response.getStatusCode().is2xxSuccessful();
      } catch (Exception e) {
        if (log.isTraceEnabled()) {
          log.warn("Error when checking R server pod {} status", pod.getName(), e);
        } else if (log.isDebugEnabled()) {
          log.warn("Error when checking R server pod {} status", pod.getName());
        }
      }
      if (!ready) {
        log.info("Waiting for R server pod {} to be ready (attempt {})", pod.getName(), attempts);
        attempts++;
        try {
          Thread.sleep(connectionDelay);
        } catch (InterruptedException e) {
          log.error("Interrupted while waiting for R server pod {} to be ready", pod.getName());
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  public String getRServerResourceUrl(PodRef pod, String path) {
    if (pod == null) {
      log.error("Pod is null, cannot get R server resource url");
      return null;
    }
    return String.format("http://%s:%s%s", pod.getName(), pod.getPort(), path);
  }

  public HttpHeaders createHeaders() {
    return new HttpHeaders() {{
      String auth = DEFAULT_CREDENTIALS.getUser() + ":" + DEFAULT_CREDENTIALS.getPassword();
      byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
      String authHeader = "Basic " + new String(encodedAuth);
      add("Authorization", authHeader);
    }};
  }
}
