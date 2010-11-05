/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Indicates that the form has changed, but it hasn't been saved yet.
 */
public class ViewSavePendingEvent extends GwtEvent<ViewSavePendingEvent.Handler> {

  private static Type<Handler> TYPE;

  public ViewSavePendingEvent() {
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSavePending(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  public interface Handler extends EventHandler {
    public void onSavePending(ViewSavePendingEvent event);
  }

}