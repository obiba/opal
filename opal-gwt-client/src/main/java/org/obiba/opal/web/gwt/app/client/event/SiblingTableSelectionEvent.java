/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.event;

import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class SiblingTableSelectionEvent extends GwtEvent<SiblingTableSelectionEvent.Handler> {

  public interface Handler extends EventHandler {

    void onSiblingTableSelection(SiblingTableSelectionEvent event);

  }

  public enum Direction {
    NEXT, PREVIOUS
  }

  private static Type<Handler> TYPE;

  private final TableDto currentSelection;

  private final Direction direction;

  /**
   * @param selectedItem
   */
  public SiblingTableSelectionEvent(TableDto currentItem, Direction direction) {
    this.currentSelection = currentItem;
    this.direction = direction;
  }

  public TableDto getCurrentSelection() {
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
    handler.onSiblingTableSelection(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

}
