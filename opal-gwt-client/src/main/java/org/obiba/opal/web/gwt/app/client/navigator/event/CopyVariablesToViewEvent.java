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

import java.util.List;

import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class CopyVariablesToViewEvent extends GwtEvent<CopyVariablesToViewEvent.Handler> {

  public interface Handler extends EventHandler {

    void onVariableCopy(CopyVariablesToViewEvent event);

  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final TableDto table;

  private final List<VariableDto> selection;

  /**
   * @param selectedItem
   */
  public CopyVariablesToViewEvent(TableDto table, List<VariableDto> selectedItems) {
    this.table = table;
    selection = selectedItems;
  }

  public TableDto getTable() {
    return table;
  }

  public List<VariableDto> getSelection() {
    return selection;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onVariableCopy(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
