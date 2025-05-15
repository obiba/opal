package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PodsServiceImpl implements PodsService {

  private static final Logger log = LoggerFactory.getLogger(PodsServiceImpl.class);

  private final OrientDbService orientDbService;

  private final EventBus eventBus;

  private KubernetesClient client;

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
      PodSpec podSpec = getSpec(id);
      orientDbService.delete(new PodSpec(id));
      eventBus.post(new PodSpecUnregisteredEvent(podSpec));
    } catch (Exception e) {
      log.warn("Unable to delete pod specification with ID: " + id, e);
    }
  }

  @Override
  public void saveSpec(PodSpec podSpec) {
    try {
      if (Strings.isNullOrEmpty(podSpec.getId())) {
        podSpec.setId(podSpec.getContainer().getName() + "-" + UUID.randomUUID().toString().substring(0, 8));
      }
      orientDbService.save(podSpec, podSpec);
      eventBus.post(new PodSpecRegisteredEvent(podSpec));
    } catch (Exception e) {
      log.warn("Unable to save pod specification: " + podSpec, e);
    }
  }

  @Override
  public void deleteSpecs() {
    try {
      List<PodSpec> specs = getSpecs();
      orientDbService.deleteAll(PodSpec.class);
      specs.forEach(spec -> eventBus.post(new PodSpecUnregisteredEvent(spec)));
    } catch (Exception e) {
      log.warn("Unable to delete all pod specifications", e);
    }
  }

  @Override
  public PodRef createPod(PodSpec spec) {
    String podName = spec.getContainer().getName() + "-" + UUID.randomUUID().toString().replace("-", "");
    List<EnvVar> env = spec.getContainer().getEnv().entrySet().stream().map(e -> new EnvVar(e.getKey(), e.getValue(), null)).toList();
    ResourceRequirements res = new ResourceRequirements();
    res.setRequests(Map.of("memory", new Quantity(spec.getContainer().getResources().getRequests().getMemory()), "cpu", new Quantity(spec.getContainer().getResources().getRequests().getCpu())));
    res.setLimits(Map.of("memory", new Quantity(spec.getContainer().getResources().getLimits().getMemory()), "cpu", new Quantity(spec.getContainer().getResources().getLimits().getCpu())));

    // Create pod spec
    Pod pod = new PodBuilder()
        .withNewMetadata()
          .withName(podName)
          .addToLabels("app", podName)
          .endMetadata()
        .withNewSpec()
          .addNewContainer()
            .withName(podName)
            .withImage(spec.getContainer().getImage())
            .addNewPort().withContainerPort(spec.getContainer().getPort()).endPort()
        .withEnv(env)
        .withResources(res)
        .endContainer()
        .endSpec()
        .build();

    client.pods().inNamespace(spec.getNamespace()).resource(pod).create();

    Service service = new ServiceBuilder()
        .withNewMetadata().withName(podName).endMetadata()
        .withNewSpec()
        .withSelector(Map.of("app", podName))
        .addNewPort()
        .withPort(spec.getContainer().getPort())
        //.withTargetPort(new IntOrString(spec.getContainer().getPort()))
        .endPort()
        .withType("NodePort")
        .endSpec()
        .build();

    client.services().inNamespace(spec.getNamespace()).resource(service).create();

    return ensureRunningPod(spec, podName);
  }

  @Override
  public PodRef getPod(PodSpec spec, String podName) {
    Pod pod = client.pods().inNamespace(spec.getNamespace()).withName(podName).get();
    if (pod == null) return null;

    String ip = pod.getStatus().getPodIP();
    String status = pod.getStatus().getPhase();
    String image = pod.getSpec().getContainers().getFirst().getImage();

    if (!image.equals(spec.getContainer().getImage())) return null;

    return new PodRef(podName, image, status, ip, spec.getContainer().getPort());
  }

  @Override
  public void deletePod(PodSpec spec, String podName) {
    client.pods().inNamespace(spec.getNamespace()).withName(podName).delete();
    client.services().inNamespace(spec.getNamespace()).withName(podName).delete();
  }

  @Override
  public List<PodRef> getPods(PodSpec spec) {
    List<PodRef> podRefs = Lists.newArrayList();
    var pods = client.pods().inNamespace(spec.getNamespace()).list().getItems();
    for (Pod pod : pods) {
      if (pod.getSpec().getContainers().getFirst().getImage().equals(spec.getContainer().getImage())) {
        PodRef ref = getPod(spec, pod.getMetadata().getName());
        if (ref != null) {
          podRefs.add(ref);
        }
      }
    }
    return podRefs;
  }

  @Override
  public void start() {
    getClient();

    orientDbService.createUniqueIndex(PodSpec.class);

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        orientDbService.list(PodSpec.class).forEach(spec -> eventBus.post(new PodSpecRegisteredEvent(spec)));
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

  private KubernetesClient getClient() {
    if (client == null) {
      try {
        client = new KubernetesClientBuilder().build();
      } catch (Exception e) {
        log.error("Error opening Kubernetes client", e);
      }
    }
    return client;
  }

  private PodRef ensureRunningPod(PodSpec spec, String podName) {
    int retries = 0;
    while (retries < 10) {
      PodRef ref = getPod(spec, podName);
      if (ref != null && "Running".equals(ref.getStatus())) {
        return ref;
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }
      retries++;
    }
    throw new RuntimeException("Pod " + podName + " did not become Running in time");
  }

}
