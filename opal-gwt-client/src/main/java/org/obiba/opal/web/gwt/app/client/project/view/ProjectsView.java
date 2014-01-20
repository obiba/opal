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

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsUiHandlers;
import org.obiba.opal.web.gwt.datetime.client.Moment;
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
import com.google.gwt.core.client.GWT;
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
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ProjectsView extends ViewWithUiHandlers<ProjectsUiHandlers> implements ProjectsPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectsView> {}

  private static final Translations translations = GWT.create(Translations.class);

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  private static final int DEFAULT_GRID_COLUMNS = 1;

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

  @Inject
  public ProjectsView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
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
    sortBy.sort(getUiHandlers(), activePanel, activeProjects);
    sort.setVisible(activeProjects.length()>0);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  private enum SortBy {
    NAME {
      @Override
      void sort(ProjectsUiHandlers handlers, Panel content, JsArray<ProjectDto> projects) {
        List<ProjectDto> projectList = JsArrays.toList(projects);
        Collections.sort(projectList, new Comparator<ProjectDto>() {
          @Override
          public int compare(ProjectDto o1, ProjectDto o2) {
            String m1 = o1.hasTitle() ? o1.getTitle() : o1.getName();
            String m2 = o2.hasTitle() ? o2.getTitle() : o2.getName();

            return m1.compareTo(m2);
          }
        });
        renderGrid(content, JsArrays.toList(projects), handlers);
      }
    }, LAST_UPDATE {
      @Override
      void sort(ProjectsUiHandlers handlers, Panel content, JsArray<ProjectDto> projects) {
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

        renderGrid(content, projectList, handlers);
      }
    };

    protected void renderGrid(Panel content, Collection<ProjectDto> projectList, ProjectsUiHandlers handlers) {
      int col = 0;
      FluidRow row = new FluidRow();
      int size = projectList.size();
      // for now there will be only one column until building cells with bootstrap 3 becomes easier
      int columns = size < DEFAULT_GRID_COLUMNS ? size : DEFAULT_GRID_COLUMNS;

      for(ProjectDto project : projectList) {
        Column column = new Column(12 / columns);
        Widget projectPanel = newProjectPanel(handlers, project, columns);
        column.add(projectPanel);
        if (col == columns -1) column.addStyleName("pull-right");
        if (col == columns) {
          content.add(row);
          row = new FluidRow();
          col = 0;
        }

        row.add(column);
        col++;
      }

      content.add(row);
    }

    /**
     * Perform the sort, add widgets to the panel and callback to project selection.
     *
     * @param handlers
     * @param content
     * @param projects
     */
    abstract void sort(ProjectsUiHandlers handlers, Panel content, JsArray<ProjectDto> projects);

    protected Widget newProjectPanel(ProjectsUiHandlers handlers, ProjectDto project, int columns) {
      Well w = new Well();
      FlowPanel panel = new FlowPanel();
      panel.add(newProjectLink(handlers, project));
      addTimestamps(project, panel);
      addTableNames(handlers, project, panel);
      addDescription(project, panel, columns);
      addTags(project, panel);
      w.add(panel);
      return w;
    }

    private void addTableNames(final ProjectsUiHandlers handlers, final ProjectDto project, FlowPanel panel) {
      JsArrayString tableNames = JsArrays.toSafeArray(project.getDatasource().getTableArray());
      if(tableNames.length() > 0) {
        Anchor countLabel = new Anchor("[" + translationMessages.tableCount(tableNames.length()) + "]");
        countLabel.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            handlers.onProjectTableSelection(project, null);
          }
        });
        countLabel.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            handlers.onProjectTableSelection(project, null);
          }
        });

        for(String table : JsArrays.toIterable(tableNames)) {
          if(Strings.isNullOrEmpty(countLabel.getTitle())) {
            countLabel.setTitle(table);
          } else {
            countLabel.setTitle(countLabel.getTitle() + ", " + table);
          }
        }
        panel.add(countLabel);
      }
    }

    private void addTimestamps(ProjectDto project, FlowPanel panel) {
      if(project.hasTimestamps() && project.getTimestamps().hasLastUpdate()) {
        FlowPanel timestampsPanel = new FlowPanel();
        timestampsPanel.addStyleName("pull-right");
        Moment lastUpdate = Moment.create(project.getTimestamps().getLastUpdate());
        InlineLabel ago = new InlineLabel(
            TranslationsUtils.replaceArguments(translations.lastUpdateAgoLabel(), lastUpdate.fromNow()));
        ago.addStyleName("project-timestamps");
        timestampsPanel.add(ago);
        panel.add(timestampsPanel);
      }
    }

    private void addTags(ProjectDto project, FlowPanel panel) {
      FlowPanel tagsPanel = new FlowPanel();
      List<String> tagList = JsArrays.toList(project.getTagsArray());
      if (tagList.isEmpty()) return;

      tagsPanel.addStyleName("inline-block");
      for(String tag : tagList) {
        Label tagLabel = new Label(tag);
        tagLabel.addStyleName("project-tag");
        tagsPanel.add(tagLabel);
      }
      panel.add(tagsPanel);
    }

    private void addDescription(ProjectDto project, FlowPanel panel, int columns) {
      FlowPanel panelDescription = new FlowPanel();
      if(project.hasDescription()) {
        // find first phrase
        String desc = project.getDescription();
        Paragraph descriptionLabel = new Paragraph(desc);
        panelDescription.add(descriptionLabel);
      }
      panelDescription.addStyleName("justified-paragraph");
      panel.add(panelDescription);
    }

    Widget newProjectLink(final ProjectsUiHandlers handlers, final ProjectDto project) {
      NavLink link = new NavLink(project.getTitle());
      link.setTitle(project.getName());
      link.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handlers.onProjectSelection(project);
        }
      });

      Heading head = new Heading(5);
      head.addStyleName("inline-block small-right-indent no-top-margin");
      head.add(link);

      return head;
    }
  }

}
