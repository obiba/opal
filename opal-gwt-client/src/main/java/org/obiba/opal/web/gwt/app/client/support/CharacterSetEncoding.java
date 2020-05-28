/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

public enum CharacterSetEncoding {

  ISO_8859_1("ISO-8859-1"),
  ISO_8859_2("ISO-8859-2"),
  ISO_8859_3("ISO-8859-3"),
  ISO_8859_4("ISO-8859-4"),
  ISO_8859_5("ISO-8859-5"),
  ISO_8859_6("ISO-8859-6"),
  ISO_8859_7("ISO-8859-7"),
  ISO_8859_8("ISO-8859-8"),
  ISO_8859_9("ISO-8859-9"),
  ISO_8859_13("ISO-8859-13"),
  ISO_8859_15("ISO-8859-15"),
  UTF_8("UTF-8"),
  UTF_16("UTF-16"),
  UTF_32("UTF-32");

  private final String name;

  CharacterSetEncoding(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static boolean isValid(String encoding) {
    for(CharacterSetEncoding characterSetEncoding : values()) {
      if(characterSetEncoding.getName().equals(encoding)) return true;
    }
    return false;
  }

}
