/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;

public class JSErrorNotificationEventBuilder {

  public NotificationEvent build(ClientErrorDto error) {
    if(error.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
      return parseJavaScriptErrorDto(error);
    }
    else if(error.getExtension(DatasourceParsingErrorDto.ClientErrorDtoExtensions.errors) != null) {
      return parseDatasourceParsingErrorDto(error);
    }

    return NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray()).build();
  }

  private NotificationEvent parseJavaScriptErrorDto(ClientErrorDto error) {
    JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) error
        .getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);

    return NotificationEvent.Builder.newNotification().error("JavascriptError").args(errors.get(0).getSourceName(), //
        errors.get(0).getMessage(), //
        String.valueOf(errors.get(0).getLineNumber()),//
        String.valueOf(errors.get(0).getColumnNumber())).build();
  }

  private NotificationEvent parseDatasourceParsingErrorDto(ClientErrorDto error) {
    JsArray<DatasourceParsingErrorDto> errors = (JsArray<DatasourceParsingErrorDto>) error
      .getExtension(DatasourceParsingErrorDto.ClientErrorDtoExtensions.errors);

    DatasourceParsingErrorDto parsingError = errors.get(0);
    String errorKey = parsingError.getKey();

    if (Strings.isNullOrEmpty(errorKey)) {
      return NotificationEvent.Builder.newNotification().error(parsingError.getDefaultMessage()).build();
    }

    return NotificationEvent.Builder.newNotification().error(parsingError.getKey())
        .args(parsingError.getArgumentsArray()).build();
  }
}
