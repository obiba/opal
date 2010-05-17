/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.client.gwt.client.event;

import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class VariableSelectionChangeEvent extends GwtEvent<VariableSelectionChangeEventHandler> {

  private static Type<VariableSelectionChangeEventHandler> TYPE;

  private final VariableDto selection;

  /**
   * @param selectedItem
   */
  public VariableSelectionChangeEvent(VariableDto selectedItem) {
    this.selection = selectedItem;
  }

  public VariableDto getSelection() {
    return selection;
  }

  public static Type<VariableSelectionChangeEventHandler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<VariableSelectionChangeEventHandler>());
  }

  @Override
  protected void dispatch(VariableSelectionChangeEventHandler handler) {
    handler.onVariableSelectionChanged(this);
  }

  @Override
  public Type<VariableSelectionChangeEventHandler> getAssociatedType() {
    return TYPE;
  }
}
