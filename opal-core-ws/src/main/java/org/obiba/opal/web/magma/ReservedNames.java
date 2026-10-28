/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.util.regex.Pattern;

/**
 * Table, view and resource names end up as one segment of a Shiro permission node ({@code rest:/datasource/ds/table/name/...:GET}),
 * where {@code :} separates the parts, {@code /} the segments, {@code ,} the alternatives and {@code *} is a wildcard. Names holding
 * those characters are rejected at the REST layer. A dot is fine: {@code ds.table} references split on the first dot and project
 * names cannot contain one.
 */
public final class ReservedNames {

  private static final Pattern RESERVED = Pattern.compile("[:/*,]");

  private ReservedNames() {}

  public static boolean isReserved(String name) {
    return name == null || name.isBlank() || "*".equals(name) || RESERVED.matcher(name).find();
  }
}
