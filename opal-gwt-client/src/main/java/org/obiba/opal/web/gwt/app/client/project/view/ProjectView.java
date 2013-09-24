/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectUiHandlers;
import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalTabPanel;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Popover;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ProjectView extends ViewWithUiHandlers<ProjectUiHandlers> implements ProjectPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectView> {}

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Breadcrumbs titlecrumbs;

  @UiField
  Heading projectHeader;

  @UiField
  com.google.gwt.user.client.ui.Label description;

  @UiField
  FlowPanel tagsPanel;

  @UiField
  Panel timestampsPanel;

  @UiField
  Popover timestamps;

  @UiField
  OpalTabPanel tabPanel;

  @UiField
  Panel tablesPanel;

  @UiField
  Panel filesPanel;

  @UiField
  Panel adminPanel;

  private ProjectDto project;

  @Inject
  ProjectView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    for(ProjectTab tab : ProjectTab.values()) {
      String title = translations.projectTabNameMap().get(tab.toString());
      TabPanelHelper.setTabTitle(tabPanel, tab.ordinal(), title);
      TabPanelHelper.setTabText(tabPanel, tab.ordinal(), title);
    }
  }

  @Override
  public void setProject(ProjectDto project) {
    this.project = project;
    if(titlecrumbs.getWidgetCount() > 1) {
      titlecrumbs.remove(1);
    }
    titlecrumbs.add(new NavLink(project.getTitle()));

    projectHeader.setText(project.getTitle());
    projectHeader.setSubtext("[" + project.getName() +"]");
    description.setText(project.getDescription());

    tagsPanel.clear();
    JsArrayString tagsArray = JsArrays.toSafeArray(project.getTagsArray());
    if (tagsArray.length()>0) {
      for (String tag : JsArrays.toIterable(tagsArray)) {
        tagsPanel.add(new Label(tag));
      }
    }

    timestampsPanel.setVisible(project.hasTimestamps());
    if (project.hasTimestamps()) {
      Moment created = Moment.create(project.getTimestamps().getCreated());
      Moment lastUpdate = Moment.create(project.getTimestamps().getLastUpdate());
      timestamps.setHeading(translations.timestampsLabel());
      String createdOn = TranslationsUtils.replaceArguments(translations.createdOnLabel(), created.format(FormatType.MONTH_NAME_TIME_SHORT));
      String lastUpdateOn = TranslationsUtils.replaceArguments(translations.lastUpdateOnLabel(), lastUpdate.fromNow());
      timestamps.setText(createdOn + "<br/>" + lastUpdateOn);
      timestamps.reconfigure();
    }
  }

  @Override
  public void selectTab(int tab) {
    tabPanel.selectTab(tab);
  }

  @Override
  public void setTabData(int index, Object data) {
    tabPanel.setData(index, data);
  }

  @Override
  public Object getTabData(int index) {
    return tabPanel.getData(index);
  }

  @UiHandler("projects")
  void onProjectsSelection(ClickEvent event) {
    getUiHandlers().onProjectsSelection();
  }

  @UiHandler("tabPanel")
  void onShown(TabPanel.ShownEvent shownEvent) {
    if(shownEvent.getTarget() == null) return;

    showTabTexts(tabPanel.getSelectedTab() == 0);

    getUiHandlers().onTabSelected(tabPanel.getSelectedTab());
  }

  private void showTabTexts(boolean show) {
    for(ProjectTab tab : ProjectTab.values()) {
      TabPanelHelper
          .setTabText(tabPanel, tab.ordinal(), show ? translations.projectTabNameMap().get(tab.toString()) : "");
    }
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == ProjectPresenter.TABLES_PANE) {
      tablesPanel.clear();
      tablesPanel.add(content);
    } else if(slot == ProjectPresenter.FILES_PANE) {
      filesPanel.clear();
      filesPanel.add(content);
    } else if(slot == ProjectPresenter.ADMIN_PANE) {
      adminPanel.clear();
      adminPanel.add(content);
    }
  }

}
