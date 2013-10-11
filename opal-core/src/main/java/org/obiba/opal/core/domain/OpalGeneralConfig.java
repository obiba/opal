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

import javax.annotation.Nonnull;

public class OpalGeneralConfig extends AbstractOrientDbTimestampedEntity {

  public static final String DEFAULT_NAME = "Opal";

  public static final String DEFAULT_LOCALE = "en";

  public static final String DEFAULT_CHARSET = "ISO-8859-1";

  @Nonnull
  private String name = DEFAULT_NAME;

  private List<String> locales = new ArrayList<String>();

  private String defaultCharacterSet = DEFAULT_CHARSET;

  @Nonnull
  public String getName() {
    return name;
  }

  public void setName(@Nonnull String name) {
    this.name = name;
  }

  public List<String> getLocales() {
    return locales;
  }

  public void setLocales(List<String> locales) {
    this.locales = locales;
  }

  public String getDefaultCharacterSet() {
    return defaultCharacterSet;
  }

  public void setDefaultCharacterSet(String defaultCharacterSet) {
    this.defaultCharacterSet = defaultCharacterSet;
  }
}
