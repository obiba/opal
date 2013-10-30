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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;

import com.google.common.collect.Lists;

/**
 * A taxonomies is a set of vocabularies that allows to describe the attributes.
 */
public class Taxonomy extends AbstractTimestamped implements HasUniqueProperties {

  @Nonnull
  @NotBlank
  private String name;

  private Map<Locale, String> titles = new HashMap<Locale, String>();

  private Map<Locale, String> descriptions = new HashMap<Locale, String>();

  private List<String> vocabularies;

  public Taxonomy() {
  }

  public Taxonomy(@Nonnull String name) {
    this.name = name;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(name);
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  public Map<Locale, String> getTitles() {
    return titles;
  }

  public void setTitles(Map<Locale, String> titles) {
    this.titles = titles;
  }

  public Map<Locale, String> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(Map<Locale, String> descriptions) {
    this.descriptions = descriptions;
  }

  public List<String> getVocabularies() {
    return vocabularies;
  }

  public void setVocabularies(List<String> vocabularies) {
    this.vocabularies = vocabularies;
  }

  public void addVocabulary(String vocabulary) {
    if(vocabularies == null) vocabularies = new ArrayList<String>();
    if(!vocabularies.contains(vocabulary)) vocabularies.add(vocabulary);
  }

  public void removeVocabulary(String vocabulary) {
    if(vocabularies != null) vocabularies.remove(vocabulary);
  }

  public boolean hasVocabulary(String vocabulary) {
    return vocabularies != null && vocabularies.contains(vocabulary);
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Taxonomy)) return false;
    Taxonomy taxonomy = (Taxonomy) o;
    return name.equals(taxonomy.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
