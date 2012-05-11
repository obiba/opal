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

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final JsArray<AttributeDto> attributes;

  private final UpdateType updateType;

  private final String originalNamespace;

  private final String originalName;

  public AttributeUpdateEvent(JsArray<AttributeDto> attributes, String originalNamespace, String originalName,
      UpdateType updateType) {
    this.attributes = attributes;
    this.updateType = updateType;
    this.originalNamespace = originalNamespace;
    this.originalName = originalName;
  }

  public AttributeUpdateEvent(JsArray<AttributeDto> attributes, UpdateType updateType) {
    this(attributes, null, null, updateType);
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onAttributeUpdate(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public JsArray<AttributeDto> getAttributes() {
    return attributes;
  }

  public UpdateType getUpdateType() {
    return updateType;
  }

  public String getOriginalNamespace() {
    return originalNamespace;
  }

  public String getOriginalName() {
    return originalName;
  }

  public interface Handler extends EventHandler {
    void onAttributeUpdate(AttributeUpdateEvent event);
  }
}
