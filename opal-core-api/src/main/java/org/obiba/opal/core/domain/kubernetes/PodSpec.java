package org.obiba.opal.core.domain.kubernetes;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.obiba.opal.core.domain.HasUniqueProperties;

import java.util.List;

public class PodSpec implements HasUniqueProperties {

  private String id = "1";

  private String type = "rock";

  private String description = "";

  private String namespace;

  private Container container;

  private boolean enabled = false;

  public PodSpec() {}

  public PodSpec(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public PodSpec setId(String id) {
    this.id = id;
    return this;
  }

  public String getType() {
    return type;
  }

  public PodSpec setType(String type) {
    this.type = type;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public PodSpec setDescription(String description) {
    this.description = description;
    return this;
  }

  public PodSpec setNamespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  public String getNamespace() {
    return namespace;
  }

  public Container getContainer() {
    return container;
  }

  public PodSpec setContainer(Container container) {
    this.container = container;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public PodSpec setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("description", description)
        .add("namespace", namespace)
        .add("container", container)
        .toString();
  }

  // DB methods

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("id");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(id);
  }

}
