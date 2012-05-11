/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.event;

import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class DatabaseCreatedEvent extends GwtEvent<DatabaseCreatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onCreated(DatabaseCreatedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();


  private final JdbcDataSourceDto dto;

  public DatabaseCreatedEvent(JdbcDataSourceDto dto) {
    this.dto = dto;
  }

  public JdbcDataSourceDto getJdbcDataSourceDto() {
    return dto;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onCreated(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }
}
