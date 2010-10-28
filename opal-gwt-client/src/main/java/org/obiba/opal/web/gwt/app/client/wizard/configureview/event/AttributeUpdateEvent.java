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

import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This event contains a collection of {@link AttributeDto}s and an {@link UpdateType} to indicate if the collection of
 * AttributeDtos are to replace existing attributes or to be added to the list of existing attributes.
 */
public class AttributeUpdateEvent extends GwtEvent<AttributeUpdateEvent.Handler> {

  private static Type<Handler> TYPE;

  private JsArray<AttributeDto> attributes;

  private UpdateType updateType;

  public AttributeUpdateEvent(JsArray<AttributeDto> attributes, UpdateType updateType) {
    this.attributes = attributes;
    this.updateType = updateType;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onAttributeUpdate(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  public JsArray<AttributeDto> getAttributes() {
    return attributes;
  }

  public UpdateType getUpdateType() {
    return updateType;
  }

  public interface Handler extends EventHandler {
    public void onAttributeUpdate(AttributeUpdateEvent event);
  }
}
