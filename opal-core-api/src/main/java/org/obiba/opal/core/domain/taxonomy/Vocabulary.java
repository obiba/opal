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

import com.google.common.collect.Lists;

public class Vocabulary extends TaxonomyEntity {

  private boolean repeatable;

  private List<Term> terms;

  public Vocabulary() {
  }

  public Vocabulary(String name) {
    setName(name);
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public void setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
  }

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

  /**
   * Add or update a term (in the latter case, cannot be renamed).
   *
   * @param term
   * @return
   */
  public Vocabulary addTerm(Term term) {
    if(terms == null) terms = Lists.newArrayList();
    int idx = terms.indexOf(term);
    if(idx <= 0) terms.add(term);
    else terms.set(idx, term);
    return this;
  }

  /**
   * Update the term (can be renamed).
   *
   * @param name
   * @param term
   * @return
   * @throws NoSuchTermException
   */
  public Vocabulary updateTerm(String name, Term term) throws NoSuchTermException {
    Term original = getTerm(name);
    int idx = terms.indexOf(original);
    terms.set(idx, term);
    return this;
  }

  /**
   * Remove {@link org.obiba.opal.core.domain.taxonomy.Term} by name (ignore if not found).
   *
   * @param name
   */
  public void removeTerm(String name) {
    if(terms == null) return;
    Term found = null;
    for(Term term : terms) {
      if(term.getName().equals(name)) {
        found = term;
        break;
      }
    }
    if(found != null) {
      terms.remove(found);
    }
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Vocabulary)) return false;
    Vocabulary voc = (Vocabulary) o;
    return getName().equals(voc.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}