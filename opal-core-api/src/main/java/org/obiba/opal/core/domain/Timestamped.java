/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import java.util.Date;

/**
 * Implemented by some persisted entities to provide access to the update timestamp and the creation timestamp.
 */
public interface Timestamped {

  /**
   * Returns the timestamp for the creation this entity.
   *
   * @return The creation timestamp.
   */
  Date getCreated();

  /**
   * Returns the timestamp for the last update of this entity.
   *
   * @return The update timestamp.
   */
  Date getUpdated();

}