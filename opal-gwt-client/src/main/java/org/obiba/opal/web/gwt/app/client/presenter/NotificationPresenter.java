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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.inject.Inject;

/**
 * Presenter used to display error, warning and info messages in a dialog box.
 */
public class NotificationPresenter extends WidgetPresenter<NotificationPresenter.Display> {

  public interface Display extends WidgetDisplay {

    public void showPopup();

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

  @Inject
  private Translations translations;

  @Inject
  public NotificationPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    display.showPopup();
  }

  public void setNotification(NotificationEvent event) {
    setMessageDialogType(event.getNotificationType());

    if(event.getTitle() != null) {
      getDisplay().setCaption(event.getTitle());
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

    getDisplay().setSticky(event.isSticky());

    addNotificationCloseHandler(event.getNotificationCloseHandler());
  }

  public void setTitle(String title) {
    getDisplay().setCaption(title);
  }

  public void setMessages(List<String> messages) {
    display.setMessages(messages);
  }

  public void setMessageDialogType(NotificationType messageDialogType) {
    getDisplay().setNotificationType(messageDialogType);
    switch(messageDialogType) {
    case ERROR:
      getDisplay().setCaption(translations.errorDialogTitle());
      break;
    case WARNING:
      getDisplay().setCaption(translations.warningDialogTitle());
      break;
    case INFO:
      getDisplay().setCaption(translations.infoDialogTitle());
      break;
    }
  }

  public void addNotificationCloseHandler(final NotificationCloseHandler handler) {
    if(handler != null) {
      getDisplay().addNotificationCloseHandler(handler);
    }
  }

  public enum NotificationType {
    ERROR, WARNING, INFO
  }

}
