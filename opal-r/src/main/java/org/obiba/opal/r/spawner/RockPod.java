package org.obiba.opal.r.spawner;

/**
 * Description of Kubernetes pod created by the Rock spawner.
 */
public class RockPod {

  private String name;
  private String image;
  private String status = "Pending";
  private String ip;
  private int port = -1;
  private String service_ip;
  private int service_port = -1;

  public RockPod() {}

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

  public String getService_ip() {
    return service_ip;
  }

  public void setService_ip(String service_ip) {
    this.service_ip = service_ip;
  }

  public int getService_port() {
    return service_port;
  }

  public void setService_port(int service_port) {
    this.service_port = service_port;
  }
}
