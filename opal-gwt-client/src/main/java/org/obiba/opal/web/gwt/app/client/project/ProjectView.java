/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.search.event.SearchDatasourceVariablesEvent;
import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalTabPanel;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.TabPanelAuthorizer;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.ProjectSummaryDto;

public class ProjectView extends ViewWithUiHandlers<ProjectUiHandlers> implements ProjectPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectView> {
  }

  private final PlaceManager placeManager;

  @UiField
  Breadcrumbs titleCrumbs;

  @UiField
  FlowPanel bookmarkIcon;

  @UiField
  FlowPanel tagsPanel;

  @UiField
  Heading title;

  @UiField
  HelpBlock description;

  @UiField
  OpalTabPanel tabPanel;

  @UiField
  Panel dashboardPanel;

  @UiField
  Panel datasourceStatus;

  @UiField
  Icon datasourceStatusIcon;

  @UiField
  Label datasourceStatusText;

  @UiField
  Button tableCount;

  @UiField
  Button variableCount;

  @UiField
  Button resourceCount;

  @UiField
  Panel tablesPanel;

  @UiField
  Panel filesPanel;

  @UiField
  Panel resourcesPanel;

  @UiField
  Panel genotypesPanel;

  @UiField
  Panel reportsPanel;

  @UiField
  Panel tasksPanel;

  @UiField
  Panel adminPanel;

  @UiField
  FlowPanel permissionsPanel;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  @Inject
  ProjectView(Binder uiBinder, PlaceManager placeManager, Translations translations, TranslationMessages translationMessages) {
    this.placeManager = placeManager;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    for (ProjectTab tab : ProjectTab.values()) {
      String title = translations.projectTabNameMap().get(tab.toString());
      TabPanelHelper.setTabTitle(tabPanel, tab.ordinal(), title);
    }
    tabPanel.addStyleName("off-content");
  }

  @Override
  public void clearTabsData() {
    tabPanel.clearData();
  }

  @Override
  public boolean isTabVisible(int index) {
    return tabPanel.isTabVisible(index);
  }

  @Override
  public void setProject(ProjectDto project) {
    if (titleCrumbs.getWidgetCount() > 1) {
      titleCrumbs.remove(1);
    }
    titleCrumbs.add(new NavLink(project.getName()));
    setTags(project);
    title.setText(project.hasTitle() ? project.getTitle() : "");
    title.setVisible(project.hasTitle() && !project.getTitle().equals(project.getName()));
    description.setVisible(project.hasDescription());
    description.setText(project.hasDescription() ? project.getDescription() : "");
    tableCount.setText("-");
    tableCount.setHref("#" + placeManager.buildHistoryToken(ProjectPlacesHelper.getDatasourcePlace(project.getName())));
    variableCount.setText("-");
    //variableCount.setHref("#" + placeManager.buildHistoryToken(ProjectPlacesHelper.getDatasourcePlace(project.getName())));
    resourceCount.setText("-");
    resourceCount.setHref("#" + placeManager.buildHistoryToken(ProjectPlacesHelper.getResourcesPlace(project.getName())));
    setProjectStatus(project.getDatasourceStatus().getName());
  }

  @Override
  public void setProjectSummary(ProjectSummaryDto projectSummary) {
    String viewCountTxt = projectSummary.getViewCount() > 0 ? translationMessages.nViewCountLabel(projectSummary.getViewCount()) : "";
    tableCount.setText(projectSummary.getTableCount() + " " + viewCountTxt);
    String derivedVariableCountTxt = projectSummary.getDerivedVariableCount() > 0 ? translationMessages.nDerivedVariableCountLabel(projectSummary.getDerivedVariableCount()) : "";
    variableCount.setText(projectSummary.getVariableCount() + " " + derivedVariableCountTxt);
    resourceCount.setText("" + projectSummary.getResourceCount());
    setProjectStatus(projectSummary.getDatasourceStatus().getName());
  }

  private void setProjectStatus(String status) {
    datasourceStatusIcon.removeStyleName("text-success");
    datasourceStatusIcon.removeStyleName("text-warning");
    datasourceStatusIcon.removeStyleName("text-error");
    datasourceStatusIcon.removeStyleName("text-info");
    datasourceStatusText.setText(translations.datasourceStatusDescriptionsMap().get(status));
    switch (status) {
      case "READY":
        datasourceStatusIcon.addStyleName("text-success");
        break;
      case "BUSY":
        datasourceStatusIcon.addStyleName("text-warning");
        break;
      case "LOADING":
        datasourceStatusIcon.addStyleName("text-error");
        break;
      default:
        datasourceStatusIcon.addStyleName("text-info");
    }
    datasourceStatus.setVisible(true);

  }

  private void setTags(ProjectDto project) {
    tagsPanel.clear();
    JsArrayString tagsArray = JsArrays.toSafeArray(project.getTagsArray());
    tagsPanel.setVisible(tagsArray.length() > 0);
    if (tagsArray.length() > 0) {
      for (final String tag : JsArrays.toIterable(tagsArray)) {
        Anchor tagLabel = new Anchor(tag);
        tagLabel.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            getUiHandlers().onProjectsSelection(tag);
          }
        });
        tagLabel.addStyleName("small-indent label");
        tagsPanel.add(tagLabel);
      }
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

  @Override
  public HasAuthorization getTablesAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, ProjectTab.TABLES.ordinal());
  }

  @Override
  public HasAuthorization getResourcesAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, ProjectTab.RESOURCES.ordinal());
  }

  @Override
  public HasAuthorization getGenotypesAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, ProjectTab.GENOTYPES.ordinal());
  }

  @Override
  public HasAuthorization getReportsAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, ProjectTab.REPORTS.ordinal());
  }

  @Override
  public HasAuthorization getTasksAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, ProjectTab.TASKS.ordinal());
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, ProjectTab.PERMISSIONS.ordinal());
  }

  @UiHandler("projects")
  void onProjectsSelection(ClickEvent event) {
    getUiHandlers().onProjectsSelection(null);
  }

  @UiHandler("tabPanel")
  void onShown(TabPanel.ShownEvent shownEvent) {
    if (shownEvent.getTarget() == null) return;
    getUiHandlers().onTabSelected(tabPanel.getSelectedTab());
  }

  @UiHandler("variableCount")
  void onSearchVariables(ClickEvent event) {
    getUiHandlers().onSearchVariables();
  }

  @Override
  @SuppressWarnings({"PMD.NcssMethodCount", "IfStatementWithTooManyBranches", "OverlyLongMethod"})
  public void setInSlot(Object slot, IsWidget content) {
    if (slot == ProjectPresenter.TABLES_PANE) {
      tablesPanel.clear();
      tablesPanel.add(content);
    } else if (slot == ProjectPresenter.FILES_PANE) {
      filesPanel.clear();
      filesPanel.add(content);
    } else if (slot == ProjectPresenter.RESOURCES_PANE) {
      resourcesPanel.clear();
      resourcesPanel.add(content);
    } else if (slot == ProjectPresenter.GENOTYPES_PANE) {
      genotypesPanel.clear();
      genotypesPanel.add(content);
    } else if (slot == ProjectPresenter.REPORTS_PANE) {
      reportsPanel.clear();
      reportsPanel.add(content);
    } else if (slot == ProjectPresenter.TASKS_PANE) {
      tasksPanel.clear();
      tasksPanel.add(content);
    } else if (slot == ProjectPresenter.PERMISSION_PANE) {
      permissionsPanel.clear();
      permissionsPanel.add(content);
    } else if (slot == ProjectPresenter.ADMIN_PANE) {
      adminPanel.clear();
      adminPanel.add(content);
    } else if (slot == ProjectPresenter.BOOKMARK_ICON) {
      bookmarkIcon.clear();
      bookmarkIcon.add(content);
    }
  }

}
