package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.project.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.project.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
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

public class ProjectPresenter extends Presenter<ProjectPresenter.Display, ProjectPresenter.Proxy> implements ProjectUiHandlers, DatasourceSelectionChangeEvent.Handler,
    TableSelectionChangeEvent.Handler, VariableSelectionChangeEvent.Handler {

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> TABLES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> FILES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> ADMIN_PANE = new GwtEvent.Type<RevealContentHandler<?>>();


  private final FileExplorerPresenter fileExplorerPresenter;

  private final PlaceManager placeManager;

  private String name;

  private ProjectDto project;

  private final Translations translations;

  @Inject
  public ProjectPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
      PlaceManager placeManager, FileExplorerPresenter fileExplorerPresenter) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.translations = translations;
    this.placeManager = placeManager;
    this.fileExplorerPresenter = fileExplorerPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(DatasourceSelectionChangeEvent.getType(), this);
    addRegisteredHandler(TableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(VariableSelectionChangeEvent.getType(), this);

    setInSlot(FILES_PANE, fileExplorerPresenter);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    name = request.getParameter(ParameterTokens.TOKEN_NAME, null);
    refresh();
  }

  public void refresh() {
    if (name == null) return;
    // TODO handle wrong or missing id
    UriBuilder builder = UriBuilder.create().segment("project", name);
    ResourceRequestBuilderFactory.<ProjectDto>newBuilder().forResource(builder.build()).get()
        .withCallback(new ResourceCallback<ProjectDto>() {
          @Override
          public void onResource(Response response, ProjectDto resource) {
            project = resource;
            getView().setProject(project);
            getEventBus().fireEvent(new DatasourceSelectionChangeEvent(project.getDatasource()));
          }
        }).send();
  }

  @Override
  public void onProjectsSelection() {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.projects).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onDatasourceSelection(String name) {
    getEventBus().fireEvent(new DatasourceSelectionChangeEvent(name));
  }

  @Override
  public void onTableSelection(String datasource, String table) {
    getEventBus().fireEvent(new TableSelectionChangeEvent(this, datasource, table));
  }

  @Override
  public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
    getView().selectDatasource(event.getSelection());
  }

  @Override
  public void onTableSelectionChanged(TableSelectionChangeEvent event) {
    getView().selectTable(event.getDatasourceName(), event.getTableName());
  }

  @Override
  public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
    getView().selectVariable(event.getDatasourceName(), event.getTableName(), event.getVariableName());
  }

  public interface Display extends View, HasUiHandlers<ProjectUiHandlers> {

    void setProject(ProjectDto project);

    void selectDatasource(String name);

    void selectTable(String datasource, String table);

    void selectVariable(String datasource, String table, String variable);
  }

  @ProxyStandard
  @NameToken(Places.project)
  public interface Proxy extends ProxyPlace<ProjectPresenter> {}
}
