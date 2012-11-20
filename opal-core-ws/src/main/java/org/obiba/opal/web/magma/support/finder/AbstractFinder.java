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

import java.util.List;

import org.obiba.magma.ValueTable;

/**
 *
 */
public abstract class AbstractFinder<TQuery extends AbstractQuery, TResult> {

  private AbstractFinder<TQuery, TResult> nextFinder;

  public abstract void find(TQuery query, List<TResult> result);

  public void next(TQuery query, List<TResult> result) {
    if(nextFinder != null) nextFinder.find(query, result);
  }

  public void setNextFinder(AbstractFinder<TQuery, TResult> nextFinder) {
    this.nextFinder = nextFinder;
  }

  public AbstractFinder<TQuery, TResult> nextFinder(AbstractFinder<TQuery, TResult> next) {
    setNextFinder(next);
    return next;
  }
}
