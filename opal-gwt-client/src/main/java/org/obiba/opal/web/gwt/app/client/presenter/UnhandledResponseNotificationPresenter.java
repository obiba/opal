/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class UnhandledResponseNotificationPresenter
    extends PresenterWidget<UnhandledResponseNotificationPresenter.Display> {

  private final Translations translations;

  public interface Display extends View {
    void clearErrorMessages();
    void setErrorMessage(String title, String msg);
  }

  @Inject
  public UnhandledResponseNotificationPresenter(Display display, EventBus eventBus, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
  }

  public UnhandledResponseNotificationPresenter withResponseEvent(UnhandledResponseEvent event) {
    String message = getClientErrorMessage(event);

    if (message == null) {
      message = getDefaultMessage(event);
    }

    getView().clearErrorMessages();
    getView().setErrorMessage(translations.systemErrorLablel(), message);

    return this;
  }

  private String getClientErrorMessage(UnhandledResponseEvent event) {

    Response response = event.getResponse();

    if (response != null) {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());

      if (errorDto != null) {
        String messageKey = errorDto.getStatus();
        assert translations.userMessageMap().containsKey(messageKey);
        return TranslationsUtils
            .replaceArguments(translations.userMessageMap().get(messageKey), errorDto.getArgumentsArray());
      }
    }

    return null;
  }

  private String getDefaultMessage(UnhandledResponseEvent event) {

    String message = event.getShortMessage();

    if(!event.getResponse().getText().isEmpty()) {
      message += ": " + event.getResponse().getText();
    }

    return message;
  }

}

