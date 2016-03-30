/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.event;

import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class DatasourceUpdatedEvent extends GwtEvent<DatasourceUpdatedEvent.Handler> {

  public interface Handler extends EventHandler {

    void onDatasourceUpdated(DatasourceUpdatedEvent event);

  }

  private static final Type<Handler> TYPE = new Type<>();

  private final String datasourceName;

  /**
   * @param datasourceDto
   */
  public DatasourceUpdatedEvent(DatasourceDto datasourceDto) {
    datasourceName = datasourceDto.getName();
  }

  public DatasourceUpdatedEvent(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDatasourceUpdated(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
