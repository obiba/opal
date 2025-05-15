package org.obiba.opal.core.domain.kubernetes;

/**
 * Description of Kubernetes pod created by the pod spawner.
 */
public class PodRef {

  private String name;
  private String image;
  private String status = "Pending";
  private String ip;
  private int port = -1;

  public PodRef() {}

  public PodRef(String podName, String image, String status, String ip, int port) {
    this.name = podName;
    this.image = image;
    this.status = status;
    this.ip = ip;
    this.port = port;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
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

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
