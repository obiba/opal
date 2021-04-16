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

public class PasswordTooShortException extends PasswordException {

  private static final long serialVersionUID = -3241347108574583631L;

  public PasswordTooShortException(int minSize) {
    super(String.format("Password is shorter than the required %d characters", minSize));
  }

}
