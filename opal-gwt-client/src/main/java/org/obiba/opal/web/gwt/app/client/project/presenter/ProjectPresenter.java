package org.obiba.opal.web.gwt.app.client.project.presenter;

import java.util.Arrays;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
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
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
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

    public enum ProjectTab {
      tables,
      files,
      visualisation,
      reports,
      tasks,
      permissions,
      administration;
    }

    void setProject(ProjectDto project);
  }

  @ProxyStandard
  @NameToken(Places.project)
  public interface Proxy extends ProxyPlace<ProjectPresenter> {}

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> TABLES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> FILES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> ADMIN_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  private final MagmaPresenter magmaPresenter;

  private final Provider<FileExplorerPresenter> fileExplorerPresenterProvider;

  private final PlaceManager placeManager;

  private String name;

  private Display.ProjectTab tab = Display.ProjectTab.tables;

  private ProjectDto project;

  private FileExplorerPresenter fileExplorerPresenter;

  @Inject
  public ProjectPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
      PlaceManager placeManager, MagmaPresenter magmaPresenter,
      Provider<FileExplorerPresenter> fileExplorerPresenterProvider) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    this.magmaPresenter = magmaPresenter;
    this.fileExplorerPresenterProvider = fileExplorerPresenterProvider;
  }

  @Override
  protected void onBind() {
    super.onBind();

    addRegisteredHandler(DatasourceSelectionChangeEvent.getType(), this);
    addRegisteredHandler(TableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(VariableSelectionChangeEvent.getType(), this);

    setInSlot(TABLES_PANE, magmaPresenter);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    name = request.getParameter(ParameterTokens.TOKEN_NAME, null);
    tab = validateTab(request.getParameter(ParameterTokens.TOKEN_TAB, null));
    String path = validatePath(request.getParameter(ParameterTokens.TOKEN_PATH, null));

    if(tab == Display.ProjectTab.tables) {
      // TODO check that datasource name is the one of project
      getView().setTabData(tab.ordinal(), path);
    } else {
      getView().setTabData(tab.ordinal(), null);
    }

    refresh();
  }

  public void refresh() {
    if(name == null) return;
    // TODO handle wrong or missing id
    UriBuilder builder = UriBuilder.create().segment("project", name);
    final HasHandlers eventSource = this;
    ResourceRequestBuilderFactory.<ProjectDto>newBuilder().forResource(builder.build()).get()
        .withCallback(new ResourceCallback<ProjectDto>() {
          @Override
          public void onResource(Response response, ProjectDto resource) {
            project = resource;
            getView().setProject(project);
            String path = (String) getView().getTabData(tab.ordinal());
            if(tab.equals(Display.ProjectTab.tables) && !Strings.isNullOrEmpty(path)){
              MagmaPath.Parser parser = new MagmaPath.Parser().parse(path);
              if(parser.hasVariable()) {
                magmaPresenter.show(parser.getDatasource(), parser.getTable(), parser.getVariable());
              } else if(parser.hasTable()) {
                magmaPresenter.show(parser.getDatasource(), parser.getTable());
              } else {
                magmaPresenter.show(project.getDatasource().getName());
              }
            }else{
              getEventBus()
                  .fireEventFromSource(new DatasourceSelectionChangeEvent(this, project.getDatasource()), eventSource);
            }
            getView().selectTab(tab.ordinal());
          }
        }).send();
  }

  @Override
  public void onProjectsSelection() {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.projects).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onTabSelected(int index) {
    tab = Display.ProjectTab.values()[index];

    switch(tab) {
      case tables:
        break;
      case files:
        onFilesTabSelected();
        break;
    }

    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Arrays.asList(ParameterTokens.TOKEN_NAME));

    builder.with(ParameterTokens.TOKEN_TAB, tab.toString());

    String queryPathParam = (String) getView().getTabData(index);

    if(!Strings.isNullOrEmpty(queryPathParam)) {

      if(tab == Display.ProjectTab.tables) {
        MagmaPath.Parser parser = new MagmaPath.Parser().parse(queryPathParam);

        if(Strings.isNullOrEmpty(parser.getVariable())) {
          getEventBus()
              .fireEventFromSource(new TableSelectionChangeEvent(this, parser.getDatasource(), parser.getTable()),
                  this);
        } else {
          getEventBus().fireEventFromSource(
              new VariableSelectionChangeEvent(this, parser.getDatasource(), parser.getTable(), parser.getVariable()),
              this);
        }
      }

      builder.with(ParameterTokens.TOKEN_PATH, queryPathParam);
    }

    placeManager.updateHistory(builder.build(), true);
  }

  private void onFilesTabSelected() {
    if(fileExplorerPresenter == null) {
      GWT.log("fileExplorerPresenter");
      fileExplorerPresenter = fileExplorerPresenterProvider.get();
      setInSlot(FILES_PANE, fileExplorerPresenter);
    }
    // TODO set project to home the first time tab is visited for this project
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
      } catch(IllegalArgumentException e) {
      }
    }

    return Display.ProjectTab.tables;
  }

  private String validatePath(String path) {
    if(!Strings.isNullOrEmpty(path)) {
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
