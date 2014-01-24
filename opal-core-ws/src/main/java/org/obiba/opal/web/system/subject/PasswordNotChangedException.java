/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.subject;

public class PasswordNotChangedException extends PasswordException {

  private static final long serialVersionUID = -4112553326905000542L;

  public PasswordNotChangedException() {
    super("New password is identical to the current password");
  }

}
