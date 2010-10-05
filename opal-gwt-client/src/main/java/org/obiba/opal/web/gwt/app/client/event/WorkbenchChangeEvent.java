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

import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class WorkbenchChangeEvent extends GwtEvent<WorkbenchChangeEvent.Handler> {

  public interface Handler extends EventHandler {

    void onWorkbenchChanged(WorkbenchChangeEvent event);

  }

  private static Type<Handler> TYPE;

  private final WidgetPresenter<?> workbench;

  private boolean bindWorkbench = true;

  private boolean unbindWorkbench = true;

  /**
   * @param selectedItem
   */
  public WorkbenchChangeEvent(WidgetPresenter<?> workbench) {
    this.workbench = workbench;
  }

  public WorkbenchChangeEvent(WidgetPresenter<?> workbench, boolean bindWorkbench, boolean unbindWorkbench) {
    this.workbench = workbench;
    this.bindWorkbench = bindWorkbench;
    this.unbindWorkbench = unbindWorkbench;
  }

  public WidgetPresenter<?> getWorkbench() {
    return workbench;
  }

  public boolean shouldBindWorkbench() {
    return bindWorkbench;
  }

  public boolean shouldUnbindWorkbench() {
    return unbindWorkbench;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onWorkbenchChanged(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }
}
