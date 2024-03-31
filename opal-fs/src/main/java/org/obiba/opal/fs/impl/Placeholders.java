/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.fs.impl;

import java.util.Map.Entry;
import java.util.Properties;

import jakarta.annotation.Nullable;

final class Placeholders {

  private static final String PREFIX = "${";

  private static final String POSTFIX = "}";

  private Placeholders() {}

  @Nullable
  public static String replaceAll(String value) {
    if(value == null || value.isEmpty()) return value;
    String replaced = value;
    Properties sysProps = System.getProperties();
    for(Entry<Object, Object> prop : sysProps.entrySet()) {
      replaced = replaced.replace(PREFIX + prop.getKey() + POSTFIX, prop.getValue().toString());
    }
    return replaced;
  }
}
