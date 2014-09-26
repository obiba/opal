/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.taxonomy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class TaxonomyEntity {

  @NotNull
  private String name;

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  private Map<String, String> title = new HashMap<>();

  private Map<String, String> description = new HashMap<>();

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

}
