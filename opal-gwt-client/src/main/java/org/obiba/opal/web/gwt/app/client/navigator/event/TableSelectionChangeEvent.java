/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.event;

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

  private static Type<Handler> TYPE;

  private final TableDto tableDto;

  private final String previous;

  private final String next;

  private final Object source;

  /**
   * @param selectedItem
   */
  public TableSelectionChangeEvent(Object source, TableDto tableDto) {
    this(source, tableDto, null, null);
  }

  public TableSelectionChangeEvent(Object source, TableDto tableDto, String previous, String next) {
    super();
    this.source = source;
    this.tableDto = tableDto;
    this.previous = previous;
    this.next = next;
  }

  public Object getSource() {
    return source;
  }

  public TableDto getSelection() {
    return tableDto;
  }

  public String getPrevious() {
    return previous;
  }

  public String getNext() {
    return next;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onTableSelectionChanged(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }
}
