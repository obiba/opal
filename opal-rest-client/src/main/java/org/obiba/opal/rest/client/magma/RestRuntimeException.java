/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.rest.client.magma;

import java.net.URI;

import org.apache.http.StatusLine;
import org.obiba.magma.MagmaRuntimeException;

/**
 * .
 */
public class RestRuntimeException extends MagmaRuntimeException {

  private final URI uri;

  private final int statusCode;

  private final String reasonPhrase;

  public RestRuntimeException(URI uri, StatusLine statusLine) {
    this(uri, statusLine.getStatusCode(), statusLine.getReasonPhrase());
  }

  public RestRuntimeException(URI uri, int statusCode, String reasonPhrase) {
    super(reasonPhrase + "[" + statusCode + ", " + uri + "]");
    this.uri = uri;
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
  }

  public URI getUri() {
    return uri;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }
}
