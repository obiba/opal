/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.database;

public class InvalidH2DatabaseException extends RuntimeException {

  private static final long serialVersionUID = 5361391812021462923L;

  public InvalidH2DatabaseException(String message) {
    super(message);
  }
}
