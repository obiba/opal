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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.TreeItem;

/**
 *
 */
public class NavigatorSelectionChangeEvent extends GwtEvent<NavigatorSelectionChangeEvent.Handler> {

  public interface Handler extends EventHandler {

    void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event);

  }

  private static Type<Handler> TYPE;

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

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onNavigatorSelectionChanged(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }
}
