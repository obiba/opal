/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support.finder;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.ValueTable;

/**
 *
 */
public class AccessFilterTablesFinder<TQuery extends AbstractQuery, TResult> extends AbstractFinder<TQuery, TResult> {

  @Override
  public void find(TQuery query, List<TResult> result) {
    // iterate on all tables for which user has at least TABLE_READ
//    query.getTableFilter().addAll(readableTables);
    next(query, result);
  }

}
