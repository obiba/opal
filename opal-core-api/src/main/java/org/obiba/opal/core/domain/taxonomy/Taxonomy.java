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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * A taxonomies is a set of vocabularies that allows to describe the attributes.
 */
public class Taxonomy extends TaxonomyEntity {

  private static final long serialVersionUID = -1868460805324793471L;

  private String author;

  private String license;

  private List<Vocabulary> vocabularies;

  public Taxonomy() {
  }

  public Taxonomy(@NotNull String name) {
    setName(name);
  }

  public boolean hasAuthor() {
    return !Strings.isNullOrEmpty(author);
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getAuthor() {
    return author;
  }

  public boolean hasLicense() {
    return !Strings.isNullOrEmpty(license);
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public String getLicense() {
    return license;
  }

  public boolean hasVocabularies() {
    return vocabularies != null && vocabularies.size() > 0;
  }

  public List<Vocabulary> getVocabularies() {
    if(vocabularies == null) vocabularies = Lists.newArrayList();
    return vocabularies;
  }

  public void setVocabularies(List<Vocabulary> vocabularies) {
    this.vocabularies = vocabularies;
  }

  /**
   * Add or update a vocabulary (in the latter case, cannot be renamed).
   *
   * @param vocabulary
   * @return
   */
  public Taxonomy addVocabulary(Vocabulary vocabulary) {
    int idx = getVocabularies().indexOf(vocabulary);
    if(idx < 0) vocabularies.add(vocabulary);
    else vocabularies.set(idx, vocabulary);
    return this;
  }

  /**
   * Update the vocabulary (can be renamed).
   *
   * @param name
   * @param vocabulary
   * @return
   * @throws NoSuchVocabularyException
   */
  public Taxonomy updateVocabulary(String name, Vocabulary vocabulary) throws NoSuchVocabularyException {
    Vocabulary original = getVocabulary(name);
    int idx = vocabularies.indexOf(original);
    vocabularies.set(idx, vocabulary);
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
    if(found != null) {
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