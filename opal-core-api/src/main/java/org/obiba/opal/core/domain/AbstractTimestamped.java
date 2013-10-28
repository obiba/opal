package org.obiba.opal.core.domain;

import java.util.Date;

import javax.annotation.Nonnull;

import org.obiba.magma.datasource.hibernate.domain.Timestamped;

@SuppressWarnings("AssignmentToDateFieldFromParameter")
public abstract class AbstractTimestamped implements Timestamped {

  @Nonnull
  private Date created = new Date();

  @Nonnull
  private Date updated;

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
