package org.obiba.opal.core.cfg;

import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.service.SystemService;

import java.util.List;
import java.util.Map;

/**
 * Kubernetes pod specifications.
 */
public interface PodsService extends SystemService {

  /**
   * Check if the current environment is running inside a Kubernetes cluster.
   * @return
   */
  boolean isInsideKubernetes();

  /**
   * Get the pod specifications.
   *
   * @return
   */
  List<PodSpec> getSpecs();

  /**
   * Get the pod specification with the given id.
   * @param id
   * @return
   */
  PodSpec getSpec(String id);

  /**
   * Delete the pod specification with the given id.
   * @param id
   */
  void deleteSpec(String id);

  /**
   * Save the pod specification.
   * @param spec
   */
  void saveSpec(PodSpec spec);

  /**
   * Delete all the pod specifications.
   */
  void deleteSpecs();

  /**
   * Create a pod with the given specification.
   * @param spec
   * @param env environment variables to be passed to the pod, can be null.
   * @return
   */
  PodRef createPod(PodSpec spec, Map<String, String> env);

  /**
   * Get the pod with the given specification and name.
   * @param spec
   * @param podName
   * @return
   */
  PodRef getPod(PodSpec spec, String podName);

  /**
   * Delete the pod with the given reference.
   * @param pod
   */
  void deletePod(PodRef pod);

  /**
   * Get the pods with the given specification.
   * @param spec
   * @return
   */
  List<PodRef> getPods(PodSpec spec);

  /**
   * Delete the pods with the given specification.
   * @param spec
   */
  void deletePods(PodSpec spec);

  interface PodRefCallback {
    void onPodRef(PodRef podRef);
  }
}
