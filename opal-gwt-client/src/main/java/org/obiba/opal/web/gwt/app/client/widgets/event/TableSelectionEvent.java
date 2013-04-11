/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.event;

import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that will be fired by {@link TableSelectorPresenter}.
 */
public class TableSelectionEvent extends GwtEvent<TableSelectionEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<Handler>();

  //
  // Instance Variables
  //

  private final Object source;

  private final List<TableDto> selectedTables;

  //
  // Constructors
  //
  public TableSelectionEvent(Object source, List<TableDto> selectedTables) {
    this.source = source;
    this.selectedTables = selectedTables;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onTableSelection(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  public Object getSource() {
    return source;
  }

  public List<TableDto> getSelectedTables() {
    return selectedTables;
  }

  /**
   * Get the first selected table.
   *
   * @return null if none
   */
  @Nullable
  public TableDto getSelectedTable() {
    return selectedTables != null && selectedTables.size() > 0 ? selectedTables.get(0) : null;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    void onTableSelection(TableSelectionEvent event);
  }
}
