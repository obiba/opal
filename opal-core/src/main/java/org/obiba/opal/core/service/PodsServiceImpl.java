package org.obiba.opal.core.service;

import com.google.cloud.tools.jib.api.ImageReference;
import com.google.cloud.tools.jib.api.InvalidImageReferenceException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.common.eventbus.EventBus;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.obiba.opal.core.cfg.PodsService;
import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.event.PodSpecRegisteredEvent;
import org.obiba.opal.core.event.PodSpecUnregisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PodsServiceImpl implements PodsService {

  private static final Logger log = LoggerFactory.getLogger(PodsServiceImpl.class);

  @Value("${pods.rock.images.allowed}")
  private String[] allowedRockPodImages;

  @Value("${pods.rock.specs}")
  private String defaultRockPodSpecs;

  @Value("${pods.maxAttempts}")
  private int podExistsMaxAttempts;

  @Value("${pods.delay}")
  private int podExistsDelay;

  private final List<ImageReference> allowedPodImageRefsRock = Lists.newArrayList();

  private final OrientDbService orientDbService;

  private final EventBus eventBus;

  private KubernetesClient client;

  private Boolean isInCluster = null;

  @Override
  public boolean isInsideKubernetes() {
    if (isInCluster != null) return isInCluster;
    isInCluster = System.getenv("KUBERNETES_SERVICE_HOST") != null;
    log.info("Inside Kubernetes cluster: {}", isInCluster);
    return isInCluster;
  }

  @Autowired
  public PodsServiceImpl(OrientDbService orientDbService, EventBus eventBus) {
    this.orientDbService = orientDbService;
    this.eventBus = eventBus;
  }

  @Override
  public List<PodSpec> getSpecs() {
    return Lists.newArrayList(orientDbService.list(PodSpec.class));
  }

  @Override
  public PodSpec getSpec(String id) {
    PodSpec found = orientDbService.findUnique(new PodSpec(id));
    if (found != null) return found;
    throw new NoSuchElementException("No registered pod specifications with ID: " + id);
  }

  @Override
  public void deleteSpec(String id) {
    try {
      PodSpec spec = getSpec(id);
      orientDbService.delete(new PodSpec(id));
      new Thread(() -> eventBus.post(new PodSpecUnregisteredEvent(spec))).start();
    } catch (Exception e) {
      log.warn("Unable to delete pod specification with ID: {}", id, e);
    }
  }

  @Override
  public void saveSpec(PodSpec spec) {
    try {
      checkImage(spec);
      if (Strings.isNullOrEmpty(spec.getId())) {
        spec.setId(spec.getContainer().getName() + "-" + UUID.randomUUID().toString().substring(0, 8));
      }
      orientDbService.save(spec, spec);
      new Thread(() -> eventBus.post(spec.isEnabled() ? new PodSpecRegisteredEvent(spec) : new PodSpecUnregisteredEvent(spec))).start();
    } catch (Exception e) {
      log.warn("Unable to save pod specification: {}", spec, e);
    }
  }

  private void checkImage(PodSpec spec) {
    String imageStr = spec.getContainer().getImage();
    try {
      ImageReference imageReference = ImageReference.parse(imageStr);
      checkRockImage(spec, imageReference);
    } catch (InvalidImageReferenceException e) {
      throw new IllegalArgumentException("Pod image " + imageStr + " is not valid", e);
    }

  }

  private void checkRockImage(PodSpec spec, ImageReference imageReference) {
    if (!"rock".equals(spec.getType())) return;
    String imageStr = spec.getContainer().getImage();
    // check if it matches any allowed image refs
    if (allowedPodImageRefsRock.isEmpty()) return;
    boolean hasMatch = allowedPodImageRefsRock.stream().anyMatch((allowedRef) -> imageReference.getRegistry().equals(allowedRef.getRegistry()) && imageReference.getRepository().equals(allowedRef.getRepository()));
    if (hasMatch) return;
    throw new IllegalArgumentException("Pod 'rock' image " + imageStr + " is not allowed (allowed images: " + Arrays.toString(allowedRockPodImages) + ")");
  }

  @Override
  public void deleteSpecs() {
    try {
      List<PodSpec> specs = getSpecs();
      orientDbService.deleteAll(PodSpec.class);
      new Thread(() -> specs.forEach(spec -> eventBus.post(new PodSpecUnregisteredEvent(spec)))).start();
    } catch (Exception e) {
      log.warn("Unable to delete all pod specifications", e);
    }
  }

  @Override
  public PodRef createPod(PodSpec spec, Map<String, String> env) {
    String podName = spec.getContainer().getName() + "--" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    // user provided env vars
    List<EnvVar> envVars = spec.getContainer().getEnv().entrySet().stream()
        .filter(e -> env == null || !env.containsKey(e.getKey()))
        .map(e -> new EnvVar(e.getKey(), e.getValue(), null)).collect(Collectors.toList());
    // system provided env vars
    if (env != null) {
      env.entrySet().stream()
          .map(e -> new EnvVar(e.getKey(), e.getValue(), null))
          .forEach(envVars::add);
    }
    ResourceRequirements res = new ResourceRequirements();
    res.setRequests(Map.of("memory", new Quantity(spec.getContainer().getResources().getRequests().getMemory()), "cpu", new Quantity(spec.getContainer().getResources().getRequests().getCpu())));
    res.setLimits(Map.of("memory", new Quantity(spec.getContainer().getResources().getLimits().getMemory()), "cpu", new Quantity(spec.getContainer().getResources().getLimits().getCpu())));

    // Create pod spec
    Pod pod = new PodBuilder()
        .withNewMetadata()
          .withName(podName)
          .addToLabels("app", podName)
          .addToLabels(spec.getLabels())
          .endMetadata()
        .withNewSpec()
          .addNewContainer()
            .withName(podName)
            .withImage(spec.getContainer().getImage())
            .withImagePullPolicy(spec.getContainer().getImagePullPolicy())
            .addNewPort().withContainerPort(spec.getContainer().getPort()).endPort()
            .withEnv(envVars)
            .withLivenessProbe(new ProbeBuilder()
                .withNewTcpSocket()
                .withPort(new IntOrString(spec.getContainer().getPort()))
                .endTcpSocket()
                .withInitialDelaySeconds(2)   // wait before first probe
                .withPeriodSeconds(5)          // check every 5s
                .build())
            .withReadinessProbe(new ProbeBuilder()
                .withNewHttpGet()
                .withPath("/_check")
                .withPort(new IntOrString(spec.getContainer().getPort()))
                .endHttpGet()
                .withInitialDelaySeconds(2)    // start checking after 5s
                .withPeriodSeconds(5)          // check every 5s
                .build())
            .withResources(res)
          .endContainer()
          .withImagePullSecrets(spec.getContainer().hasImagePullSecret() ? new LocalObjectReferenceBuilder().withName(spec.getContainer().getImagePullSecret()).build() : null)
          .withNodeName(spec.getNodeName())
          .withNodeSelector(spec.getNodeSelector())
          .withTolerations(spec.getTolerations().stream()
            .map(tol -> new Toleration(tol.getEffect().name(), tol.getKey(), tol.getOperator().name(), tol.getTolerationSeconds(), tol.getValue()))
            .toList())
        .endSpec()
        .build();

    client.pods().inNamespace(getNamespace(spec)).resource(pod).create();

    Service service = new ServiceBuilder()
        .withNewMetadata().withName(podName).endMetadata()
        .withNewSpec()
        .withSelector(Map.of("app", podName))
        .addNewPort()
        .withPort(spec.getContainer().getPort())
        //.withTargetPort(new IntOrString(spec.getContainer().getPort()))
        .endPort()
        .withType("ClusterIP")
        .endSpec()
        .build();

    client.services().inNamespace(getNamespace(spec)).resource(service).create();

    return ensurePodExists(spec, podName);
  }

  @Override
  public PodRef getPod(PodSpec spec, String podName) {
    Pod pod = client.pods().inNamespace(getNamespace(spec)).withName(podName).get();
    if (pod == null) return null;
    return getPodRef(spec, pod);
  }

  @Override
  public void deletePod(PodRef pod) {
    try {
      PodSpec spec = pod.getPodSpec();
      client.pods().inNamespace(getNamespace(spec)).withName(pod.getName()).delete();
      client.services().inNamespace(getNamespace(spec)).withName(pod.getName()).delete();
    } catch (Exception e) {
      log.warn("Unable to delete pod: {}", pod, e);
    }
  }

  @Override
  public List<PodRef> getPods(PodSpec spec) {
    log.debug("Get pods of {}", spec.getId());
    List<PodRef> podRefs = Lists.newArrayList();
    var pods = client.pods().inNamespace(getNamespace(spec)).list().getItems();
    for (Pod pod : pods) {
      log.debug("Pod name={} image={}", pod.getMetadata().getName(), pod.getSpec().getContainers().getFirst().getImage());
      // check image
      boolean sameImage = pod.getSpec().getContainers().getFirst().getImage().equals(spec.getContainer().getImage());
      if (!sameImage) continue;
      // check name prefix
      String[] parts = pod.getMetadata().getName().split("--");
      log.debug("... prefix={}", parts[0]);
      if (parts.length == 2 && parts[0].equals(spec.getContainer().getName())) {
        PodRef ref = getPodRef(spec, pod);
        if (ref != null) {
          podRefs.add(ref);
        }
      }
    }
    return podRefs;
  }

  @Override
  public void deletePods(PodSpec spec) {
    List<PodRef> podRefs = getPods(spec);
    podRefs.forEach(this::deletePod);
  }

  @Override
  public void start() {
    initClient();
    orientDbService.createUniqueIndex(PodSpec.class);

    if (allowedRockPodImages != null) {
      allowedPodImageRefsRock.clear();
      for (String allowedPodImage : allowedRockPodImages) {
        try {
          allowedPodImageRefsRock.add(ImageReference.parse(allowedPodImage));
        } catch (Exception e) {
          log.error("Unable to parse allowed docker image {}", allowedPodImage, e);
        }
      }
    }

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        Streams.stream(orientDbService.list(PodSpec.class))
            .filter(PodSpec::isEnabled)
            .forEach(spec -> eventBus.post(new PodSpecRegisteredEvent(spec)));
        if (!Strings.isNullOrEmpty(defaultRockPodSpecs)) {
          List<PodSpec> podSpecs = RockPodSpecFactory.makeRockPodSpecs(defaultRockPodSpecs);
          for (PodSpec podSpec : podSpecs) {
            PodSpec found = orientDbService.findUnique(new PodSpec(podSpec.getId()).setType("rock"));
            if (found == null) {
              saveSpec(podSpec);
            }
          }
        }
      }
    }, 5000);
  }

  @Override
  public void stop() {
    orientDbService.list(PodSpec.class).forEach(spec -> eventBus.post(new PodSpecUnregisteredEvent(spec)));
    if (client == null) return;
    try {
      client.close();
    } catch (Exception e) {
      log.error("Error closing Kubernetes client", e);
    } finally {
      client = null;
    }
  }

  //
  // Private methods
  //

  private String getNamespace(PodSpec spec) {
    return Strings.isNullOrEmpty(spec.getNamespace()) ? client.getNamespace() : spec.getNamespace();
  }

  private PodRef getPodRef(PodSpec spec, Pod pod) {
    String podName = pod.getMetadata().getName();
    Service service = client.services().inNamespace(getNamespace(spec)).withName(podName).get();
    if (service == null)
      log.warn("Unable to find service for pod: {}", podName);
    String ip = service != null ? service.getSpec().getClusterIP() : null;
    String status = pod.getStatus().getPhase();
    String image = pod.getSpec().getContainers().getFirst().getImage();
    if (!image.equals(spec.getContainer().getImage())) return null;

//    EventList eventList = client.v1().events().inNamespace(getNamespace(spec))
//        .withField("involvedObject.name", podName)
//        .withField("involvedObject.kind", "Pod").list();
//    for (Event event : eventList.getItems()) {
//      System.out.println(
//            "Type: " + event.getType() +
//                " Reason: " + event.getReason() +
//                " Message: " + event.getMessage() +
//                " Count: " + event.getCount() +
//                " LastTimestamp: " + event.getLastTimestamp()
//        );
//    }
    return new PodRef(podName, spec, status, ip, spec.getContainer().getPort());
  }

  private void initClient() {
    if (client == null) {
      try {
        client = new KubernetesClientBuilder().build();
        isInsideKubernetes();
      } catch (Exception e) {
        log.error("Error opening Kubernetes client", e);
      }
    }
  }

  /**
   * Wait for pod to be available, may take some time when a k8s node creation is needed.
   *
   * @param spec
   * @param podName
   * @return
   */
  private PodRef ensurePodExists(PodSpec spec, String podName) {
    int retries = 0;
    while (retries < podExistsMaxAttempts) {
      PodRef pod = getPod(spec, podName);
      if (pod != null && !Strings.isNullOrEmpty(pod.getName())) {
        return pod;
      }
      retries++;
      log.info("Waiting for pod {} to be ready (attempt {})", podName, retries);
      try {
        Thread.sleep(podExistsDelay);
      } catch (InterruptedException ignored) {
      }
    }
    throw new RuntimeException("Pod " + podName + " was not created in time");
  }

}
