package org.obiba.opal.web.gwt.app.client.project.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectUiHandlers;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
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
  HasText description;

  @UiField
  Button ellipsis;

  @UiField
  Panel tablesPanel;

  @UiField
  Breadcrumbs magmacrumbs;

  private ProjectDto project;

  @Inject
  ProjectView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setProject(ProjectDto project) {
    this.project = project;
    if(titlecrumbs.getWidgetCount() > 1) {
      titlecrumbs.remove(1);
    }
    titlecrumbs.add(new NavLink(project.getName()));
    String desc = project.getDescription();
    description.setText(desc.substring(0, desc.indexOf('.') + 1));
    ellipsis.setIcon(IconType.PLUS_SIGN);
  }

  @Override
  public void selectDatasource(String name) {
    magmacrumbs.clear();
    magmacrumbs.add(new InlineLabel(name));
  }

  @Override
  public void selectTable(String datasource, String table) {
    magmacrumbs.clear();
    magmacrumbs.add(newDatasourceLink(datasource));
    magmacrumbs.add(new InlineLabel(table));
  }

  @Override
  public void selectVariable(String datasource, String table, String variable) {
    magmacrumbs.clear();
    magmacrumbs.add(newDatasourceLink(datasource));
    magmacrumbs.add(newTableLink(datasource, table));
    magmacrumbs.add(new InlineLabel(variable));
  }

  @UiHandler("projects")
  void onProjectsSelection(ClickEvent event) {
    getUiHandlers().onProjectsSelection();
  }

  @UiHandler("ellipsis")
  void onEllipsis(ClickEvent event) {
    String desc = project.getDescription();
    if(desc.equals(description.getText())) {
      description.setText(desc.substring(0, desc.indexOf('.')));
      ellipsis.setIcon(IconType.PLUS_SIGN);
    } else {
      description.setText(desc);
      ellipsis.setIcon(IconType.MINUS_SIGN);
    }
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == ProjectPresenter.TABLES_PANE) {
      tablesPanel.clear();
      tablesPanel.add(content);
    }
  }

  private Widget newDatasourceLink(final String name) {
    NavLink link = new NavLink(name);
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
       getUiHandlers().onDatasourceSelection(name);
      }
    });
    return link;
  }

  private Widget newTableLink(final String datasource, final String table) {
    NavLink link = new NavLink(table);
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getUiHandlers().onTableSelection(datasource, table);
      }
    });
    return link;
  }

}
