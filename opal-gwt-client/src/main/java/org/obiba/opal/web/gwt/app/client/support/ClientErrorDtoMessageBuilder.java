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
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;

public class ClientErrorDtoMessageBuilder {

  private static final Translations translations = GWT.create(Translations.class);

  private static ClientErrorDto clientErrorDto;

  private static String defaultMessage;

  private ClientErrorDtoMessageBuilder(ClientErrorDto dto) {
    clientErrorDto = dto;
  }

  public static ClientErrorDtoMessageBuilder get(ClientErrorDto dto) {
    return new ClientErrorDtoMessageBuilder(dto);
  }

  public ClientErrorDtoMessageBuilder withdefaultMessage(String message) {
    defaultMessage = message;
    return this;
  }

  public String build() {
    return clientErrorDto == null ? getDefaultMessage() : getClientErrorMessage();
  }

  private String getClientErrorMessage() {
    String messageKey = clientErrorDto.getStatus();

    //assert translations.userMessageMap().containsKey(messageKey);
    if (translations.userMessageMap().containsKey(messageKey)) {
        return TranslationsUtils
                .replaceArguments(translations.userMessageMap().get(messageKey), clientErrorDto.getArgumentsArray());
    } else {
        return getDefaultMessage(); //this way we are able to get something out of the error
    }
  }

  private String getDefaultMessage() {
    return defaultMessage == null
        ? TranslationsUtils
        .replaceArguments(translations.userMessageMap().get("InternalError"), clientErrorDto.getArgumentsArray())
        : defaultMessage;
  }

}

