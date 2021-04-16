/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseMessageBuilder;
import org.obiba.opal.web.gwt.app.client.support.NotificationAlertTypeMap;
import org.obiba.opal.web.gwt.app.client.support.NotificationMessageBuilder;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.github.gwtbootstrap.client.ui.event.HiddenHandler;
import com.github.gwtbootstrap.client.ui.event.ShownEvent;
import com.github.gwtbootstrap.client.ui.event.ShownHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;

public abstract class ModalPopupViewWithUiHandlers<C extends ModalUiHandlers> extends ModalViewImpl
    implements HasUiHandlers<C> {

  private C uiHandlers;

  private HandlerRegistration handlerRegistration;

  private HandlerRegistration notificationHandlerRegistration;

  protected ModalPopupViewWithUiHandlers(EventBus eventBus) {
    super(eventBus);
    registerUserMessageEventHandler();
  }

  protected C getUiHandlers() {
    return uiHandlers;
  }

  @Override
  public void setUiHandlers(C uiHandlers) {
    this.uiHandlers = uiHandlers;
    asModal().addShownHandler(new ModalShownHandler());
    asModal().addHiddenHandler(new ModalHiddenHandler());
  }

  private void registerUserMessageEventHandler() {
    notificationHandlerRegistration = getEventBus()
        .addHandler(NotificationEvent.getType(), new NotificationEvent.Handler() {

      @Override
      public void onUserMessage(NotificationEvent event) {
        if(event.isConsumed() || !asModal().isVisible()) return;
        event.setConsumed(true);
        List<String> messages = NotificationMessageBuilder.get(event).build();

        for (String message : messages) {
          asModal().addAlert(message, NotificationAlertTypeMap.getAlertType(event.getNotificationType()));
        }
      }
    });


    handlerRegistration = getEventBus()
        .addHandler(UnhandledResponseEvent.getType(), new UnhandledResponseEvent.Handler() {
          @Override
          public void onUnhandledResponse(UnhandledResponseEvent event) {
            if(event.isConsumed() || !asModal().isVisible()) return;
            event.setConsumed(true);
            asModal().addAlert(
                ErrorResponseMessageBuilder.get(event.getResponse()).withDefaultMessage(event.getShortMessage())
                    .build(), AlertType.ERROR);
          }
        });
  }

  private class ModalHiddenHandler implements HiddenHandler {

    @Override
    public void onHidden(HiddenEvent hiddenEvent) {
      if(handlerRegistration != null) handlerRegistration.removeHandler();
      if(notificationHandlerRegistration != null) notificationHandlerRegistration.removeHandler();
      getUiHandlers().onModalHidden();
    }
  }

  private class ModalShownHandler implements ShownHandler {

    @Override
    public void onShown(ShownEvent shownEvent) {
      getUiHandlers().onModalShown();
    }
  }

}


