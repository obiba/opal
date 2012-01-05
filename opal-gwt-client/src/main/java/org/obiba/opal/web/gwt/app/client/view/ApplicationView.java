/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ApplicationView implements ApplicationPresenter.Display {

  @UiTemplate("ApplicationView.ui.xml")
  interface ViewUiBinder extends UiBinder<LayoutPanel, ApplicationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final LayoutPanel dock;

  @UiField
  Panel topBar;

  @UiField
  MenuBar menuBar;

  @UiField
  Anchor help;

  @UiField
  Anchor quit;

  @UiField
  Anchor administration;

  @UiField
  InlineLabel username;

  @UiField
  Anchor obiba;

  @UiField
  Label version;

  @UiField
  MenuItem dashboardItem;

  @UiField
  MenuItem datasourcesItem;

  @UiField
  MenuItem listJobsItem;

  @UiField
  MenuItem fileExplorer;

  @UiField
  MenuItem reportsItem;

  @UiField
  MenuItem unitsItem;

  @UiField
  Panel workbench;

  MenuItem currentSelection;

  public ApplicationView() {
    dock = uiBinder.createAndBindUi(this);

    obiba.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        Window.open("http://obiba.org", "_blank", null);
      }
    });
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    this.workbench.clear();
    this.workbench.add(content);
  }

  @Override
  public MenuItem getDashboardItem() {
    return dashboardItem;
  }

  @Override
  public MenuItem getListJobsItem() {
    return listJobsItem;
  }

  @Override
  public void updateWorkbench(Widget workbench) {
    this.workbench.clear();
    this.workbench.add(workbench);
  }

  @Override
  public HasClickHandlers getQuit() {
    return quit;
  }

  @Override
  public HasClickHandlers getHelp() {
    return help;
  }

  @Override
  public HasClickHandlers getAdministration() {
    return administration;
  }

  @Override
  public Widget asWidget() {
    return dock;
  }

  @Override
  public MenuItem getFileExplorerItem() {
    return fileExplorer;
  }

  @Override
  public MenuItem getReportsItem() {
    return reportsItem;
  }

  @Override
  public void setCurrentSelection(MenuItem selection) {
    if(currentSelection != null) {
      currentSelection.removeStyleName("selected");
    }
    if(selection != null) {
      selection.addStyleName("selected");
    }
    currentSelection = selection;
  }

  @Override
  public void clearSelection() {
    setCurrentSelection(null);
  }

  @Override
  public MenuItem getDatasourcesItem() {
    return datasourcesItem;
  }

  @Override
  public MenuItem getUnitsItem() {
    return unitsItem;
  }

  @Override
  public HasAuthorization getAdministrationAuthorizer() {
    return new UIObjectAuthorizer(administration) {
      @Override
      public void authorized() {
        super.authorized();
      }

      @Override
      public void unauthorized() {
        super.unauthorized();
      }
    };
  }

  @Override
  public void setUsername(String username) {
    this.username.setText(username);
  }

  @Override
  public void setVersion(String version) {
    this.version.setText(version);
  }

}
