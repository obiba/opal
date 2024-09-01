/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.service;

import org.obiba.magma.Timestamped;

import java.util.Calendar;

/**
 * An index of a {@code ValueTable}
 */
public interface ValueTableIndex extends Timestamped {

  /**
   * The full path of this index to make requests.
   *
   * @return
   */
  // TODO: this should be hidden in the implementation. Probably we should expose some sort of Request/Response api?
  String getIndexName();

  /**
   * Type in the index (which might not be exactly the table reference).
   *
   * @return
   */
  String getIndexType();

  String getValueTableReference();

  /**
   * Returns true if the lastUpdate timestamp of this index is more recent than that of its corresponding
   * {@code ValueTable}
   *
   * @return true when this index is up to date, false otherwise
   */
  boolean isUpToDate();

  /**
   * Check whether the table was indexed.
   *
   * @return
   */
  boolean exists();

  /**
   * Delete the index.
   */
  void delete();

  /**
   * For testing purposes, we need to be able to mock the current datetime
   *
   * @return the current date
   */
  Calendar now();
}
