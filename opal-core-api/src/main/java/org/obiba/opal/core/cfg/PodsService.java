package org.obiba.opal.core.cfg;

import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.service.SystemService;

import java.util.List;

/**
 * Kubernetes pod specifications.
 */
public interface PodsService extends SystemService {

  List<PodSpec> getSpecs();

  PodSpec getSpec(String id);

  void deleteSpec(String id);

  void saveSpec(PodSpec podSpec);

  void deleteSpecs();

  PodRef createPod(PodSpec podSpec);

  PodRef getPod(PodSpec spec, String podName);

  void deletePod(PodSpec spec, String podName);

  List<PodRef> getPods(PodSpec spec);
}
