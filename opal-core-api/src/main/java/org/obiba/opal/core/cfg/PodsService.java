package org.obiba.opal.core.cfg;

import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.service.SystemService;

import java.util.List;

/**
 * Kubernetes pod specifications.
 */
public interface PodsService extends SystemService {

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
   * @return
   */
  PodRef createPod(PodSpec spec);

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
}
