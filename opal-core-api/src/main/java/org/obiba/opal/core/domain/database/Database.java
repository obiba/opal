package org.obiba.opal.core.domain.database;

import javax.annotation.Nonnull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;

import com.google.common.base.Objects;

public abstract class Database extends AbstractTimestamped {

  public enum Usage {
    IMPORT, STORAGE, EXPORT
  }

  @Nonnull
  @NotBlank
  private String name;

  @Nonnull
  private Usage usage;

  private String description;

  /**
   * Flag to indicate if this database configuration can be modified:
   * if it has a Magma Datasource plugged on it, it won't be editable.
   */
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
  public Usage getUsage() {
    return usage;
  }

  public void setUsage(@Nonnull Usage usage) {
    this.usage = usage;
  }

  public boolean isUsedForIdentifiers() {
    return usedForIdentifiers;
  }

  public void setUsedForIdentifiers(boolean usedForIdentifiers) {
    this.usedForIdentifiers = usedForIdentifiers;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    //noinspection SimplifiableIfStatement
    if(obj == null || getClass() != obj.getClass()) return false;
    return Objects.equal(name, ((Database) obj).name);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("defaultStorage", defaultStorage).add("name", name).add("usage", usage)
        .add("description", description).add("editable", editable).add("usedForIdentifiers", usedForIdentifiers)
        .toString();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
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

    public TBuilder usage(Usage usage) {
      database.setUsage(usage);
      return builder;
    }

    public TBuilder usedForIdentifiers(boolean usedForIdentifiers) {
      database.setUsedForIdentifiers(usedForIdentifiers);
      return builder;
    }

  }

}
