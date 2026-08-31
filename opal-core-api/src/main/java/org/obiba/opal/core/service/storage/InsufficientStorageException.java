/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.storage;

/**
 * Thrown when a write is refused because the volume it would land on is too close to full. Refusing is the point:
 * H2 does not fail an individual statement when the disk fills, it panics and closes the store without persisting, so
 * the only cheap outcome is the one that never gets there.
 */
public class InsufficientStorageException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InsufficientStorageException(String message) {
    super(message);
  }
}
