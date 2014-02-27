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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Response;

public final class UnhandledResponseEventMessageBuilder {

  private static final Translations translations = GWT.create(Translations.class);
  private static UnhandledResponseEvent unhandledResponseEvent;

  public UnhandledResponseEventMessageBuilder(UnhandledResponseEvent event) {
    unhandledResponseEvent = event;
  }

  public static UnhandledResponseEventMessageBuilder get(UnhandledResponseEvent event) {
    return new UnhandledResponseEventMessageBuilder(event);
  }
  
  public String build() {
    String message = getClientErrorMessage();
    return (message == null) ? getDefaultMessage() : message;
  }

  private String getClientErrorMessage() {

    Response response = unhandledResponseEvent.getResponse();

    if(response != null && !response.getText().isEmpty()) {
      try {
        ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());

        if (errorDto != null) {
          String messageKey = errorDto.getStatus();
          assert translations.userMessageMap().containsKey(messageKey);
          return TranslationsUtils
              .replaceArguments(translations.userMessageMap().get(messageKey), errorDto.getArgumentsArray());
        }
      } catch(IllegalArgumentException e) {
        // a NULL will be return to get the default message
      }
    }

    return null;
  }
  
  private String getDefaultMessage() {

    String message = unhandledResponseEvent.getShortMessage();

    if(!unhandledResponseEvent.getResponse().getText().isEmpty()) {
      message += ": " + unhandledResponseEvent.getResponse().getText();
    }

    return message;
  }
}
