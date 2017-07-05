/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.search;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public interface VariableSummaryHandler {

  void stackVariable(ValueTable valueTable, Variable variable, Value value);

  void computeSummaries(ValueTable valueTable);

  void clearComputingSummaries(ValueTable valueTable);

}
