/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.magma;

import org.obiba.magma.ValueTable;
import org.obiba.magma.views.WhereClause;

/**
 * {@link org.obiba.magma.views.WhereClause} that apply a where filter by issuing a query (on a table index).
 */
public interface QueryWhereClause extends WhereClause {

  void setValueTable(ValueTable valueTable);

  void setQuery(String query);

}
