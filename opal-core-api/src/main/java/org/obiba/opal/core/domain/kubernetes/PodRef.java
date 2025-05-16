package org.obiba.opal.core.domain.kubernetes;

/**
 * Description of Kubernetes pod created by the pod spawner.
 */
public class PodRef {

  private final String name;
  private final PodSpec podSpec;
  private String status = "Pending";
  private final String ip;
  private final int port;


  public PodRef(String podName, PodSpec podSpec, String status, String ip, int port) {
    this.name = podName;
    this.podSpec = podSpec;
    this.status = status;
    this.ip = ip;
    this.port = port;
  }

  public String getName() {
    return name;
  }

  public PodSpec getPodSpec() {
    return podSpec;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getIp() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return podSpec.getNamespace() + ":" + name + " (" + status + ")";
  }
}
