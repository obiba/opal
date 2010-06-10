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
import java.util.Collections;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;

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

  private String message;

  private List<String> messageArgs;

  //
  // Constructors
  //

  public UserMessageEvent(MessageDialogType messageType, String message, List<String> messageArgs) {
    if(message == null) {
      throw new IllegalArgumentException("null message");
    }
    if(messageArgs == null) {
      messageArgs = new ArrayList<String>();
    }

    this.messageType = messageType;
    this.message = message;
    this.messageArgs = new ArrayList<String>(messageArgs);
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

  public String getMessage() {
    return message;
  }

  public List<String> getMessageArgs() {
    return Collections.unmodifiableList(messageArgs);
  }
}
