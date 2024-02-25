/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.identifiers;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;

public class IdentifiersMapping {

  @NotNull
  private final String name;

  @NotNull
  private final String entityType;

  public IdentifiersMapping(String name, String entityType) {
    if(entityType == null) throw new IllegalArgumentException("entityType cannot be null");
    if(name == null) throw new IllegalArgumentException("name cannot be null");
    if(name.trim().isEmpty()) throw new IllegalArgumentException("name cannot be empty");

    this.name = name;
    this.entityType = entityType;
  }

  public String getName() {
    return name;
  }

  public String getEntityType() {
    return entityType;
  }

  public boolean isForTable(ValueTable table) {
    return table.getEntityType().toLowerCase().equals(entityType.toLowerCase());
  }
}
