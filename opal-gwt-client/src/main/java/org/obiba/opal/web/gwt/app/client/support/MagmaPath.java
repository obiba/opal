package org.obiba.opal.web.gwt.app.client.support;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class MagmaPath {

  public static final String PATH_SEPARATOR = ".";
  public static final String VARIABLE_PATH_SEPARATOR = ":";

  public static class Builder {
    private String datsourceName;
    private String tableName;
    private String variableName;

    public Builder setDatasourceName(String value) {
      datsourceName = value;
      return this;
    }

    public Builder setTableName(String value) {
      tableName = value;
      return this;
    }

    public Builder setVariableName(String value) {
      variableName = value;
      return this;
    }

    public String build() {
      return datsourceName + (!Strings.isNullOrEmpty(tableName) ? PATH_SEPARATOR + tableName : "") +
          (!Strings.isNullOrEmpty(variableName) ? VARIABLE_PATH_SEPARATOR + variableName : "");
    }
  }

  public static class Parser {
    private String dataSourcename;
    private String tableName;
    private String variableName;

    public Parser parse(String path) {
      RegExp pattern = RegExp.compile("(\\w+)[\\.]*(\\w*)[\\:]*(\\w*)");
      MatchResult matcher = pattern.exec(path);

      dataSourcename = matcher.getGroup(1);
      tableName = matcher.getGroup(2);
      variableName = matcher.getGroup(3);

      return this;
    }

    public String getDatasourceName() {
      return dataSourcename;
    }

    public String getTableName() {
      return tableName;
    }

    public String getVariableName() {
      return variableName;
    }

  }

}
