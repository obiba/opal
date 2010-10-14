/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationCloseHandler;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class NotificationEvent extends GwtEvent<NotificationEvent.Handler> {

  public interface Handler extends EventHandler {

    void onUserMessage(NotificationEvent event);

  }

  private static Type<Handler> TYPE;

  private NotificationType notificationType;

  private String title;

  private List<String> messages;

  private List<String> messageArgs;

  private NotificationCloseHandler notificationCloseHandler;

  //
  // Constructors
  //

  public NotificationEvent(NotificationType notificationType, List<String> messages, List<String> messageArgs, NotificationCloseHandler notificationCloseHandler) {
    if(messages.isEmpty()) {
      throw new IllegalArgumentException("Missing message");
    }
    if(messageArgs == null) {
      messageArgs = new ArrayList<String>();
    }

    this.notificationType = notificationType;
    this.messages = messages;
    this.messageArgs = new ArrayList<String>(messageArgs);
    this.notificationCloseHandler = notificationCloseHandler;
  }

  public NotificationEvent(NotificationType notificationType, List<String> messages, List<String> messageArgs) {
    this(notificationType, messages, messageArgs, null);
  }

  public NotificationEvent(NotificationType notificationType, String message, List<String> messageArgs, NotificationCloseHandler notificationCloseHandler) {
    this(notificationType, Arrays.asList(message), messageArgs, notificationCloseHandler);
  }

  public NotificationEvent(NotificationType notificationType, String message, List<String> messageArgs) {
    this(notificationType, message, messageArgs, null);
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onUserMessage(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  public NotificationType getNotificationType() {
    return notificationType;
  }

  public NotificationEvent setNotificationType(NotificationType notificationType) {
    this.notificationType = notificationType;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public NotificationEvent setTitle(String title) {
    this.title = title;
    return this;
  }

  public NotificationEvent addMessage(String message) {
    if(message != null) {
      getMessages().add(message);
    }
    return this;
  }

  public List<String> getMessages() {
    return messages != null ? messages : (messages = new ArrayList<String>());
  }

  public List<String> getMessageArgs() {
    return Collections.unmodifiableList(messageArgs);
  }

  public NotificationCloseHandler getNotificationCloseHandler() {
    return notificationCloseHandler;
  }

  public NotificationEvent setNotificationCloseHandler(NotificationCloseHandler notificationCloseHandler) {
    this.notificationCloseHandler = notificationCloseHandler;
    return this;
  }
}
