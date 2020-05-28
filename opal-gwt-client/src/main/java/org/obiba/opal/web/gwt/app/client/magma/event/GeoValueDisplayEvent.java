/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.event;

import javax.annotation.Nullable;

import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class GeoValueDisplayEvent extends GwtEvent<GeoValueDisplayEvent.Handler> {

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final VariableDto variable;

  private final String entityIdentifier;

  private final ValueSetsDto.ValueDto value;

  private final Integer index;

  public GeoValueDisplayEvent(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value) {
    this(variable, entityIdentifier, value, null);
  }

  /**
   * @param variable
   * @param entityIdentifier
   * @param value
   * @param index if value is a value sequence index will provide which value in the sequence is to be displayed
   */
  public GeoValueDisplayEvent(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value,
      @Nullable Integer index) {
    this.variable = variable;
    this.entityIdentifier = entityIdentifier;
    this.value = value;
    this.index = index;
  }

  public VariableDto getVariable() {
    return variable;
  }

  public String getEntityIdentifier() {
    return entityIdentifier;
  }

  public ValueSetsDto.ValueDto getValue() {
    return value;
  }

  public Integer getIndex() {
    return index;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  public Type<GeoValueDisplayEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(GeoValueDisplayEvent.Handler handler) {
    handler.onGeoValueDisplay(this);
  }

  public interface Handler extends EventHandler {

    void onGeoValueDisplay(GeoValueDisplayEvent event);

  }
}
