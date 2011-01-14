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

public class FunctionalUnitDeletedEvent extends GwtEvent<FunctionalUnitDeletedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onFunctionalUnitDeleted(FunctionalUnitDeletedEvent event);
  }

  private static Type<Handler> TYPE;

  private final FunctionalUnitDto functionalUnit;

  public FunctionalUnitDeletedEvent(FunctionalUnitDto functionalUnit) {
    this.functionalUnit = functionalUnit;
  }

  public FunctionalUnitDto getFunctionalUnit() {
    return functionalUnit;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onFunctionalUnitDeleted(this);
  }

  @Override
  public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }
}
