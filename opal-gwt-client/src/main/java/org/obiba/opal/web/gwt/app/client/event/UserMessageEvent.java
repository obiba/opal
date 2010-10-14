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

import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.NotificationCloseHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class UserMessageEvent extends GwtEvent<UserMessageEvent.Handler> {

  public interface Handler extends EventHandler {

    void onUserMessage(UserMessageEvent event);

  }

  private static Type<Handler> TYPE;

  private MessageDialogType messageType;

  private String title;

  private List<String> messages;

  private List<String> messageArgs;

  private NotificationCloseHandler notificationCloseHandler;

  //
  // Constructors
  //

  public UserMessageEvent(MessageDialogType messageType, List<String> messages, List<String> messageArgs, NotificationCloseHandler notificationCloseHandler) {
    if(messages.isEmpty()) {
      throw new IllegalArgumentException("Missing message");
    }
    if(messageArgs == null) {
      messageArgs = new ArrayList<String>();
    }

    this.messageType = messageType;
    this.messages = messages;
    this.messageArgs = new ArrayList<String>(messageArgs);
    this.notificationCloseHandler = notificationCloseHandler;
  }

  public UserMessageEvent(MessageDialogType messageType, List<String> messages, List<String> messageArgs) {
    this(messageType, messages, messageArgs, null);
  }

  public UserMessageEvent(MessageDialogType messageType, String message, List<String> messageArgs, NotificationCloseHandler notificationCloseHandler) {
    this(messageType, Arrays.asList(message), messageArgs, notificationCloseHandler);
  }

  public UserMessageEvent(MessageDialogType messageType, String message, List<String> messageArgs) {
    this(messageType, message, messageArgs, null);
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

  public MessageDialogType getMessageType() {
    return messageType;
  }

  public UserMessageEvent setMessageType(MessageDialogType messageType) {
    this.messageType = messageType;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public UserMessageEvent setTitle(String title) {
    this.title = title;
    return this;
  }

  public UserMessageEvent addMessage(String message) {
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

  public UserMessageEvent setNotificationCloseHandler(NotificationCloseHandler notificationCloseHandler) {
    this.notificationCloseHandler = notificationCloseHandler;
    return this;
  }
}
