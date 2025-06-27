package org.obiba.opal.core.domain.kubernetes;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.opal.core.domain.HasUniqueProperties;

import java.util.List;
import java.util.Map;

public class PodSpec implements HasUniqueProperties {

  private String id = "1";

  private String type = "rock";

  private String description = "";

  private String namespace;

  private Map<String, String> labels;

  private Container container;

  private String nodeName;

  private Map<String, String> nodeSelector;

  private List<Toleration> tolerations;

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

  public Map<String, String> getLabels() {
    return labels == null ? labels = Maps.newHashMap() : labels;
  }

  public PodSpec setLabels(Map<String, String> labels) {
    this.labels = labels;
    return this;
  }

  public Container getContainer() {
    return container;
  }

  public PodSpec setContainer(Container container) {
    this.container = container;
    return this;
  }

  public boolean hasNodeName() {
    return !Strings.isNullOrEmpty(nodeName);
  }

  public String getNodeName() {
    return nodeName;
  }

  public PodSpec setNodeName(String nodeName) {
    this.nodeName = nodeName;
    return this;
  }

  public Map<String, String> getNodeSelector() {
    return nodeSelector == null ? nodeSelector = Maps.newHashMap() : nodeSelector;
  }

  public PodSpec setNodeSelector(Map<String, String> nodeSelector) {
    this.nodeSelector = nodeSelector;
    return this;
  }

  public List<Toleration> getTolerations() {
    return tolerations == null ? tolerations = Lists.newArrayList() : tolerations;
  }

  public PodSpec setTolerations(List<Toleration> tolerations) {
    this.tolerations = tolerations;
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
