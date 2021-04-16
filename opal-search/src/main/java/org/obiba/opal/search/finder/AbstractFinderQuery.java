/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.finder;

import org.obiba.magma.ValueTable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractFinderQuery {

  /**
   * Tables to search within
   */
  private final List<ValueTable> tableFilter = new ArrayList<>();

  public List<ValueTable> getTableFilter() {
    return tableFilter;
  }

}
