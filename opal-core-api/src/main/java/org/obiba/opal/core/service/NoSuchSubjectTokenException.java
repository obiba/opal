/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

public class NoSuchSubjectTokenException extends RuntimeException {

  private final String token;

  public NoSuchSubjectTokenException(String token) {
    super("Subject token with identifier not found '" + token + "'.");
    this.token = token;
  }

  public String getToken() {
    return token;
  }
}
