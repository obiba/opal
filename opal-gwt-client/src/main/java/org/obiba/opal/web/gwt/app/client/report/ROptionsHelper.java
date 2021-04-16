/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.report;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class ROptionsHelper {

  private ROptionsHelper() {}

  public static String renderROptionValue(String key, String value) {
    String optValue = value;
    RegExp regExp = RegExp.compile("password|token");
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
