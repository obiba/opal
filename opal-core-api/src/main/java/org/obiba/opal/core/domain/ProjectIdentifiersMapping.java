package org.obiba.opal.core.domain;

public class ProjectIdentifiersMapping {
  private String entityType;
  private String name;
  private String mapping;

  ProjectIdentifiersMapping() {
  }

  public String getEntityType() {
    return entityType;
  }

  void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  public String getMapping() {
    return mapping;
  }

  void setMapping(String mapping) {
    this.mapping = mapping;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private ProjectIdentifiersMapping idMapping = new ProjectIdentifiersMapping();
    private Builder() {}

    public Builder entityType(String value) {
      idMapping.entityType = value;
      return this;
    }

    public Builder name(String value) {
      idMapping.name = value;
      return this;
    }

    public Builder mapping(String value) {
      idMapping.mapping = value;
      return this;
    }

    public ProjectIdentifiersMapping build() {
      return idMapping;
    }
  }
}
