package org.obiba.opal.core.domain.kubernetes;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;

public class Container {

  private String name;
  private String image;
  private String imagePullPolicy = "IfNotPresent";
  private String imagePullSecret;
  private int port;
  private Map<String, String> env;
  private ResourceRequirements resources;

  public Container() {}

  public String getName() {
    return name;
  }

  public Container setName(String name) {
    this.name = name;
    return this;
  }

  public String getImage() {
    return image;
  }

  public Container setImage(String image) {
    this.image = image;
    return this;
  }

  public String getImagePullPolicy() {
    return imagePullPolicy;
  }

  public Container setImagePullPolicy(String imagePullPolicy) {
    this.imagePullPolicy = Strings.isNullOrEmpty(imagePullPolicy) ? "IfNotPresent" : imagePullPolicy.trim();
    return this;
  }

  public boolean hasImagePullSecret() {
    return !Strings.isNullOrEmpty(imagePullSecret);
  }

  public String getImagePullSecret() {
    return imagePullSecret;
  }

  public Container setImagePullSecret(String imagePullSecret) {
    this.imagePullSecret = imagePullSecret;
    return this;
  }

  public int getPort() {
    return port;
  }

  public Container setPort(int port) {
    this.port = port;
    return this;
  }

  public Map<String, String> getEnv() {
    return env == null ? env = Maps.newHashMap() : env;
  }

  public Container setEnv(Map<String, String> env) {
    this.env = env;
    return this;
  }

  public ResourceRequirements getResources() {
    return resources;
  }

  public Container setResources(ResourceRequirements resources) {
    this.resources = resources;
    return this;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("image", image)
        .add("port", port)
        .add("env", env)
        .add("resources", resources)
        .toString();
  }

  //
  // Inner classes
  //

  public static class ResourceList {

    String cpu;
    String memory;

    public ResourceList() {}

    public ResourceList setCpu(String cpu) {
      this.cpu = cpu;
      return this;
    }

    public String getCpu() {
      return cpu;
    }

    public ResourceList setMemory(String memory) {
      this.memory = memory;
      return this;
    }

    public String getMemory() {
      return memory;
    }
  }

  public static class ResourceRequirements {
    ResourceList limits;
    ResourceList requests;

    public ResourceRequirements() {}

    public ResourceRequirements setLimits(ResourceList limits) {
      this.limits = limits;
      return this;
    }

    public ResourceList getLimits() {
      return limits;
    }

    public ResourceRequirements setRequests(ResourceList requests) {
      this.requests = requests;
      return this;
    }

    public ResourceList getRequests() {
      return requests;
    }
  }
}
