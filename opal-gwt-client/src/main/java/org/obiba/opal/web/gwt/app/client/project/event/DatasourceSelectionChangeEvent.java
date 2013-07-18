/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.project.event;

import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event to indicate that a Magma Datasource has been selected.
 */
public class DatasourceSelectionChangeEvent extends GwtEvent<DatasourceSelectionChangeEvent.Handler> {

  public interface Handler extends EventHandler {

    void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event);

  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final String datasource;

  /**
   * @param datasourceDto
   */
  public DatasourceSelectionChangeEvent(DatasourceDto datasourceDto) {
    datasource = datasourceDto.getName();
  }

  public DatasourceSelectionChangeEvent(String datasource) {
    this.datasource = datasource;
  }

  public String getSelection() {
    return datasource;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDatasourceSelectionChanged(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
