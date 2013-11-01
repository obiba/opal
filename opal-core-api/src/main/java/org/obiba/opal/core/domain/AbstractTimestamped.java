package org.obiba.opal.core.domain;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.obiba.magma.datasource.hibernate.domain.Timestamped;

@SuppressWarnings("AssignmentToDateFieldFromParameter")
public abstract class AbstractTimestamped implements Timestamped {

  @NotNull
  private Date created = new Date();

  private Date updated;

  @Override
  @NotNull
  public Date getCreated() {
    return created;
  }

  public void setCreated(@NotNull Date created) {
    this.created = created;
  }

  @Override
  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(@NotNull Date updated) {
    this.updated = updated;
  }

}
