/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.taxonomy;

import java.util.List;

import com.google.common.collect.Lists;

public class Term {

  private String name;

  private List<Term> terms;

  public Term(String name) {
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setTerms(List<Term> terms) {
    this.terms = terms;
  }

  public void add(Term term) {
    getTerms().add(term);
  }

  public boolean hasTerm(String termName) {
    for(Term t : getTerms()) {
      if(t.getName().equals(termName)) {
        return true;
      }
    }
    return false;
  }

  public void removeTerm(String termName) {
    Term term = null;
    for(Term t : getTerms()) {
      if(t.getName().equals(termName)) {
        term = t;
        break;
      }
    }
    if(term != null) {
      getTerms().remove(term);
    }
  }

  public List<Term> getTerms() {
    return terms == null ? terms = Lists.newArrayList() : terms;
  }
}
