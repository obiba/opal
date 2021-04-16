/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

public class PasswordException extends RuntimeException {

  private static final long serialVersionUID = -2373380700796847543L;

  public PasswordException(String msg) {
    super(msg);
  }

}
