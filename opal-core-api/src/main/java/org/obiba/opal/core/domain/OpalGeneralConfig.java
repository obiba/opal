/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.hibernate.validator.constraints.NotBlank;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class OpalGeneralConfig extends AbstractTimestamped {

  public static final String DEFAULT_NAME = "Opal";

  public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  public static final String DEFAULT_CHARSET = "ISO-8859-1";

  @Nonnull
  @NotBlank
  private String name = DEFAULT_NAME;

  private List<Locale> locales = new ArrayList<Locale>();

  private String defaultCharacterSet = DEFAULT_CHARSET;

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  public List<Locale> getLocales() {
    return locales;
  }

  public List<String> getLocalesAsString() {
    return Lists.newArrayList(Iterables.transform(getLocales(), new Function<Locale, String>() {
      @Override
      public String apply(Locale locale) {
        return locale.getLanguage();
      }
    }));
  }

  public void setLocales(List<Locale> locales) {
    this.locales = locales;
  }

  public String getDefaultCharacterSet() {
    return defaultCharacterSet;
  }

  public void setDefaultCharacterSet(String defaultCharacterSet) {
    this.defaultCharacterSet = defaultCharacterSet;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    //noinspection SimplifiableIfStatement
    if(!(o instanceof OpalGeneralConfig)) return false;
    return name.equals(((OpalGeneralConfig) o).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
