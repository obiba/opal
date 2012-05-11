/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class ConfirmationRequiredEvent extends GwtEvent<ConfirmationRequiredEvent.Handler> {
  //
  // Static Variables
  //

  private static Type<Handler> TYPE;

  //
  // Instance Variables
  //

  private final Object source;

  private final String titleKey;

  private final String messageKey;

  private final String title;

  private final String message;

  //
  // Constructors
  //

  private ConfirmationRequiredEvent(Object source, String titleKey, String messageKey, String title, String message) {
    this.source = source;
    this.titleKey = titleKey;
    this.messageKey = messageKey;
    this.title = title;
    this.message = message;
  }

//
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onConfirmationRequired(this);
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

  @Override
  public Object getSource() {
    return source;
  }

  public String getTitleKey() {
    return titleKey;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  /**
   * Creates a {@link ConfirmationRequiredEvent}.
   * @param source     the source of the event
   * @param titleKey   the unlocalized confirmation title
   * @param messageKey the unlocalized confirmation message
   */
  public static ConfirmationRequiredEvent createWithKeys(Object source, String titleKey, String messageKey) {
    return new ConfirmationRequiredEvent(source, titleKey, messageKey, null, null);
  }

  /**
   * Creates a {@link ConfirmationRequiredEvent}.
   * @param source  the source of the event
   * @param title   the localized confirmation title
   * @param message the localized confirmation message
   */
  public static ConfirmationRequiredEvent createWithMessages(Object source, String title, String message) {
    return new ConfirmationRequiredEvent(source, null, null, title, message);
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {
    void onConfirmationRequired(ConfirmationRequiredEvent event);
  }

}
