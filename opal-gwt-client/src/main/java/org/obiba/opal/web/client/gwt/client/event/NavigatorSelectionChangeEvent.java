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

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.TreeItem;

/**
 *
 */
public class NavigatorSelectionChangeEvent extends GwtEvent<NavigatorSelectionChangeEventHandler> {

  private static Type<NavigatorSelectionChangeEventHandler> TYPE;

  private final TreeItem selection;

  /**
   * @param selectedItem
   */
  public NavigatorSelectionChangeEvent(TreeItem selectedItem) {
    this.selection = selectedItem;
  }

  public TreeItem getSelection() {
    return selection;
  }

  public static Type<NavigatorSelectionChangeEventHandler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<NavigatorSelectionChangeEventHandler>());
  }

  @Override
  protected void dispatch(NavigatorSelectionChangeEventHandler handler) {
    handler.onNavigatorSelectionChanged(this);
  }

  @Override
  public Type<NavigatorSelectionChangeEventHandler> getAssociatedType() {
    return TYPE;
  }
}
