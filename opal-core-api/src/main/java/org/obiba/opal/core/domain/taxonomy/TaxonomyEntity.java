/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.taxonomy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class TaxonomyEntity implements Serializable {

  private static final long serialVersionUID = 276565753582045864L;

  @NotNull
  private String name;

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "name");

    this.name = name;
  }

  private Map<String, String> title = new HashMap<>();

  private Map<String, String> description = new HashMap<>();

  private Map<String, String> keywords = new HashMap<>();

  private Map<String, String> attributes = new HashMap<>();

  public Map<String, String> getTitle() {
    return title;
  }

  public void setTitle(Map<String, String> titles) {
    this.title = titles;
  }

  public TaxonomyEntity addTitle(Locale locale, String value) {
    if(title == null) title = new HashMap<>();
    title.put(locale.toLanguageTag(), value);
    return this;
  }

  public Map<String, String> getDescription() {
    return description;
  }

  public void setDescription(Map<String, String> descriptions) {
    this.description = descriptions;
  }

  public TaxonomyEntity addDescription(Locale locale, String value) {
    if(description == null) description = new HashMap<>();
    description.put(locale.toLanguageTag(), value);
    return this;
  }

  public void setKeywords(Map<String, String> keywords) {
    this.keywords = keywords;
  }

  public TaxonomyEntity addKeyword(Locale locale, String value) {
    if(keywords == null) keywords = new HashMap<>();
    keywords.put(locale.toLanguageTag(), value);
    return this;
  }

  public Map<String, String> getKeywords() {
    return keywords;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public TaxonomyEntity addAttribute(String key, String value) {
    if(attributes == null) attributes = new HashMap<>();
    attributes.put(key, value);
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  /**
   * Get the attribute value if exists.
   *
   * @return null if there is no such attribute key entry.
   */
  public String getAttributeValue(String key) {
    if (attributes == null || attributes.isEmpty()) return null;
    return attributes.get(key);
  }

}
