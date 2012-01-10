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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * Presenter used to display error, warning and info messages in a dialog box.
 */
public class NotificationPresenter extends PresenterWidget<NotificationPresenter.Display> {

  public interface Display extends PopupView {

    public void setMessages(List<String> messages);

    public void setCaption(String txt);

    public void setNotificationType(NotificationType type);

    public void setSticky(boolean sticky);

    public void addNotificationCloseHandler(final NotificationCloseHandler handler);
  }

  public interface NotificationCloseHandler {
    /**
     * Called when {@link CloseEvent} is fired.
     * 
     * @param event the {@link CloseEvent} that was fired
     */
    void onClose(CloseEvent<?> event);
  }

  private final Translations translations;

  @Inject
  public NotificationPresenter(Display display, EventBus eventBus, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
  }

  public void setNotification(NotificationEvent event) {
    setMessageDialogType(event.getNotificationType());

    if(event.getTitle() != null) {
      getView().setCaption(event.getTitle());
    }

    List<String> translatedMessages = new ArrayList<String>();
    for(String message : event.getMessages()) {
      if(translations.userMessageMap().containsKey(message)) {
        translatedMessages.add(translations.userMessageMap().get(message));
      } else {
        translatedMessages.add(message);
      }
    }
    setMessages(translatedMessages);

    getView().setSticky(event.isSticky());

    addNotificationCloseHandler(event.getNotificationCloseHandler());
  }

  private void setMessages(List<String> messages) {
    getView().setMessages(messages);
  }

  private void setMessageDialogType(NotificationType messageDialogType) {
    getView().setNotificationType(messageDialogType);
    switch(messageDialogType) {
    case ERROR:
      getView().setCaption(translations.errorDialogTitle());
      break;
    case WARNING:
      getView().setCaption(translations.warningDialogTitle());
      break;
    case INFO:
      getView().setCaption(translations.infoDialogTitle());
      break;
    }
  }

  private void addNotificationCloseHandler(final NotificationCloseHandler handler) {
    if(handler != null) {
      getView().addNotificationCloseHandler(handler);
    }
  }

  public enum NotificationType {
    ERROR, WARNING, INFO
  }

}
