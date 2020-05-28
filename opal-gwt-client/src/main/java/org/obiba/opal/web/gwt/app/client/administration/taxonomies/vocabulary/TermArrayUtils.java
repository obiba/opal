/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.TermDto;

import com.google.gwt.core.client.JsArray;

public class TermArrayUtils {
  private TermArrayUtils() {}

  public static TermDto findTerm(JsArray<TermDto> terms, String termName) {
    if(termName == null) return null;

    JsArray<TermDto> termsArray = JsArrays.toSafeArray(terms);

    // find in child
    for(int i = 0; i < termsArray.length(); i++) {
      if(termsArray.get(i).getName().equals(termName)) {
        return termsArray.get(i);
      }
    }

    return null;
  }

  public static TermDto findParent(TermDto parent, JsArray<TermDto> terms, TermDto termToFind) {
    if(termToFind == null) return null;

    JsArray<TermDto> termsArray = JsArrays.toSafeArray(terms);

    // find in child
    for(int i = 0; i < termsArray.length(); i++) {
      if(termsArray.get(i).getName().equals(termToFind.getName())) {
        return parent;
      }
    }

    return null;
  }
}