package org.obiba.opal.web.gwt.app.client.support;

import java.util.ArrayList;
import java.util.Collection;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;
import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Widget;

public class ErrorResponseCallback implements ResponseCodeCallback {

  private static final Translations translations = GWT.create(Translations.class);

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  private final Widget widget;

  public ErrorResponseCallback(Widget widget) {
    this.widget = widget;
  }

  @Override
  public void onResponseCode(Request request, Response response) {
    RequestErrorEvent.Builder builder = new RequestErrorEvent.Builder();
    ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
    Collection<ConstraintViolationErrorDto> violationDtos = parseErrors(error);
    if(violationDtos.isEmpty()) {
      String errorMessage = translations.userMessageMap().get(error.getStatus());
      builder.message(errorMessage == null
          ? translationMessages
          .unknownResponse(error.getStatus(), String.valueOf(JsArrays.toList(error.getArgumentsArray())))
          : errorMessage);
    } else {
      builder.violations(violationDtos);
    }
    RequestErrorEvent.fire(widget, builder.build());
  }

  @SuppressWarnings("unchecked")
  private Collection<ConstraintViolationErrorDto> parseErrors(ClientErrorDto error) {
    Collection<ConstraintViolationErrorDto> violations = new ArrayList<ConstraintViolationErrorDto>();
    JsArray<ConstraintViolationErrorDto> errors = (JsArray<ConstraintViolationErrorDto>) error
        .getExtension(ConstraintViolationErrorDto.ClientErrorDtoExtensions.errors);
    if(errors != null) {
      for(int i = 0; i < errors.length(); i++) {
        violations.add(errors.get(i));
      }
    }
    return violations;
  }

}
