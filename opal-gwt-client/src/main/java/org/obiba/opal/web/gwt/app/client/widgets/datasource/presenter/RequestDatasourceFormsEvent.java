/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class RequestDatasourceFormsEvent extends GwtEvent<RequestDatasourceFormsEvent.Handler> {

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final HasDatasourceForms datasourceForms;

  public RequestDatasourceFormsEvent(HasDatasourceForms datasourceForms) {
    this.datasourceForms = datasourceForms;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRequestDatasourceFormsEvent(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public interface Handler extends EventHandler {
    void onRequestDatasourceFormsEvent(RequestDatasourceFormsEvent event);
  }

  public HasDatasourceForms getHasDatasourceForms() {
    return datasourceForms;
  }
}
