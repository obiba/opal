/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.finder;

/**
 *
 */
public abstract class AbstractFinder<TQuery extends AbstractFinderQuery, TResult extends FinderResult<?>> {

  private AbstractFinder<TQuery, TResult> nextFinder;

  private int limit;

  public abstract void find(TQuery query, TResult result);

  public void next(TQuery query, TResult result) {
    if(nextFinder != null) nextFinder.find(query, result);
  }

  public void setNextFinder(AbstractFinder<TQuery, TResult> nextFinder) {
    this.nextFinder = nextFinder;
  }

  public AbstractFinder<TQuery, TResult> nextFinder(AbstractFinder<TQuery, TResult> next) {
    setNextFinder(next);
    return next;
  }

  public AbstractFinder<TQuery, TResult> withLimit(@SuppressWarnings("ParameterHidesMemberVariable") int limit) {
    this.limit = limit;
    return this;
  }

  public int getLimit() {
    return limit;
  }
}
