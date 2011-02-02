/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.event;

import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class DataShieldMethodUpdatedEvent extends GwtEvent<DataShieldMethodUpdatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onDataShieldMethodUpdated(DataShieldMethodUpdatedEvent event);
  }

  private static Type<Handler> TYPE;

  private final DataShieldMethodDto dto;

  public DataShieldMethodUpdatedEvent(DataShieldMethodDto dto) {
    this.dto = dto;
  }

  public DataShieldMethodDto getDataShieldMethod() {
    return dto;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDataShieldMethodUpdated(this);
  }

  @Override
  public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }
}
