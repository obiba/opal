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

public class Vocabulary extends AbstractTimestamped implements HasUniqueProperties {

  @Nonnull
  @NotBlank
  private String taxonomy;

  @Nonnull
  @NotBlank
  private String name;

  private boolean repeatable;

  private Map<Locale, String> titles = new HashMap<Locale, String>();

  private Map<Locale, String> descriptions = new HashMap<Locale, String>();

  private List<Term> terms = new ArrayList<Term>();

  public Vocabulary() {
  }

  public Vocabulary(@Nonnull String taxonomy, @Nonnull String name) {
    this.taxonomy = taxonomy;
    this.name = name;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("taxonomy", "name");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(taxonomy, name);
  }

  @Nonnull
  public String getTaxonomy() {
    return taxonomy;
  }

  public void setTaxonomy(@Nonnull String taxonomy) {
    this.taxonomy = taxonomy;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public void setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
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

  public List<Term> getTerms() {
    return terms;
  }

  public void setTerms(List<Term> terms) {
    this.terms = terms;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Vocabulary)) return false;
    Vocabulary that = (Vocabulary) o;
    return name.equals(that.name) && taxonomy.equals(that.taxonomy);
  }

  @Override
  public int hashCode() {
    int result = taxonomy.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
