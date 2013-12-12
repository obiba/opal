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

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectUiHandlers;
import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalTabPanel;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.magma.TimestampsDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.ProjectSummaryDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.TabPanel;
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

  @UiField
  Breadcrumbs titlecrumbs;

  @UiField
  Heading projectHeader;

  @UiField
  Paragraph description;

  @UiField
  FlowPanel tagsPanel;

  @UiField
  NavLink timestamps;

  @UiField
  NavLink tableCount;

  @UiField
  NavLink variableCount;

  @UiField
  NavLink entityCount;

  @UiField
  OpalTabPanel tabPanel;

  @UiField
  Panel tablesPanel;

  @UiField
  Panel filesPanel;

  @UiField
  Panel reportsPanel;

  @UiField
  Panel tasksPanel;

  @UiField
  Panel adminPanel;

  @UiField
  FlowPanel permissionsPanel;

  @UiField
  FlowPanel encryptionKeysPanel;

  private ProjectDto project;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  @Inject
  ProjectView(Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    this.translations = translations;
    this.translationMessages = translationMessages;
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
    projectHeader.setSubtext("[" + project.getName() + "]");
    description.setText(project.getDescription());

    setTags();
    if(project.hasTimestamps()) setTimestamps(project.getTimestamps());
  }

  private void setTags() {
    tagsPanel.clear();
    JsArrayString tagsArray = JsArrays.toSafeArray(project.getTagsArray());
    if(tagsArray.length() > 0) {
      for(String tag : JsArrays.toIterable(tagsArray)) {
        tagsPanel.add(new Label(tag));
      }
    }
  }

  private void setTimestamps(TimestampsDto ts) {
    String lastUpdateOn = "?";
    if(ts.hasLastUpdate()) lastUpdateOn = Moment.create(ts.getLastUpdate()).fromNow();
    timestamps.setText(TranslationsUtils.replaceArguments(translations.lastUpdateOnLabel(), lastUpdateOn));
  }

  @Override
  public void setProjectSummary(ProjectSummaryDto projectSummary) {
    tableCount.setText(translationMessages.tableCount(projectSummary.getTableCount()));
    variableCount.setText(translationMessages.variableCount(projectSummary.getVariableCount()));
    entityCount.setText(translationMessages.entityCount(projectSummary.getEntityCount()));
    if(projectSummary.hasTimestamps()) setTimestamps(projectSummary.getTimestamps());
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

  @UiHandler("timestamps")
  void onTimestamps(ClickEvent event) {
    tabPanel.selectTab(ProjectTab.TABLES.ordinal());
  }

  @UiHandler("tableCount")
  void onTableCount(ClickEvent event) {
    tabPanel.selectTab(ProjectTab.TABLES.ordinal());
  }

  @UiHandler("variableCount")
  void onVariableCount(ClickEvent event) {
    tabPanel.selectTab(ProjectTab.TABLES.ordinal());
  }

  @UiHandler("entityCount")
  void onEntityCount(ClickEvent event) {
    tabPanel.selectTab(ProjectTab.TABLES.ordinal());
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
  @SuppressWarnings("PMD.NcssMethodCount")
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == ProjectPresenter.TABLES_PANE) {
      tablesPanel.clear();
      tablesPanel.add(content);
    } else if(slot == ProjectPresenter.FILES_PANE) {
      filesPanel.clear();
      filesPanel.add(content);
    } else if(slot == ProjectPresenter.REPORTS_PANE) {
      reportsPanel.clear();
      reportsPanel.add(content);
    } else if(slot == ProjectPresenter.TASKS_PANE) {
      tasksPanel.clear();
      tasksPanel.add(content);
    } else if(slot == ProjectPresenter.PERMISSION_PANE) {
      permissionsPanel.clear();
      permissionsPanel.add(content);
    } else if(slot == ProjectPresenter.PERMISSION_PANE) {
      permissionsPanel.clear();
      permissionsPanel.add(content);
    } else if(slot == ProjectPresenter.ADMIN_PANE) {
      adminPanel.clear();
      adminPanel.add(content);
    }
  }

}
