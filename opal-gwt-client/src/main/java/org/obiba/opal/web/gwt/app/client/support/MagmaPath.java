/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class MagmaPath {

  public static class Builder {
    private String datasourceName;
    private String tableName;
    private String variableName;

    public static Builder datasource(String value) {
      Builder b = new Builder();
      b.datasourceName = value;
      return b;
    }

    public Builder table(String value) {
      tableName = value;
      return this;
    }

    public Builder variable(String value) {
      variableName = value;
      return this;
    }

    public String build() {
      String path = datasourceName;
      if (!Strings.isNullOrEmpty(tableName)) {
        path = path + "." + tableName;
        if (!Strings.isNullOrEmpty(variableName)) {
          path = path + ":" + variableName;
        }
      }
      return path;
    }
  }

  public static class Parser {
    private String datasourceName;
    private String tableName;
    private String variableName;

    public static Parser parse(String path) {
      Parser parser = new Parser();
      int tableSep = path.indexOf('.');
      if (tableSep == -1) {
        parser.datasourceName = path;
      } else {
        parser.datasourceName = path.substring(0, tableSep);
        String p = path.substring(tableSep + 1);
        int varSep = p.indexOf(':');
        if (varSep == -1) {
          parser.tableName = p;
        } else {
          parser.tableName = p.substring(0,varSep);
          parser.variableName = p.substring(varSep + 1);
        }
      }

      return parser;
    }

    public String getDatasource() {
      return datasourceName;
    }

    public boolean hasTable() {
      return tableName != null;
    }

    public String getTable() {
      return tableName;
    }

    public boolean hasVariable() {
      return variableName != null;
    }

    public String getVariable() {
      return variableName;
    }

    public String getTableReference() {
      return datasourceName + "." + tableName;
    }
  }

}
