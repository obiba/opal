/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.finder;

/**
 *
 */
public class AccessFilterTablesFinder<TQuery extends AbstractFinderQuery, TResult extends FinderResult<?>>
    extends AbstractFinder<TQuery, TResult> {

  @Override
  public void find(TQuery query, TResult result) {
    // TODO iterate on all tables for which user has at least TABLE_READ
//    query.getTableFilter().addAll(readableTables);
    next(query, result);
  }

}
