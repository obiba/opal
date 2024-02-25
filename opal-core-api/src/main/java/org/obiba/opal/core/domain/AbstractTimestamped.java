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

import java.util.Date;

import javax.validation.constraints.NotNull;

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
