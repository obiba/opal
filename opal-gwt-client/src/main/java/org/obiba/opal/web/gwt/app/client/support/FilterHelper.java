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


import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import java.util.List;

public class FilterHelper {

  /**
   * Split query as normalized tokens (trim, lower case).
   *
   * @param query
   * @return
   */
  public static List<String> tokenize(String query) {
    List<String> tokens = Lists.newArrayList();
    if (com.google.common.base.Strings.isNullOrEmpty(query)) return tokens;
    for (String token : Splitter.on(" ").splitToList(query.toLowerCase())) {
      String nToken = token.trim();
      if (!nToken.isEmpty()) tokens.add(nToken);
    }
    return tokens;
  }

  /**
   * True in text matches all tokens (token negation also supported (starts with '-')).
   *
   * @param text
   * @param tokens
   * @return
   */
  public static boolean matches(String text, List<String> tokens) {
    String nText = text.toLowerCase();
    for (String token : tokens) {
      boolean not = false;
      String nQuery = token;
      if (token.startsWith("-")) {
        not = true;
        nQuery = token.substring(1);
      }
      if (!nQuery.isEmpty()) {
        if (not) {
          if (nText.contains(nQuery)) return false;
        } else if (!nText.contains(nQuery)) return false;
      }
    }
    return true;
  }

  public static boolean labelMatches(JsArray<AttributeDto> attributes, List<String> tokens) {
    return attributeMatches("label", attributes, tokens);
  }

  public static boolean attributeMatches(String name, JsArray<AttributeDto> attributes, List<String> tokens) {
    for (AttributeDto attr : JsArrays.toIterable(attributes)) {
      if (attr.getName().equals(name) && matches(attr.getValue(), tokens)) return true;
    }
    return false;
  }
}
