/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search.support;

/**
 * Criterion for filtering values based on a variable field.
 */
public interface ValueSetVariableCriterionParser {

  /**
   * Get the query as a child query.
   *
   * @return
   */
  QuerySearchJsonBuilder.ChildQuery asChildQuery();

  /**
   * Get the query that was parsed.
   *
   * @return
   */
  String getOriginalQuery();
}
