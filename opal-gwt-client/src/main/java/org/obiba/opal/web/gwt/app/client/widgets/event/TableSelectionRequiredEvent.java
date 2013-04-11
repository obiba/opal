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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that will be handled by {@link TableSelectorPresenter}.
 */
public class TableSelectionRequiredEvent extends GwtEvent<TableSelectionRequiredEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<Handler>();

  //
  // Instance Variables
  //

  private final Object source;

  private final SelectionType selectionType;

  //
  // Constructors
  //

  public TableSelectionRequiredEvent(Object source) {
    this(source, SelectionType.MULTIPLE);
  }

  public TableSelectionRequiredEvent(Object source, SelectionType tableSelectionType) {
    this.source = source;
    this.selectionType = tableSelectionType;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onTableSelectionRequired(this);
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

  public SelectionType getSelectionType() {
    return selectionType;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    public void onTableSelectionRequired(TableSelectionRequiredEvent event);
  }
}
