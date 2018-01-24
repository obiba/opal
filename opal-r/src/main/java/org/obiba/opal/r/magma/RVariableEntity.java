/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import org.obiba.magma.support.VariableEntityBean;

import javax.validation.constraints.NotNull;

class RVariableEntity extends VariableEntityBean {

  private String rEntityIdentifier;

  RVariableEntity(@NotNull String entityType, @NotNull String entityIdentifier) {
    super(entityType, normalizeId(entityIdentifier));
    this.rEntityIdentifier = entityIdentifier;
  }

  String getRIdentifier() {
    return rEntityIdentifier;
  }

  private static String normalizeId(String id) {
    return id.endsWith(".0") ?
        id.substring(0, id.lastIndexOf(".0")) : id;
  }
}
