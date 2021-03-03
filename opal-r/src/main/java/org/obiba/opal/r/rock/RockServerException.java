/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import com.google.common.base.Strings;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.opal.spi.r.RServerException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public class RockServerException extends RServerException {

  private final boolean clientError;

  private String causeMessage;

  RockServerException(String message) {
    super(message);
    this.clientError = false;
  }

  RockServerException(String message, RestClientException e) {
    super(message);
    if (e instanceof RestClientResponseException) {
      RestClientResponseException re = (RestClientResponseException) e;
      this.clientError = re.getRawStatusCode()<500;
      try {
        JSONObject error = new JSONObject(re.getResponseBodyAsString());
        this.causeMessage = error.getString("message");
      } catch (JSONException je) {
        this.causeMessage = re.getStatusText();
      }
    } else
      this.clientError = false;
  }

  @Override
  public boolean isClientError() {
    return clientError;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + (Strings.isNullOrEmpty(causeMessage) ? "" : " -> " + causeMessage);
  }
}
