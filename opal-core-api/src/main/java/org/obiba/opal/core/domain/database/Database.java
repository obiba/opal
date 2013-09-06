package org.obiba.opal.core.domain.database;

import javax.annotation.Nonnull;

import org.obiba.magma.datasource.hibernate.domain.AbstractTimestampedEntity;

import com.google.common.base.Objects;

@SuppressWarnings("ParameterHidesMemberVariable")
public abstract class Database extends AbstractTimestampedEntity {

  private static final long serialVersionUID = 7804325269326932874L;

  public enum Type {
    IMPORT, STORAGE, EXPORT
  }

  //TODO add timestamps

  @Nonnull
  //TODO unique
  private String name;

  @Nonnull
  private Type type;

  private String description;

  private boolean editable = true;

  private boolean defaultStorage;

  private boolean usedForIdentifiers;

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

  public boolean isUsedForIdentifiers() {
    return usedForIdentifiers;
  }

  public void setUsedForIdentifiers(boolean usedForIdentifiers) {
    this.usedForIdentifiers = usedForIdentifiers;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("defaultStorage", defaultStorage).add("name", name).add("type", type)
        .add("description", description).add("editable", editable).add("usedForIdentifiers", usedForIdentifiers)
        .toString();
  }

  public static abstract class Builder<TDatabase extends Database, TBuilder extends Builder<TDatabase, TBuilder>> {

    protected final TDatabase database;

    protected final TBuilder builder;

    protected Builder() {
      database = createDatabase();
      builder = createBuilder();
    }

    protected abstract TDatabase createDatabase();

    protected abstract TBuilder createBuilder();

    public TBuilder defaultStorage(boolean defaultStorage) {
      database.setDefaultStorage(defaultStorage);
      return builder;
    }

    public TBuilder description(String description) {
      database.setDescription(description);
      return builder;
    }

    public TBuilder editable(boolean editable) {
      database.setEditable(editable);
      return builder;
    }

    public TBuilder name(String name) {
      database.setName(name);
      return builder;
    }

    public TBuilder type(Type type) {
      database.setType(type);
      return builder;
    }

    public TBuilder usedForIdentifiers(boolean usedForIdentifiers) {
      database.setUsedForIdentifiers(usedForIdentifiers);
      return builder;
    }

  }

}
