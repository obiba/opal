package org.obiba.opal.web.gwt.app.client.administration.taxonomies.utils;

import org.obiba.opal.web.model.client.opal.TermDto;

import com.google.gwt.core.client.JsArray;

public class TermArrayUtils {
  private TermArrayUtils() {}

  public static TermDto findTerm(JsArray<TermDto> terms, String termName) {
    if(termName == null) return null;

    // find in child
    for(int i = 0; i < terms.length(); i++) {
      if(terms.get(i).getName().equals(termName)) {
        return terms.get(i);
      }

      // Find in child
      TermDto t = findTerm(terms.get(i).getTermsArray(), termName);
      if(t != null) {
        return t;
      }
    }

    return null;
  }

  public static TermDto findParent(TermDto parent, JsArray<TermDto> terms, TermDto termToFind) {
    // find in child
    for(int i = 0; i < terms.length(); i++) {
      if(terms.get(i).getName().equals(termToFind.getName())) {
        return parent;
      }

      // Find in child
      TermDto t = findParent(terms.get(i), terms.get(i).getTermsArray(), termToFind);
      if(t != null) {
        return t;
      }
    }

    return null;
  }
}