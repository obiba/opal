/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.spi.r.RServerResult;

import java.util.Map;

public interface TibbleTable extends ValueTable {

  String getSymbol();

  RServerResult execute(String script);

  RVariableEntity getRVariableEntity(VariableEntity entity);

  String getIdColumn();

  boolean isMultilines();

  String getDefaultLocale();

  int getIdPosition();

  Map<String, Integer> getColumnPositions();
}
