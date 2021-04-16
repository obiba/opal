/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseMessageBuilder;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

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
    getView().clearErrorMessages();

    getView().setErrorMessage(event.getResponse().getStatusCode()>=500 ? translations.systemErrorLablel() : translations.errorLabel(),
        ErrorResponseMessageBuilder.get(event.getResponse()).withDefaultMessage(event.getShortMessage()).build());
    return this;
  }
}

