/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.support;

public class AclResourceTokenizer {

  private static final int PROJECT_INDEX = 2;

  private static final int TABLE_INDEX = 4;

  private static final int VARIABLE_INDEX = 6;

  private final String[] tokens;

  public enum ResourceTokens {
    PROJECT,
    DATASOURCE,
    TABLE,
    VARIABLE,
    REPORTTEMPLATE,
    VCFSTORE
  }

  public AclResourceTokenizer(String resource) {
    tokens = resource.split("/");
  }

  public String getToken(ResourceTokens token) {
    switch(token) {
      case PROJECT:
      case VCFSTORE:
      case DATASOURCE:
        return tokens[PROJECT_INDEX];
      case TABLE:
      case REPORTTEMPLATE:
        return tokens[TABLE_INDEX];
      case VARIABLE:
        return tokens[VARIABLE_INDEX];
      default:
        return "";
    }
  }
}
