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

import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class SiblingVariableSelectionEvent extends GwtEvent<SiblingVariableSelectionEvent.Handler> {

  public interface Handler extends EventHandler {

    void onSiblingVariableSelection(SiblingVariableSelectionEvent event);

  }

  public enum Direction {
    NEXT, PREVIOUS
  }

  private static Type<Handler> TYPE;

  private final VariableDto currentSelection;

  private final Direction direction;

  /**
   * @param selectedItem
   */
  public SiblingVariableSelectionEvent(VariableDto currentItem, Direction direction) {
    this.currentSelection = currentItem;
    this.direction = direction;
  }

  public VariableDto getCurrentSelection() {
    return currentSelection;
  }

  public Direction getDirection() {
    return direction;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSiblingVariableSelection(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

}
