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

public class PasswordTooWeakException extends PasswordException {

  public PasswordTooWeakException() {
    super("Password is too weak: must contain at least one digit, one upper case alphabet, one lower case alphabet, one special character (which includes @#$%^&+=!) and no white space");
  }

}
