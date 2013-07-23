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

    public Parser parse(String path) {
      GWT.log("parsing: " + path);

      int tableSep = path.indexOf('.');
      if (tableSep == -1) {
        datasourceName = path;
      } else {
        datasourceName = path.substring(0, tableSep);
        String p = path.substring(tableSep + 1);
        int varSep = p.indexOf(':');
        if (varSep == -1) {
          tableName = p;
        } else {
          tableName = p.substring(0,varSep);
          variableName = p.substring(varSep + 1);
        }
      }

      return this;
    }

    public String getDatasourceName() {
      return datasourceName;
    }

    public String getTableName() {
      return tableName;
    }

    public String getVariableName() {
      return variableName;
    }

  }

}
