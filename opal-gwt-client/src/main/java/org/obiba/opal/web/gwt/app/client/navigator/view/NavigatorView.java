/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorTreePresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class NavigatorView extends Composite implements NavigatorPresenter.Display {

  @UiTemplate("NavigatorView.ui.xml")
  interface NavigatorViewUiBinder extends UiBinder<Widget, NavigatorView> {
  }

  private static NavigatorViewUiBinder uiBinder = GWT.create(NavigatorViewUiBinder.class);

  @UiField
  ScrollPanel navigatorDisplayPanel;

  @UiField
  ScrollPanel treePanel;

  @UiField
  Button createDatasourceButton;

  public NavigatorView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setTreeDisplay(NavigatorTreePresenter.Display treeDisplay) {
    if(treePanel.getWidget() != null) {
      treePanel.remove(treePanel.getWidget());
    }
    treePanel.add(treeDisplay.asWidget());
  }

  @Override
  public HasWidgets getDetailsPanel() {
    return navigatorDisplayPanel;
  }

  @Override
  public void addCreateDatasourceClickHandler(ClickHandler handler) {
    createDatasourceButton.addClickHandler(handler);
  }

}
