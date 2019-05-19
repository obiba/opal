/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Response;

public final class ErrorResponseMessageBuilder {

  private final Response httpResponse;

  private String defaultMessage;

  private ErrorResponseMessageBuilder(Response response) {
    httpResponse = response;
  }

  public static ErrorResponseMessageBuilder get(Response response) {
    return new ErrorResponseMessageBuilder(response);
  }

  public ErrorResponseMessageBuilder withDefaultMessage(String message) {
    defaultMessage = message;
    return this;
  }
  
  public String build() {
    return ClientErrorDtoMessageBuilder.get(getClientDtoError()).withdefaultMessage(getDefaultMessage()).build();
  }

  private ClientErrorDto getClientDtoError() {

    if(hasResponseText()) {
      try {
        return JsonUtils.unsafeEval(httpResponse.getText());
      } catch(IllegalArgumentException e) {
        // a NULL will be return to get the default message
      }
    }

    return null;
  }

  private boolean hasResponseText() {
    return httpResponse != null && !httpResponse.getText().isEmpty();
  }

  private String getDefaultMessage() {
    String message = defaultMessage;

    if(hasResponseText()) {
      message += ": " + httpResponse.getText();
    }

    return message;
  }
}
