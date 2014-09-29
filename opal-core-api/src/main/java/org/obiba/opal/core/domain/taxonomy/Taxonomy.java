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

import org.obiba.opal.core.cfg.NoSuchVocabularyException;

import com.google.common.collect.Lists;

/**
 * A taxonomies is a set of vocabularies that allows to describe the attributes.
 */
public class Taxonomy extends TaxonomyEntity {

  private List<Vocabulary> vocabularies;

  public Taxonomy() {
  }

  public Taxonomy(@NotNull String name) {
    setName(name);
  }

  public boolean hasVocabularies() {
    return vocabularies != null && vocabularies.size() > 0;
  }

  public List<Vocabulary> getVocabularies() {
    return vocabularies;
  }

  public void setVocabularies(List<Vocabulary> vocabularies) {
    this.vocabularies = vocabularies;
  }

  public Taxonomy addVocabulary(Vocabulary vocabulary) {
    if(vocabularies == null) vocabularies = Lists.newArrayList();
    int idx = vocabularies.indexOf(vocabulary);
    if(idx < 0) vocabularies.add(vocabulary);
    else vocabularies.set(idx, vocabulary);
    return this;
  }

  public void removeVocabulary(String name) {
    if(vocabularies == null) return;
    Vocabulary found = null;
    for(Vocabulary vocabulary : vocabularies) {
      if(vocabulary.getName().equals(name)) {
        found = vocabulary;
        break;
      }
    }
    if (found != null) {
      vocabularies.remove(found);
    }
  }

  public boolean hasVocabulary(String name) {
    if(vocabularies == null) return false;
    for(Vocabulary vocabulary : vocabularies) {
      if(vocabulary.getName().equals(name)) return true;
    }
    return false;
  }

  public Vocabulary getVocabulary(@NotNull String name) throws NoSuchVocabularyException {
    if(vocabularies == null) throw new NoSuchVocabularyException(getName(), name);
    for(Vocabulary vocabulary : vocabularies) {
      if(vocabulary.getName().equals(name)) return vocabulary;
    }
    throw new NoSuchVocabularyException(getName(), name);
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Taxonomy)) return false;
    Taxonomy taxonomy = (Taxonomy) o;
    return getName().equals(taxonomy.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

}