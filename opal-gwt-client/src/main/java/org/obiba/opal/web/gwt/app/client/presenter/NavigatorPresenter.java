/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;

public class NavigatorPresenter extends WidgetPresenter<NavigatorPresenter.Display> {

  public interface Display extends WidgetDisplay {

    ScrollPanel getTreePanel();

    ScrollPanel getDetailsPanel();

  }

  @Inject
  private NavigatorTreePresenter navigatorTreePresenter;

  @Inject
  private DatasourcePresenter datasourcePresenter;

  @Inject
  private TablePresenter tablePresenter;

  @Inject
  public NavigatorPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    navigatorTreePresenter.bind();
    datasourcePresenter.bind();
    tablePresenter.bind();

    getDisplay().getTreePanel().add(navigatorTreePresenter.getDisplay().asWidget());

    super.registerHandler(eventBus.addHandler(NavigatorSelectionChangeEvent.getType(), new NavigatorSelectionChangeEvent.Handler() {

      @Override
      public void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event) {
        TreeItem item = event.getSelection();
        if(item.getParentItem() == null) {
          getDisplay().getDetailsPanel().clear();
          getDisplay().getDetailsPanel().add(datasourcePresenter.getDisplay().asWidget());
        } else {
          getDisplay().getDetailsPanel().clear();
          getDisplay().getDetailsPanel().add(tablePresenter.getDisplay().asWidget());
        }
      }
    }));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    navigatorTreePresenter.unbind();
  }

  @Override
  public void refreshDisplay() {
    navigatorTreePresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    navigatorTreePresenter.revealDisplay();
  }

}
