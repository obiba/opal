/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.i18n;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;

import com.google.gwt.core.client.JsArrayString;

/**
 *
 */
public class TranslationsUtils {

  public static String replaceArguments(String msg, List<String> args) {
    String message = msg;
    if(args != null) {
      for(int i = 0; i < args.size(); i++) {
        message = message.replace("{" + i + "}", args.get(i));
      }
    }
    return message;
  }

  public static String replaceArguments(String msg, JsArrayString args) {
    return replaceArguments(msg, JsArrays.toList(args));
  }

}
