/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search.support;


public class RQLIdentifierCriterionParser extends RQLCriterionParser {

  private static final String ID_FIELD = "identifier";

  public RQLIdentifierCriterionParser(String rqlQuery) {
    super(rqlQuery);
  }

  @Override
  protected String parseField(String path) {
    if (!ID_FIELD.equals(path)) throw new IllegalArgumentException("ID field is expected to be '" + ID_FIELD + "'");
    return super.parseField(path);
  }

}
