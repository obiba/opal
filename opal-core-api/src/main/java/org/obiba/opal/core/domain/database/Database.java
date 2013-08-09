package org.obiba.opal.core.domain.database;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.obiba.core.domain.AbstractEntity;

@MappedSuperclass
public abstract class Database extends AbstractEntity {

  private static final long serialVersionUID = 7804325269326932874L;

  public enum Type {
    IMPORT, STORAGE, EXPORT
  }

  @Nonnull
  @Column(nullable = false, unique = true)
  private String name;

  @Nonnull
  private Type type;

  private String description;

  private boolean editable;

  private boolean defaultStorage;

  public boolean isDefaultStorage() {
    return defaultStorage;
  }

  public void setDefaultStorage(boolean defaultStorage) {
    this.defaultStorage = defaultStorage;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public Type getType() {
    return type;
  }

  public void setType(@Nonnull Type type) {
    this.type = type;
  }
}
