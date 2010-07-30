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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ApplicationView implements ApplicationPresenter.Display {

  @UiTemplate("ApplicationView.ui.xml")
  interface ViewUiBinder extends UiBinder<DockLayoutPanel, ApplicationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final DockLayoutPanel dock;

  @UiField
  Panel topBar;

  @UiField
  MenuBar menuBar;

  @UiField
  Anchor quit;

  @UiField
  Anchor help;

  @UiField
  Anchor profile;

  @UiField
  Anchor studies;

  @UiField
  MenuItem dashboardItem;

  @UiField
  MenuItem exploreVariablesItem;

  @UiField
  MenuItem dataImportItem;

  @UiField
  MenuItem dataExportItem;

  @UiField
  MenuItem listJobsItem;

  @UiField
  MenuItem fileExplorer;

  @UiField
  Panel workbench;

  public ApplicationView() {
    dock = uiBinder.createAndBindUi(this);
  }

  @Override
  public MenuItem getDashboardItem() {
    return dashboardItem;
  }

  @Override
  public MenuItem getExploreVariables() {
    return exploreVariablesItem;
  }

  @Override
  public MenuItem getDataImportItem() {
    return dataImportItem;
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
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public Widget asWidget() {
    return dock;
  }

  @Override
  public MenuItem getDataExportItem() {
    return dataExportItem;
  }

  @Override
  public MenuItem getFileExplorer() {
    return fileExplorer;
  }

  @Override
  public HasClickHandlers getHelp() {
    return help;
  }

  @Override
  public HasClickHandlers getProfile() {
    return profile;
  }

  @Override
  public HasClickHandlers getStudies() {
    return studies;
  }

}
