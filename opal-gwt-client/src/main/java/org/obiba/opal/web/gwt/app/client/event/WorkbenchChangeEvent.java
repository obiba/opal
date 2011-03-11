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

  private String resource;

  private boolean bindWorkbench = true;

  private boolean unbindWorkbench = true;

  /**
   * @param selectedItem
   */
  WorkbenchChangeEvent(WidgetPresenter<?> workbench) {
    this.workbench = workbench;
  }

  WorkbenchChangeEvent(WidgetPresenter<?> workbench, boolean bindWorkbench, boolean unbindWorkbench) {
    this.workbench = workbench;
    this.bindWorkbench = bindWorkbench;
    this.unbindWorkbench = unbindWorkbench;
  }

  public boolean resourceStartsWith(String path) {
    if(resource == null) return false;
    return resource.startsWith(path);
  }

  public String getResource() {
    return resource;
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

  public static Builder newBuilder(WidgetPresenter<?> workbench) {
    return new Builder(workbench);
  }

  public static class Builder {
    WorkbenchChangeEvent event;

    Builder(WidgetPresenter<?> workbench) {
      event = new WorkbenchChangeEvent(workbench);
    }

    public Builder forResource(String resource) {
      event.resource = resource;
      return this;
    }

    public Builder noBind() {
      event.bindWorkbench = false;
      return this;
    }

    public Builder noUnbind() {
      event.unbindWorkbench = false;
      return this;
    }

    public WorkbenchChangeEvent build() {
      return event;
    }
  }
}
