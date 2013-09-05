/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.presenter;

import java.util.Arrays;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.MagmaPathSelectionEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.app.client.ui.HasTabPanel;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.common.base.Strings;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class ProjectPresenter extends Presenter<ProjectPresenter.Display, ProjectPresenter.Proxy>
    implements ProjectUiHandlers, DatasourceSelectionChangeEvent.Handler, TableSelectionChangeEvent.Handler,
    VariableSelectionChangeEvent.Handler {

  public interface Display extends View, HasUiHandlers<ProjectUiHandlers>, HasTabPanel {

    enum ProjectTab {
      HOME,
      TABLES,
      FILES,
      VISUALISATION,
      REPORTS,
      TASKS,
      PERMISSIONS,
      ADMINISTRATION
    }

    void setProject(ProjectDto project);
  }

  @ProxyStandard
  @NameToken(Places.PROJECT)
  public interface Proxy extends ProxyPlace<ProjectPresenter> {}

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> TABLES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> FILES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> ADMIN_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  private final Provider<MagmaPresenter> magmaPresenterProvider;

  private final Provider<FileExplorerPresenter> fileExplorerPresenterProvider;

  private final Provider<ProjectAdministrationPresenter> projectAdministrationPresenterProvider;

  private final PlaceManager placeManager;

  private String name;

  private Display.ProjectTab tab = Display.ProjectTab.TABLES;

  private ProjectDto project;

  private MagmaPresenter magmaPresenter;

  private FileExplorerPresenter fileExplorerPresenter;

  private ProjectAdministrationPresenter projectAdministrationPresenter;

  @Inject
  public ProjectPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
      PlaceManager placeManager, Provider<MagmaPresenter> magmaPresenterProvider,
      Provider<FileExplorerPresenter> fileExplorerPresenterProvider,
      Provider<ProjectAdministrationPresenter> projectAdministrationPresenterProvider) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    this.magmaPresenterProvider = magmaPresenterProvider;
    this.fileExplorerPresenterProvider = fileExplorerPresenterProvider;
    this.projectAdministrationPresenterProvider = projectAdministrationPresenterProvider;
  }

  @Override
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(DatasourceSelectionChangeEvent.getType(), this);
    addRegisteredHandler(TableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(VariableSelectionChangeEvent.getType(), this);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    name = request.getParameter(ParameterTokens.TOKEN_NAME, null);
    tab = validateTab(request.getParameter(ParameterTokens.TOKEN_TAB, null));
    String path = validatePath(request.getParameter(ParameterTokens.TOKEN_PATH, null));

    if(tab == Display.ProjectTab.TABLES) {
      // TODO check that datasource name is the one of project
      getView().setTabData(tab.ordinal(), path);
    } else {
      getView().setTabData(tab.ordinal(), null);
    }

    refresh();
  }

  public void refresh() {
    // TODO handle wrong or missing project name
    if(name == null) return;
    ResourceRequestBuilderFactory.<ProjectDto>newBuilder().forResource(UriBuilder.URI_PROJECT.build(name)).get()
        .withCallback(new ResourceCallback<ProjectDto>() {
          @Override
          public void onResource(Response response, ProjectDto resource) {
            project = resource;
            // TODO check project is null
            getView().setProject(project);
            onTabSelected(tab.ordinal());
            getView().selectTab(tab.ordinal());
          }
        }).send();
  }

  @Override
  public void onProjectsSelection() {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.PROJECTS).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onTabSelected(int index) {
    tab = Display.ProjectTab.values()[index];
    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Arrays.asList(ParameterTokens.TOKEN_NAME));
    builder.with(ParameterTokens.TOKEN_TAB, tab.toString());
    String queryPathParam = (String) getView().getTabData(index);

    switch(tab) {
      case TABLES:
        onTablesTabSelected(queryPathParam);
        break;
      case FILES:
        onFilesTabSelected(queryPathParam);
        break;
      case ADMINISTRATION:
        onAdminTabSelected();
        break;
    }

    if(!Strings.isNullOrEmpty(queryPathParam)) {
      builder.with(ParameterTokens.TOKEN_PATH, queryPathParam);
    }

    placeManager.updateHistory(builder.build(), true);
  }

  private void onTablesTabSelected(String path) {
    if(magmaPresenter == null) {
      magmaPresenter = magmaPresenterProvider.get();
      setInSlot(TABLES_PANE, magmaPresenter);
    }
    if(Strings.isNullOrEmpty(path)) {
      fireEvent(new MagmaPathSelectionEvent(this, project.getDatasource().getName()));
    } else {
      fireEvent(new MagmaPathSelectionEvent(this, path));
    }
  }

  private void onFilesTabSelected(String path) {
    if(fileExplorerPresenter == null) {
      fileExplorerPresenter = fileExplorerPresenterProvider.get();
      setInSlot(FILES_PANE, fileExplorerPresenter);
    }
    // TODO set project to home the first time tab is visited for this project
  }

  private void onAdminTabSelected() {
    if(projectAdministrationPresenter == null) {
      projectAdministrationPresenter = projectAdministrationPresenterProvider.get();
      setInSlot(ADMIN_PANE, projectAdministrationPresenter);
    }
    projectAdministrationPresenter.setProject(project);
  }

  @Override
  public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
    if(event.getSource() == this) return;
    updateHistory(null);
  }

  @Override
  public void onTableSelectionChanged(TableSelectionChangeEvent event) {
    if(event.getSource() == this) return;
    updateHistory(MagmaPath.Builder.datasource(event.getDatasourceName()).table(event.getTableName()).build());
  }

  @Override
  public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
    if(event.getSource() == this) return;
    updateHistory(new MagmaPath.Builder().datasource(event.getDatasourceName()).table(event.getTableName())
        .variable(event.getVariableName()).build());
  }

  private void updateHistory(String queryPathParam) {
    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Arrays.asList(ParameterTokens.TOKEN_NAME, ParameterTokens.TOKEN_TAB));

    if(!Strings.isNullOrEmpty(queryPathParam)) {
      builder.with(ParameterTokens.TOKEN_PATH, queryPathParam).build();
    }

    placeManager.updateHistory(builder.build(), true);
    getView().setTabData(tab.ordinal(), queryPathParam);
  }

  private Display.ProjectTab validateTab(String tab) {
    if(!Strings.isNullOrEmpty(tab)) {
      try {
        return Display.ProjectTab.valueOf(tab);
      } catch(IllegalArgumentException ignored) {
      }
    }

    return Display.ProjectTab.HOME;
  }

  private String validatePath(String path) {
    if(tab == Display.ProjectTab.TABLES && !Strings.isNullOrEmpty(path)) {
      MagmaPath.Parser parser = new MagmaPath.Parser().parse(path);
      String datasourceName = parser.getDatasource();
      if(!Strings.isNullOrEmpty(datasourceName) && name.equals(datasourceName)) {
        return path;
      }
    }

    updateHistory(null);
    return null;
  }
}
