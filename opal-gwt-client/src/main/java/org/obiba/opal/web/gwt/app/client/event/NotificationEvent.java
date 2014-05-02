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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationCloseHandler;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class NotificationEvent extends GwtEvent<NotificationEvent.Handler> {

  public interface Handler extends EventHandler {

    void onUserMessage(NotificationEvent event);

  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private NotificationType notificationType;

  private String title;

  private List<String> messages;

  private List<String> messageArgs;

  private NotificationCloseHandler notificationCloseHandler;

  private boolean sticky = false;

  private boolean consumed = false;

  //
  // Constructors
  //

  private NotificationEvent(NotificationType notificationType, List<String> messages, List<String> messageArgs,
      NotificationCloseHandler notificationCloseHandler) {
    if(messages.isEmpty()) {
      throw new IllegalArgumentException("Missing message");
    }

    this.notificationType = notificationType;
    this.messages = messages;
    this.messageArgs = messageArgs != null ? new ArrayList<String>(messageArgs) : new ArrayList<String>();
    this.notificationCloseHandler = notificationCloseHandler;
  }

  private NotificationEvent(NotificationType notificationType, List<String> messages, List<String> messageArgs) {
    this(notificationType, messages, messageArgs, null);
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
    return getType();
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE;
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

  public NotificationEvent sticky() {
    sticky = true;
    return this;
  }

  public void setConsumed(boolean value) {
    consumed = value;
  }

  public boolean isConsumed() {
    return consumed;
  }

  public boolean isSticky() {
    return sticky;
  }

  public List<String> getMessages() {
    return messages == null ? (messages = new ArrayList<String>()) : messages;
  }

  public List<String> getMessageArgs() {
    if(messageArgs == null) {
      messageArgs = new ArrayList<String>();
    }
    return Collections.unmodifiableList(messageArgs);
  }

  public NotificationCloseHandler getNotificationCloseHandler() {
    return notificationCloseHandler;
  }

  public NotificationEvent setNotificationCloseHandler(NotificationCloseHandler notificationCloseHandler) {
    this.notificationCloseHandler = notificationCloseHandler;
    return this;
  }

  //
  // Builder
  //

  public static Builder newBuilder() {
    return new Builder();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {
    private NotificationEvent event;

    Builder() {

    }

    public Builder error(List<String> messages) {
      event = new NotificationEvent(NotificationType.ERROR, messages, null);
      return this;
    }

    public Builder error(ClientErrorDto error) {
      error(error.getStatus()).args(error.getArgumentsArray());
      return this;
    }

    public Builder error(String... messages) {
      return error(Arrays.asList(messages));
    }

    public Builder warn(List<String> messages) {
      event = new NotificationEvent(NotificationType.WARNING, messages, null);
      return this;
    }

    public Builder warn(String... messages) {
      return warn(Arrays.asList(messages));
    }

    public Builder info(List<String> messages) {
      event = new NotificationEvent(NotificationType.INFO, messages, null);
      return this;
    }

    public Builder info(String... messages) {
      return info(Arrays.asList(messages));
    }

    public Builder args(String... messageArgs) {
      return args(Arrays.asList(messageArgs));
    }

    public Builder args(JsArrayString messageArgs) {
      if(messageArgs != null && messageArgs.length() > 0) {
        return args(JsArrays.toList(messageArgs));
      }
      return this;
    }

    public Builder args(List<String> messageArgs) {
      event.messageArgs = messageArgs;
      return this;
    }

    public Builder sticky() {
      event.sticky();
      return this;
    }

    public NotificationEvent build() {
      return event;
    }
  }
}
