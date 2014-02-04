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

import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsUiHandlers;
import org.obiba.opal.web.gwt.app.client.support.Strings;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import static com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;

public class ProjectsView extends ViewWithUiHandlers<ProjectsUiHandlers> implements ProjectsPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectsView> {}

  private static final int SORTABLE_COLUMN_NAME = 0;
  private static final int SORTABLE_COLUMN_LAST_UPDATED = 2;
  private static final short MAX_CHARACTERS = 100;

  @UiField
  SimplePager tablePager;

  @UiField
  Table<ProjectDto> projectsTable;

  private static final Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<ProjectDto> projectsDataProvider = new ListDataProvider<ProjectDto>();

  private final PlaceManager placeManager;

  private ListHandler<ProjectDto> typeSortHandler;

  private JsArray<ProjectDto> projects;

  @Inject
  public ProjectsView(Binder uiBinder, PlaceManager placeManager) {
    this.placeManager = placeManager;
    initWidget(uiBinder.createAndBindUi(this));
    initProjectsTable();
  }

  @Override
  public void setProjects(JsArray<ProjectDto> projects) {
    this.projects = projects;
    redraw();
    renderProjectsTable(JsArrays.toList(projects));
  }

  @UiHandler("add")
  void onShowAddProject(ClickEvent event) {
    getUiHandlers().showAddProject();
  }

  private void initProjectsTable() {
    tablePager.setDisplay(projectsTable);
    projectsTable.addColumn(new NameColumn(new ProjectLinkCell(placeManager)), translations.nameLabel());
    projectsTable.addColumn(new DescriptionColumn(), translations.descriptionLabel());
    projectsTable.addColumn(new LastUpdatedColumn(), translations.lastUpdatedLabel());
    projectsDataProvider.addDataDisplay(projectsTable);
    typeSortHandler = new ListHandler<ProjectDto>(projectsDataProvider.getList());
    typeSortHandler.setComparator(projectsTable.getColumn(SORTABLE_COLUMN_NAME), new NameComparator());
    typeSortHandler.setComparator(projectsTable.getColumn(SORTABLE_COLUMN_LAST_UPDATED), new LastUpdateComparator());

    projectsTable.getHeader(SORTABLE_COLUMN_NAME).setHeaderStyleNames("sortable-header-column");
    projectsTable.getHeader(SORTABLE_COLUMN_LAST_UPDATED).setHeaderStyleNames("sortable-header-column");

    projectsTable.addColumnSortHandler(typeSortHandler);
  }

  private void renderProjectsTable(List<ProjectDto> projectDtos) {
    projectsDataProvider.setList(projectDtos);
    tablePager.firstPage();
    projectsDataProvider.refresh();
    tablePager.setVisible(projectsDataProvider.getList().size() > tablePager.getPageSize());
    typeSortHandler.setList(projectsDataProvider.getList());
    projectsTable.getColumnSortList().push(projectsTable.getColumn(SORTABLE_COLUMN_LAST_UPDATED));
    projectsTable.getColumnSortList().push(projectsTable.getColumn(SORTABLE_COLUMN_NAME));
    ColumnSortEvent.fire(projectsTable, projectsTable.getColumnSortList());
  }

  private class ProjectLinkCell extends PlaceRequestCell<ProjectDto> {

    private ProjectLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(ProjectDto projectDto) {
      return ProjectPlacesHelper.getTablesPlace(projectDto.getName());
    }

    @Override
    public String getText(ProjectDto projectDto) {
      return projectDto.hasTitle() ? projectDto.getTitle() : projectDto.getName();
    }
  }

  private final class NameColumn extends Column<ProjectDto, ProjectDto> {

    private NameColumn(ProjectLinkCell cell) {
      super(cell);
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public ProjectDto getValue(ProjectDto projectDto) {
      return projectDto;
    }
  }

  private final class DescriptionColumn extends TextColumn<ProjectDto> {

    @Override
    public String getValue(ProjectDto projectDto) {
      return Strings.abbreviate(projectDto.getDescription(), MAX_CHARACTERS);
    }
  }

  private final class LastUpdatedColumn extends TextColumn<ProjectDto> {

    private LastUpdatedColumn() {
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public String getValue(ProjectDto projectDto) {
      return Moment.create(projectDto.getTimestamps().getLastUpdate()).fromNow();
    }
  }

  private void redraw() {
    JsArray<ProjectDto> activeProjects = JsArrays.create();
    JsArray<ProjectDto> archivedProjects = JsArrays.create();
    for(ProjectDto project : JsArrays.toIterable(projects)) {
      if(project.hasArchived() && project.getArchived()) {
        archivedProjects.push(project);
      } else {
        activeProjects.push(project);
      }
    }
  }

  private static final class NameComparator implements Comparator<ProjectDto> {
    @Override
    public int compare(ProjectDto o1, ProjectDto o2) {
      String m1 = o1.hasTitle() ? o1.getTitle() : o1.getName();
      String m2 = o2.hasTitle() ? o2.getTitle() : o2.getName();

      return m1.compareTo(m2);
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

