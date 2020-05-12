/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.list;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.Strings;
import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.ui.NavTabsPanel;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;

public class ProjectsView extends ViewWithUiHandlers<ProjectsUiHandlers> implements ProjectsPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectsView> {}

  private static final int SORTABLE_COLUMN_NAME = 0;

  private static final int SORTABLE_COLUMN_LAST_UPDATED = 3;

  private static final short MAX_CHARACTERS = 100;

  @UiField
  NavTabsPanel tabs;

  @UiField
  OpalSimplePager tablePager;

  @UiField
  Table<ProjectDto> projectsTable;

  @UiField
  ButtonGroup addGroup;

  @UiField
  TextBoxClearable filter;

  private final ListDataProvider<ProjectDto> projectsDataProvider = new ListDataProvider<ProjectDto>();

  private final PlaceManager placeManager;

  private final Translations translations;

  private ListHandler<ProjectDto> typeSortHandler;

  private List<String> tags = Lists.newArrayList();

  private int selectedTagTab = 0;

  @Inject
  public ProjectsView(Binder uiBinder, PlaceManager placeManager, Translations translations) {
    this.placeManager = placeManager;
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initTabs();
    initProjectsTable();
    initializeFilter();
  }

  @Override
  public void beforeRenderProjects() {
    tablePager.setPagerVisible(false);
    projectsTable.showLoadingIndicator(projectsDataProvider);
  }

  @Override
  public void setTags(List<String> tags, String selectedTag) {
    this.tags = tags;
    tabs.clear();
    tabs.add(new SimplePanel(), new Anchor(translations.allProjectsLabel()));
    selectedTagTab = 0;
    if (!tags.isEmpty()) {
      int i = 0;
      for (String tag : tags) {
        if (tag.equals(selectedTag)) selectedTagTab = i + 1;
        tabs.add(new SimplePanel(), new Anchor(tag));
        i++;
      }
    }
    tabs.selectTab(selectedTagTab, false);
  }

  @Override
  public void setProjects(JsArray<ProjectDto> projects) {
    renderProjectsTable(JsArrays.toList(projects));
  }

  @Override
  public String getSelectedTag() {
    if (selectedTagTab>0) {
      int tagIdx = selectedTagTab - 1;
      String tag = tags.get(tagIdx);
      return tag;
    } else {
      return "";
    }
  }

  @Override
  public HasAuthorization getAddProjectAuthorizer() {
    return new WidgetAuthorizer(addGroup);
  }

  @UiHandler("add")
  void onShowAddProject(ClickEvent event) {
    getUiHandlers().showAddProject();
  }

  @UiHandler("filter")
  void onFilterUpdate(KeyUpEvent event) {
    getUiHandlers().onProjectsFilterUpdate(getSelectedTag(), filter.getText());
  }

  private void initTabs() {
    tabs.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        selectedTagTab = event.getSelectedItem();
        getUiHandlers().onProjectsFilterUpdate(getSelectedTag(), filter.getText());
      }
    });
  }

  private void initProjectsTable() {
    tablePager.setDisplay(projectsTable);
    projectsTable.addColumn(new NameColumn(new ProjectLinkCell(placeManager)), translations.nameLabel());
    projectsTable.addColumn(new TitleColumn() , translations.titleLabel());
    projectsTable.addColumn(new DescriptionColumn(), translations.descriptionLabel());
    projectsTable.addColumn(new LastUpdatedColumn(), translations.lastUpdatedLabel());
    projectsDataProvider.addDataDisplay(projectsTable);
    typeSortHandler = new ListHandler<ProjectDto>(projectsDataProvider.getList());
    typeSortHandler.setComparator(projectsTable.getColumn(SORTABLE_COLUMN_NAME), new NameComparator());
    typeSortHandler.setComparator(projectsTable.getColumn(SORTABLE_COLUMN_LAST_UPDATED), new LastUpdateComparator());

    projectsTable.getHeader(SORTABLE_COLUMN_NAME).setHeaderStyleNames("sortable-header-column");
    projectsTable.getHeader(SORTABLE_COLUMN_LAST_UPDATED).setHeaderStyleNames("sortable-header-column");
    projectsTable.getColumnSortList().push(projectsTable.getColumn(SORTABLE_COLUMN_LAST_UPDATED));
    projectsTable.getColumnSortList().push(projectsTable.getColumn(SORTABLE_COLUMN_NAME));
    projectsTable.addColumnSortHandler(typeSortHandler);
  }

  private void renderProjectsTable(List<ProjectDto> projects) {
    List<ProjectDto> projectDtos = Lists.newArrayList();
    String selectedTag = getSelectedTag();
    if (!selectedTag.isEmpty()) {
      for (ProjectDto project : projects) {
        List<String> tags = JsArrays.toList(project.getTagsArray());
        if (tags.contains(selectedTag)) projectDtos.add(project);
      }
    } else {
      projectDtos = projects;
    }
    projectsTable.hideLoadingIndicator();
    projectsDataProvider.setList(projectDtos);
    tablePager.firstPage();
    projectsDataProvider.refresh();
    tablePager.setPagerVisible(projectsDataProvider.getList().size() > tablePager.getPageSize());
    typeSortHandler.setList(projectsDataProvider.getList());
    ColumnSortEvent.fire(projectsTable, projectsTable.getColumnSortList());
  }

  private void initializeFilter() {
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterProjects());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
  }

  private static class ProjectLinkCell extends PlaceRequestCell<ProjectDto> {

    private ProjectLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(ProjectDto projectDto) {
      return ProjectPlacesHelper.getDashboardPlace(projectDto.getName());
    }

    @Override
    public String getText(ProjectDto projectDto) {
      return projectDto.getName();
    }
  }

  private static final class NameColumn extends Column<ProjectDto, ProjectDto> {

    private NameColumn(ProjectLinkCell cell) {
      super(cell);
      //TODO render bookmark icon
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public ProjectDto getValue(ProjectDto projectDto) {
      return projectDto;
    }

  }

  private static final class TitleColumn extends TextColumn<ProjectDto> {

    @Override
    public String getValue(ProjectDto projectDto) {
      return projectDto.getTitle();
    }
  }

  private static final class DescriptionColumn extends TextColumn<ProjectDto> {

    @Override
    public String getValue(ProjectDto projectDto) {
      return Strings.abbreviate(projectDto.getDescription(), MAX_CHARACTERS);
    }
  }

  private static final class LastUpdatedColumn extends TextColumn<ProjectDto> {

    private LastUpdatedColumn() {
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public String getValue(ProjectDto projectDto) {
      return projectDto.hasTimestamps() && projectDto.getTimestamps().hasLastUpdate() //
          ? Moment.create(projectDto.getTimestamps().getLastUpdate()).fromNow() //
          : "";

    }
  }

  private static final class NameComparator implements Comparator<ProjectDto> {
    @Override
    public int compare(ProjectDto o1, ProjectDto o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  private static final class LastUpdateComparator implements Comparator<ProjectDto> {
    @Override
    public int compare(ProjectDto o1, ProjectDto o2) {
      Moment m1 = Moment.create(o1.getTimestamps().getLastUpdate());
      Moment m2 = Moment.create(o2.getTimestamps().getLastUpdate());
      if(m1 == null) {
        return m2 == null ? 0 : 1;
      }
      return m2 == null ? -1 : m2.unix() - m1.unix();
    }
  }
}

