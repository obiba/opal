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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;

import com.google.common.collect.Lists;

/**
 * A taxonomies is a set of vocabularies that allows to describe the attributes.
 */
public class Taxonomy extends AbstractTimestamped implements HasUniqueProperties {

  @NotNull
  @NotBlank
  private String name;

  private Map<Locale, String> titles = new HashMap<Locale, String>();

  private Map<Locale, String> descriptions = new HashMap<Locale, String>();

  private List<String> vocabularies;

  public Taxonomy() {
  }

  public Taxonomy(@NotNull String name) {
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

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  public Map<Locale, String> getTitles() {
    return titles;
  }

  public void setTitles(Map<Locale, String> titles) {
    this.titles = titles;
  }

  public Taxonomy addTitle(Locale locale, String title) {
    if(titles == null) titles = new HashMap<Locale, String>();
    titles.put(locale, title);
    return this;
  }

  public Map<Locale, String> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(Map<Locale, String> descriptions) {
    this.descriptions = descriptions;
  }

  public Taxonomy addDescription(Locale locale, String title) {
    if(descriptions == null) descriptions = new HashMap<Locale, String>();
    descriptions.put(locale, title);
    return this;
  }

  public boolean hasVocabularies() {
    return vocabularies != null && vocabularies.size() > 0;
  }

  public List<String> getVocabularies() {
    return vocabularies;
  }

  public void setVocabularies(List<String> vocabularies) {
    this.vocabularies = vocabularies;
  }

  public Taxonomy addVocabulary(String vocabulary) {
    if(vocabularies == null) vocabularies = new ArrayList<String>();
    if(!vocabularies.contains(vocabulary)) vocabularies.add(vocabulary);
    return this;
  }

  public Taxonomy removeVocabulary(String vocabulary) {
    if(vocabularies != null) vocabularies.remove(vocabulary);
    return this;
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

  public void renameVocabulary(String templateName, String name) {
    int pos = vocabularies.indexOf(templateName);

    if(pos >= 0) {
      vocabularies.set(pos, name);
    }
  }
}
