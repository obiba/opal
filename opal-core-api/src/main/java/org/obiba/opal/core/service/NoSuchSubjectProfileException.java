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

public class NoSuchSubjectProfileException extends RuntimeException {

  private static final long serialVersionUID = 8076327259073208897L;

  private final String principal;

  public NoSuchSubjectProfileException(String principal) {
    super("Subject profile not found for principal '" + principal + "'.");
    this.principal = principal;
  }

  public String getPrincipal() {
    return principal;
  }
}
