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
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;

public class JSErrorNotificationEventBuilder {

  public NotificationEvent build(ClientErrorDto error) {

    if(error.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
      JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) error
          .getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);

      NotificationEvent notificationEvent = NotificationEvent.Builder.newNotification().error("JavascriptError")
          .args(errors.get(0).getSourceName(), //
              errors.get(0).getMessage(), //
              String.valueOf(errors.get(0).getLineNumber()),//
              String.valueOf(errors.get(0).getColumnNumber())).build();

      return notificationEvent;
    }

    return NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray()).build();
  }
}
