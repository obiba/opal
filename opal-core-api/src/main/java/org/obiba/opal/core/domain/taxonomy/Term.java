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

import javax.validation.constraints.NotNull;

import org.obiba.opal.core.cfg.NoSuchTermException;

public class Term extends TaxonomyEntity {

  private static final long serialVersionUID = -6205259721540166478L;

  private List<Term> terms;

  public Term() {}

  public Term(String name) { setName(name); }

  public List<Term> getTerms() {
    return terms;
  }

  public void setTerms(List<Term> terms) {
    this.terms = terms;
  }

  public boolean hasTerms() {
    return terms != null && terms.size() > 0;
  }

  public boolean hasTerm(String name) {
    if (!hasTerms()) return false;
    for(Term term : terms) {
      if (term.getName().equals(name)) return true;
    }
    return false;
  }

  public Term getTerm(@NotNull String name) {
    if(terms == null) throw new NoSuchTermException(getName(), name);
    for(Term term : terms) {
      if(term.getName().equals(name)) return term;
    }
    throw new NoSuchTermException(getName(), name);
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Term)) return false;
    Term term = (Term) o;
    return getName().equals(term.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}
