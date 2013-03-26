/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.support;

import java.util.ArrayList;
import java.util.Collection;

public enum LanguageLocale {
  AR("ar"),
  BE("be"),
  BG("bg"),
  CA("ca"),
  CS("cs"),
  DA("da"),
  DE("de"),
  EL("el"),
  EN("en"),
  ES("es"),
  ET("et"),
  FI("fi"),
  FR("fr"),
  GA("ga"),
  HI("hi"),
  HR("hr"),
  HU("hu"),
  IN("in"),
  IS("is"),
  IT("it"),
  IW("iw"),
  JA("ja"),
  KO("ko"),
  LT("lt"),
  LV("lv"),
  MK("mk"),
  MS("ms"),
  MT("mt"),
  NL("nl"),
  NO("no"),
  PL("pl"),
  PT("pt"),
  RO("ro"),
  RU("ru"),
  SK("sk"),
  SL("sl"),
  SQ("sq"),
  SR("sr"),
  SV("sv"),
  TH("th"),
  TR("tr"),
  UK("uk"),
  VI("vi"),
  ZH("zh");

  public static Iterable<String> getAllLocales() {
    return localeNames;
  }

  public String getName() {
    return name;
  }

  public static boolean isValid(String localeName) {
    return localeNames.contains(localeName);
  }


  //
  // Private Members
  //

  private final static Collection<String> localeNames = new ArrayList<String>();

  static {
    for(LanguageLocale locale : LanguageLocale.values()) {
      localeNames.add(locale.name);
    }
  }

  private final String name;

  LanguageLocale(String name) {
    this.name = name;
  }
}
