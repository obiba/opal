/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.ws.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * {@code Content-Disposition} header values for a file name that may hold quotes, backslashes or non-ASCII characters:
 * a plain ASCII {@code filename} for old clients and the RFC 6266 {@code filename*} that browsers prefer.
 */
public final class ContentDisposition {

  private ContentDisposition() {}

  public static String attachment(String fileName) {
    return of("attachment", fileName);
  }

  public static String inline(String fileName) {
    return of("inline", fileName);
  }

  private static String of(String disposition, String fileName) {
    String name = fileName.replaceAll("[\\r\\n]", "");
    String ascii = name.replaceAll("[\"\\\\]", "").replaceAll("[^\\x20-\\x7E]", "_");
    String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A");
    return disposition + "; filename=\"" + ascii + "\"; filename*=UTF-8''" + encoded;
  }
}
