/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@SuppressWarnings("AssignmentToDateFieldFromParameter")
@MappedSuperclass
public abstract class AbstractTimestamped implements Timestamped {

  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date created = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  /**
   * Keeps the timestamps up to date on the way to the database, which is what the OrientDB TimestampedHook did for
   * every document written. Unlike that hook, this does not overwrite a creation date the object already carries: the
   * field defaults to now for a new object, and a row being migrated from the old store has to keep the date it was
   * actually created on.
   */
  @PrePersist
  void onPersist() {
    if(created == null) created = new Date();
    if(updated == null) updated = created;
  }

  @PreUpdate
  void onUpdate() {
    updated = new Date();
  }

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
