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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsUiHandlers;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ProjectsView extends ViewWithUiHandlers<ProjectsUiHandlers> implements ProjectsPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectsView> {}

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Panel activePanel;

  @UiField
  Panel archivedPanel;

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
    if(projects.length() == 0) {
      activePanel.clear();
      archivedPanel.clear();
    } else {
      redraw();
    }
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
    archivedPanel.clear();
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
    sortBy.sort(getUiHandlers(), archivedPanel, archivedProjects);
  }

  private enum SortBy {
    NAME {
      @Override
      void sort(ProjectsUiHandlers handlers, Panel content, JsArray<ProjectDto> projects) {
        for(ProjectDto project : JsArrays.toIterable(projects)) {
          content.add(newProjectPanel(handlers, project));
        }
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
            return m2.unix() - m1.unix();
          }
        });
        for(ProjectDto project : projectList) {
          content.add(newProjectPanel(handlers, project));
        }
      }
    };

    /**
     * Perform the sort, add widgets to the panel and callback to project selection.
     *
     * @param handlers
     * @param content
     * @param projects
     */
    abstract void sort(ProjectsUiHandlers handlers, Panel content, JsArray<ProjectDto> projects);

    protected Panel newProjectPanel(final ProjectsUiHandlers handlers, final ProjectDto project) {
      FlowPanel panel = new FlowPanel();
      panel.addStyleName("item");

      Widget projectLink = newProjectLink(handlers, project);
      panel.add(projectLink);

      FlowPanel tagsPanel = new FlowPanel();
      tagsPanel.addStyleName("tags inline-block");
      for(String tag : JsArrays.toIterable(JsArrays.toSafeArray(project.getTagsArray()))) {
        tagsPanel.add(new com.github.gwtbootstrap.client.ui.Label(tag));
      }
      panel.add(tagsPanel);

      JsArrayString tableNames = JsArrays.toSafeArray(project.getDatasource().getTableArray());
      if(tableNames.length() > 0) {
        Anchor countLabel = new Anchor(tableNames.length() == 1
            ? translations.tableCountLabel()
            : TranslationsUtils.replaceArguments(translations.tablesCountLabel(), "" + tableNames.length()));
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

      if(project.hasDescription()) {
        // find first phrase
        String desc = project.getDescription();
        int idx = desc.indexOf('.');
        if(idx > 0) desc = desc.substring(0, idx) + "...";
        Label descriptionLabel = new Label(desc);
        panel.add(descriptionLabel);
      }

      if(project.hasTimestamps()) {
        Moment lastUpdate = Moment.create(project.getTimestamps().getLastUpdate());
        Label ago = new Label(
            TranslationsUtils.replaceArguments(translations.lastUpdateAgoLabel(), lastUpdate.fromNow()));
        ago.addStyleName("help-block");
        panel.add(ago);
      }

      return panel;
    }

    protected Widget newProjectLink(final ProjectsUiHandlers handlers, final ProjectDto project) {
      NavLink link = new NavLink(project.getTitle());
      link.setTitle(project.getName());
      link.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handlers.onProjectSelection(project);
        }
      });

      Heading head = new Heading(4);
      head.addStyleName("inline-block small-right-indent");
      head.add(link);

      return head;
    }
  }

}
