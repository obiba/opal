/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class GwtEventBusAdaptor extends DefaultEventBus implements EventBus {

  private final DefaultEventBus oldEventBus = new DefaultEventBus();

  private final com.google.gwt.event.shared.EventBus eventBus;

  @Inject
  public GwtEventBusAdaptor(com.google.gwt.event.shared.EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public <H extends EventHandler> HandlerRegistration addHandler(Type<H> type, H handler) {
    oldEventBus.addHandler(type, handler);
    return eventBus.addHandler(type, handler);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    eventBus.fireEvent(event);
  }

  @Override
  public <H extends EventHandler> H getHandler(Type<H> type, int index) {
    return oldEventBus.getHandler(type, index);
  }

  @Override
  public int getHandlerCount(Type<?> type) {
    return oldEventBus.getHandlerCount(type);
  }

  @Override
  public boolean isEventHandled(Type<?> e) {
    return oldEventBus.isEventHandled(e);
  }

}
