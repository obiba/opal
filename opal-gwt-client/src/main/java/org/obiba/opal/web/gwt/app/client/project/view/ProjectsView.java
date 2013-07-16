package org.obiba.opal.web.gwt.app.client.project.view;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsUiHandlers;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.ProjectFactoryDto;

import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ProjectsView extends ViewWithUiHandlers<ProjectsUiHandlers> implements ProjectsPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectsView> {}

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Panel content;

  @UiField
  Modal m;

  @UiField
  Panel alertPlace;

  Alert alert;

  @UiField
  ControlGroup labelGroup;

  @UiField
  HasText nameTxt;

  @UiField
  HasText descriptionTxt;

  private SortBy sortBy = SortBy.NAME;

  private JsArray<ProjectDto> projects;

  @Inject
  ProjectsView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    // TODO translation
    m.setTitle(translations.addProject());
  }

  @Override
  public void setProjects(JsArray<ProjectDto> projects) {
    this.projects = projects;
    if(projects.length() == 0) {
      content.clear();
    } else {
      redraw();
    }
  }

  @Override
  public void setNameError(String message) {
    clearAlert();
    labelGroup.setType(ControlGroupType.ERROR);
    alert = new Alert(message);
    alert.setType(AlertType.ERROR);
    alert.setAnimation(true);
    alert.setClose(true);
    alert.addClosedHandler(new ClosedHandler() {
      @Override
      public void onClosed(ClosedEvent closedEvent) {
        clearAlert();
      }
    });
    alertPlace.add(alert);
  }

  @UiHandler("nameNav")
  void onSortByName(ClickEvent event) {
    if(sortBy != SortBy.NAME) {
      sortBy = SortBy.NAME;
      redraw();
    }
  }

  @UiHandler("tagNav")
  void onSortByTag(ClickEvent event) {
    if(sortBy != SortBy.TAG) {
      sortBy = SortBy.TAG;
      redraw();
    }
  }

  @UiHandler("add")
  void onShowAddProject(ClickEvent event) {
    // clear and show
    nameTxt.setText("");
    descriptionTxt.setText("");
    clearAlert();
    m.show();
  }

  @UiHandler("save")
  void onSaveProject(ClickEvent event) {
    // validate
    ProjectFactoryDto p = ProjectFactoryDto.create();
    p.setName(nameTxt.getText());
    p.setDescription(descriptionTxt.getText());
    if(getUiHandlers().onAddProject(p)) {
      m.hide();
    }
  }

  @UiHandler("cancel")
  void onCancelAddProject(ClickEvent event) {
    m.hide();
  }

  private void redraw() {
    content.clear();
    sortBy.sort(getUiHandlers(), content, projects);
  }

  private void clearAlert() {
    labelGroup.setType(ControlGroupType.NONE);
    if (alert != null && alert.getElement().hasParentElement()) {
      alert.removeFromParent();
    }
    alertPlace.clear();
  }

  private enum SortBy {
    NAME {
      @Override
      void sort(ProjectsUiHandlers handlers, Panel content, JsArray<ProjectDto> projects) {
        for(ProjectDto project : JsArrays.toIterable(projects)) {
          FlowPanel panel = new FlowPanel();
          panel.addStyleName("item");
          NavLink projectLink = newProjectLink(handlers, project);
          panel.add(projectLink);
          Label descriptionLabel = new Label(project.getDescription());
          panel.add(descriptionLabel);
          FlowPanel tagsPanel = new FlowPanel();
          tagsPanel.addStyleName("tags");
          for(String tag : JsArrays.toIterable(JsArrays.toSafeArray(project.getTagsArray()))) {
            tagsPanel.add(new com.github.gwtbootstrap.client.ui.Label(tag));
          }
          panel.add(tagsPanel);
          content.add(panel);
        }
      }
    }, TAG {
      @Override
      void sort(ProjectsUiHandlers handlers, Panel content, JsArray<ProjectDto> projects) {
        // get a unique list of tags
        Map<String, JsArray<ProjectDto>> tagMap = Maps.newHashMap();
        for(ProjectDto project : JsArrays.toIterable(projects)) {
          JsArrayString tags = JsArrays.toSafeArray(project.getTagsArray());
          if (tags.length() == 0) {
            addToTagMap(tagMap, "N/A", project);
          }
          for(String tag : JsArrays.toIterable(tags)) {
            addToTagMap(tagMap, tag, project);
          }
        }
        List<String> sortedTags = Lists.newArrayList(tagMap.keySet());
        Collections.sort(sortedTags, String.CASE_INSENSITIVE_ORDER);
        for(String tag : sortedTags) {
          AccordionGroup tagPanel = new AccordionGroup();
          tagPanel.setHeading(tag + " (" + tagMap.get(tag).length() + ")");
          tagPanel.addStyleName("item");
          FlowPanel tagContent = new FlowPanel();
          tagPanel.add(tagContent);
          content.add(tagPanel);
          NAME.sort(handlers, tagContent, tagMap.get(tag));
        }

      }

      private void addToTagMap(Map<String, JsArray<ProjectDto>> tagMap, String tag, ProjectDto project) {
        if(!tagMap.keySet().contains(tag)) {
          JsArray<ProjectDto> p = JsArrays.create();
          tagMap.put(tag, p);
        }
        tagMap.get(tag).push(project);
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

    protected NavLink newProjectLink(final ProjectsUiHandlers handlers, final ProjectDto project) {
      NavLink link = new NavLink(project.getName());
      link.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handlers.onProjectSelection(project);
        }
      });
      return link;
    }
  }

}
