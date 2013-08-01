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

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

/**
 * A taxonomy is a set of vocabularies that allows to describe the attributes.
 */
public class Taxonomy {

  @Nullable
  private String name;

  private List<Vocabulary> vocabularies;

  public Taxonomy(@Nullable String name) {
    this.name = name;
  }

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(@Nullable String name) {
    this.name = name;
  }

  public List<Vocabulary> getVocabularies() {
    return vocabularies == null ? vocabularies = Lists.newArrayList() : vocabularies;
  }

  public void add(Vocabulary vocabulary) {
    getVocabularies().add(vocabulary);
  }

  public void removeVocabulary(String vocabularyName) {
    Vocabulary vocabulary = null;
    for(Vocabulary v : getVocabularies()) {
      if(v.getName().equals(vocabularyName)) {
        vocabulary = v;
        break;
      }
    }
    if(vocabulary != null) {
      getVocabularies().remove(vocabulary);
    }
  }

  public boolean hasVocabulary(String vocabularyName) {
    for(Vocabulary v : getVocabularies()) {
      if(v.getName().equals(vocabularyName)) {
        return true;
      }
    }
    return false;
  }

  public void setVocabularies(List<Vocabulary> vocabularies) {
    this.vocabularies = vocabularies;
  }
}
