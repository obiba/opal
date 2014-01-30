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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.bookmark.icon.BookmarkIconPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsUiHandlers;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.Well;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ProjectsView extends ViewWithUiHandlers<ProjectsUiHandlers> implements ProjectsPresenter.Display {

  private static final int NB_GRID_COLUMNS = 12;

  private static final int DEFAULT_GRID_COLUMNS = 1;

  interface Binder extends UiBinder<Widget, ProjectsView> {}

  @UiField
  Panel activePanel;

  @UiField
  DropdownButton sort;

  @UiField
  NavLink nameNav;

  @UiField
  NavLink lastUpdateNav;

  private SortBy sortBy = SortBy.LAST_UPDATE;

  private JsArray<ProjectDto> projects;

  private final TranslationMessages translationMessages;

  private final Provider<BookmarkIconPresenter> bookmarkIconPresenterProvider;

  @Inject
  public ProjectsView(Binder uiBinder, TranslationMessages translationMessages,
      Provider<BookmarkIconPresenter> bookmarkIconPresenterProvider) {
    this.bookmarkIconPresenterProvider = bookmarkIconPresenterProvider;
    initWidget(uiBinder.createAndBindUi(this));
    this.translationMessages = translationMessages;
  }

  @Override
  public void setProjects(JsArray<ProjectDto> projects) {
    this.projects = projects;
    redraw();
  }

  @UiHandler("nameNav")
  void onSortByName(ClickEvent event) {
    if(sortBy != SortBy.NAME) {
      sortBy = SortBy.NAME;
      nameNav.setIcon(IconType.OK);
      nameNav.removeStyleName("no-icon");
      lastUpdateNav.setIcon(null);
      lastUpdateNav.addStyleName("no-icon");
      redraw();
    }
  }

  @UiHandler("lastUpdateNav")
  void onSortByLastUpdate(ClickEvent event) {
    if(sortBy != SortBy.LAST_UPDATE) {
      sortBy = SortBy.LAST_UPDATE;
      lastUpdateNav.setIcon(IconType.OK);
      lastUpdateNav.removeStyleName("no-icon");
      nameNav.setIcon(null);
      nameNav.addStyleName("no-icon");
      redraw();
    }
  }

  @UiHandler("add")
  void onShowAddProject(ClickEvent event) {
    getUiHandlers().showAddProject();
  }

  private void redraw() {
    activePanel.clear();
    JsArray<ProjectDto> activeProjects = JsArrays.create();
    JsArray<ProjectDto> archivedProjects = JsArrays.create();
    for(ProjectDto project : JsArrays.toIterable(projects)) {
      if(project.hasArchived() && project.getArchived()) {
        archivedProjects.push(project);
      } else {
        activeProjects.push(project);
      }
    }
    renderGrid(sortBy.sort(activeProjects));
    sort.setVisible(activeProjects.length() > 0);
  }

  protected void renderGrid(Collection<ProjectDto> sortedProjects) {
    int col = 0;
    FluidRow row = new FluidRow();
    int size = sortedProjects.size();
    // for now there will be only one column until building cells with bootstrap 3 becomes easier
    int columns = size < DEFAULT_GRID_COLUMNS ? size : DEFAULT_GRID_COLUMNS;

    for(ProjectDto project : sortedProjects) {
      Column column = new Column(NB_GRID_COLUMNS / columns);
      column.add(newProjectPanel(project));
      if(col == columns - 1) column.addStyleName("pull-right");
      if(col == columns) {
        activePanel.add(row);
        row = new FluidRow();
        col = 0;
      }

      row.add(column);
      col++;
    }

    activePanel.add(row);
  }

  private Widget newProjectPanel(ProjectDto project) {
    FlowPanel panel = new FlowPanel();
    panel.add(newProjectLink(project));

    FlowPanel timestamps = getTimestampsWidget(project);
    if(timestamps != null) panel.add(timestamps);

    Anchor tableNames = getTableNamesWidget(project);
    if(tableNames != null) panel.add(tableNames);

    panel.add(getDescriptionWidget(project));

    FlowPanel tags = getTagsWidget(project);
    if(tags != null) panel.add(tags);

    Well well = new Well();
    well.add(panel);
    return well;
  }

  @Nullable
  private Anchor getTableNamesWidget(final ProjectDto project) {
    JsArrayString tableNames = JsArrays.toSafeArray(project.getDatasource().getTableArray());
    if(tableNames.length() == 0) return null;

    Anchor countLabel = new Anchor("[" + translationMessages.tableCount(tableNames.length()) + "]");
    countLabel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getUiHandlers().onProjectTableSelection(project, null);
      }
    });
    countLabel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getUiHandlers().onProjectTableSelection(project, null);
      }
    });

    for(String table : JsArrays.toIterable(tableNames)) {
      if(Strings.isNullOrEmpty(countLabel.getTitle())) {
        countLabel.setTitle(table);
      } else {
        countLabel.setTitle(countLabel.getTitle() + ", " + table);
      }
    }
    return countLabel;
  }

  @Nullable
  private FlowPanel getTimestampsWidget(ProjectDto project) {
    if(project.hasTimestamps() && project.getTimestamps().hasLastUpdate()) {
      FlowPanel timestampsPanel = new FlowPanel();
      timestampsPanel.addStyleName("pull-right");
      Moment lastUpdate = Moment.create(project.getTimestamps().getLastUpdate());
      InlineLabel ago = new InlineLabel(translationMessages.lastUpdateAgoLabel(lastUpdate.fromNow()));
      ago.addStyleName("project-timestamps");
      timestampsPanel.add(ago);
      return timestampsPanel;
    }
    return null;
  }

  @Nullable
  private FlowPanel getTagsWidget(ProjectDto project) {
    List<String> tagList = JsArrays.toList(project.getTagsArray());
    if(tagList.isEmpty()) return null;

    FlowPanel tagsPanel = new FlowPanel();
    tagsPanel.addStyleName("inline-block");
    for(String tag : tagList) {
      Label tagLabel = new Label(tag);
      tagLabel.addStyleName("project-tag");
      tagsPanel.add(tagLabel);
    }
    return tagsPanel;
  }

  private FlowPanel getDescriptionWidget(ProjectDto project) {
    FlowPanel panelDescription = new FlowPanel();
    if(project.hasDescription()) {
      // find first phrase
      String desc = project.getDescription();
      Paragraph descriptionLabel = new Paragraph(desc);
      panelDescription.add(descriptionLabel);
    }
    panelDescription.addStyleName("justified-paragraph");
    return panelDescription;
  }

  private Widget newProjectLink(final ProjectDto project) {
    Anchor link = new Anchor(project.getTitle());
    link.setTitle(project.getName());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getUiHandlers().onProjectSelection(project);
      }
    });

    Heading head = new Heading(5);
    head.addStyleName("inline-block small-right-indent no-top-margin");
    head.add(getBookmarkIconWidget(project));
    head.add(link);
    return head;
  }

  private Widget getBookmarkIconWidget(ProjectDto project) {
    BookmarkIconPresenter bookmarkIconPresenter = bookmarkIconPresenterProvider.get();
    bookmarkIconPresenter.setBookmarkable(UriBuilders.DATASOURCE.create().build(project.getName()));
    bookmarkIconPresenter.addStyleName("small-right-indent");
    return bookmarkIconPresenter.asWidget();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  private enum SortBy {
    NAME {
      @Override
      List<ProjectDto> sort(JsArray<ProjectDto> projects) {
        List<ProjectDto> projectList = JsArrays.toList(projects);
        Collections.sort(projectList, new Comparator<ProjectDto>() {
          @Override
          public int compare(ProjectDto o1, ProjectDto o2) {
            String m1 = o1.hasTitle() ? o1.getTitle() : o1.getName();
            String m2 = o2.hasTitle() ? o2.getTitle() : o2.getName();
            return m1.compareTo(m2);
          }
        });
        return projectList;
      }
    }, //
    LAST_UPDATE {
      @Override
      List<ProjectDto> sort(JsArray<ProjectDto> projects) {
        List<ProjectDto> projectList = JsArrays.toList(projects);
        Collections.sort(projectList, new Comparator<ProjectDto>() {
          @Override
          public int compare(ProjectDto o1, ProjectDto o2) {
            Moment m1 = Moment.create(o1.getTimestamps().getLastUpdate());
            Moment m2 = Moment.create(o2.getTimestamps().getLastUpdate());
            if(m1 == null) {
              return m2 == null ? 0 : 1;
            }
            return m2 == null ? -1 : m2.unix() - m1.unix();
          }
        });
        return projectList;
      }
    };

    abstract List<ProjectDto> sort(JsArray<ProjectDto> projects);

  }

}
