/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.event;

import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event to indicate that a Magma Table has been selected.
 */
public class TableSelectionChangeEvent extends GwtEvent<TableSelectionChangeEvent.Handler> {

  public interface Handler extends EventHandler {

    void onTableSelectionChanged(TableSelectionChangeEvent event);

  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final String datasourceName;

  private final String tableName;

  private final TableDto table;

  private final Object source;

  public TableSelectionChangeEvent(Object source, String datasourceName, String tableName) {
    this.source = source;
    this.datasourceName = datasourceName;
    this.tableName = tableName;
    table = null;
  }

  public TableSelectionChangeEvent(Object source, TableDto table) {
    this.source = source;
    datasourceName = table.getDatasourceName();
    tableName = table.getName();
    this.table = table;
  }

  @Override
  public Object getSource() {
    return source;
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public String getTableName() {
    return tableName;
  }

  public boolean hasTable() {
    return table != null;
  }

  public TableDto getTable() {
    return table;
  }

  public boolean isView() {
    return hasTable() && table.hasViewLink();
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onTableSelectionChanged(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
