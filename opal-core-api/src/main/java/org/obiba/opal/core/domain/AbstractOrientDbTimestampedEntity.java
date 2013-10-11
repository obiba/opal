package org.obiba.opal.core.domain;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.persistence.Id;
import javax.persistence.Version;

import org.obiba.magma.datasource.hibernate.domain.Timestamped;

import com.google.common.base.Objects;

@SuppressWarnings("AssignmentToDateFieldFromParameter")
public abstract class AbstractOrientDbTimestampedEntity implements OrientDbEntity, Timestamped {

  @Id
  private String id;

  @Version
  private Integer version;

  @Nonnull
  private Date created = new Date();

  @Nonnull
  private Date updated;

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    return Objects.equal(id, ((AbstractOrientDbTimestampedEntity) obj).id);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Integer getVersion() {
    return version;
  }

  @Override
  public void setVersion(Integer version) {
    this.version = version;
  }

  @Override
  @Nonnull
  public Date getCreated() {
    return created;
  }

  public void setCreated(@Nonnull Date created) {
    this.created = created;
  }

  @Override
  @Nonnull
  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(@Nonnull Date updated) {
    this.updated = updated;
  }

}
