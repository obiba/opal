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

import org.obiba.magma.ValueTable;

/**
 *
 */
public abstract class AbstractMagmaFinder<TQuery extends AbstractFinderQuery, TResult extends FinderResult<?>> extends
    AbstractFinder<TQuery, TResult> {

  public abstract Boolean executeQuery(TQuery query, TResult result);

  @Override
  public void find(TQuery query, TResult result) {
    executeQuery(query, result);
    next(query, result);
  }

}
