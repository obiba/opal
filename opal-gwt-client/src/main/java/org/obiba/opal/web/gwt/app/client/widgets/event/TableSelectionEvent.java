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

import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that will be fired by {@link TableSelectorPresenter}.
 */
public class TableSelectionEvent extends GwtEvent<TableSelectionEvent.Handler> {
  //
  // Static Variables
  //

  private static Type<Handler> TYPE;

  //
  // Instance Variables
  //

  private Object callSource;

  private List<TableDto> selectedTables;

  //
  // Constructors
  //
  public TableSelectionEvent(Object callSource, List<TableDto> selectedTables) {
    super();
    this.callSource = callSource;
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
    return TYPE;
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  public Object getCallSource() {
    return callSource;
  }

  public List<TableDto> getSelectedTables() {
    return selectedTables;
  }

  /**
   * Get the first selected table.
   * @return null if none
   */
  public TableDto getSelectedTable() {
    if(selectedTables != null && selectedTables.size() > 0) {
      return selectedTables.get(0);
    } else
      return null;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    public void onTableSelection(TableSelectionEvent event);
  }
}
