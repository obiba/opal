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
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class NavigatorView extends Composite implements NavigatorPresenter.Display {

  public static final int PAGE_SIZE = 100;

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

  @UiField
  Button importDataButton;

  @UiField
  Button exportDataButton;

  public NavigatorView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
    if(slot == NavigatorPresenter.LEFT_PANE) {
      treePanel.add(content);
    } else {
      navigatorDisplayPanel.add(content);
    }
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {
    if(slot == NavigatorPresenter.LEFT_PANE) {
      treePanel.remove(content);
    } else {
      navigatorDisplayPanel.remove(content);
    }
  };

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == NavigatorPresenter.LEFT_PANE) {
      treePanel.clear();
      treePanel.add(content);
    } else {
      navigatorDisplayPanel.clear();
      navigatorDisplayPanel.add(content);
    }
  }

  @Override
  public HasWidgets getDetailsPanel() {
    return navigatorDisplayPanel;
  }

  @Override
  public HandlerRegistration addCreateDatasourceClickHandler(ClickHandler handler) {
    return createDatasourceButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addExportDataClickHandler(ClickHandler handler) {
    return exportDataButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addImportDataClickHandler(ClickHandler handler) {
    return importDataButton.addClickHandler(handler);
  }

  @Override
  public HasAuthorization getCreateDatasourceAuthorizer() {
    return new UIObjectAuthorizer(createDatasourceButton);
  }

  @Override
  public HasAuthorization getImportDataAuthorizer() {
    return new UIObjectAuthorizer(importDataButton);
  }

  @Override
  public HasAuthorization getExportDataAuthorizer() {
    return new UIObjectAuthorizer(exportDataButton);
  }

  private void setTreeDisplay(Widget content) {
    if(treePanel.getWidget() != null) {
      treePanel.remove(treePanel.getWidget());
    }
    treePanel.add(content);
  }
}
