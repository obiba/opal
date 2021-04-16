/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.cfg;

public class NoSuchTaxonomyException extends RuntimeException {

  private static final long serialVersionUID = -2729149786583030898L;

  private final String taxonomyName;

  public NoSuchTaxonomyException(String taxonomyName) {
    super("No taxonomy exists with the specified name '" + taxonomyName + "'");
    this.taxonomyName = taxonomyName;
  }

  public String getTaxonomyName() {
    return taxonomyName;
  }
}
