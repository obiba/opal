/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.event;

import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class KeyPairCreatedEvent extends GwtEvent<KeyPairCreatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onKeyPairCreated(KeyPairCreatedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();


  private final FunctionalUnitDto functionalUnit;

  private final String alias;

  public KeyPairCreatedEvent(FunctionalUnitDto functionalUnit, String alias) {
    this.functionalUnit = functionalUnit;
    this.alias = alias;
  }

  public FunctionalUnitDto getFunctionalUnit() {
    return functionalUnit;
  }

  public String getAlias() {
    return alias;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onKeyPairCreated(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }
}
