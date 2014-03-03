package org.obiba.opal.web.gwt.app.client.report;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class ROptionsHelper {

  private ROptionsHelper() {}

  public static String renderROptionValue(String key, String value) {
    String optValue = value;
    RegExp regExp = RegExp.compile("password");
    MatchResult matcher = regExp.exec(key);
    if(matcher != null) {
      return "******";
    }
    regExp = RegExp.compile("^T$|^TRUE$|^F$|^FALSE$|^NULL$");
    matcher = regExp.exec(optValue);
    if(matcher == null) {
      optValue = "\"" + optValue + "\"";
    }

    return optValue;
  }

}
