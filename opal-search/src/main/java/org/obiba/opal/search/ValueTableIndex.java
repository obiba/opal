/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import java.util.Calendar;

import javax.annotation.Nonnull;

import org.obiba.magma.Timestamped;

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
  @Nonnull
  String getRequestPath();

  /**
   * Name of the index (which might not be exactly the table reference).
   *
   * @return
   */
  @Nonnull
  String getIndexName();

  /**
   * Returns true if the the lastUpdate timestamp of this index is more recent than that of its corresponding
   * {@code ValueTable}
   *
   * @return true when this index is up to date, false otherwise
   */
  boolean isUpToDate();

  /**
   * Returns true if the index was created using a Opal with an older version.
   *
   * @return
   */
  boolean requiresUpgrade();

  /**
   * Delete the index.
   */
  void delete();

  /**
   * For testing purposes, we need to be able to mock the current datetime
   *
   * @return the current date
   */
  @Nonnull
  Calendar now();
}
