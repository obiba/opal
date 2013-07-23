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
import org.obiba.opal.web.gwt.app.client.widgets.tab.HasTabPanel;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.common.base.Strings;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
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

public class ProjectPresenter extends Presenter<ProjectPresenter.Display, ProjectPresenter.Proxy>
    implements ProjectUiHandlers, DatasourceSelectionChangeEvent.Handler, TableSelectionChangeEvent.Handler,
    VariableSelectionChangeEvent.Handler {

  public interface Display extends View, HasUiHandlers<ProjectUiHandlers>, HasTabPanel {

    public enum ProjectTab {
      TAB_TABLES,
      TAB_FILES,
      TAB_DATA_VISUALIZATION,
      TAB_REPORTS,
      TAB_TASKS,
      TAB_PERMISSIONS,
      TAB_ADMINISTRATION;
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

  private final FileExplorerPresenter fileExplorerPresenter;

  private final PlaceManager placeManager;

  private String name;

  private int tab = 0;

  private ProjectDto project;

  @Inject
  public ProjectPresenter(EventBus eventBus, Display display, Proxy proxy, Translations translations,
      PlaceManager placeManager, MagmaPresenter magmaPresenter, FileExplorerPresenter fileExplorerPresenter) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    this.magmaPresenter = magmaPresenter;
    this.fileExplorerPresenter = fileExplorerPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();

    addRegisteredHandler(DatasourceSelectionChangeEvent.getType(), this);
    addRegisteredHandler(TableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(VariableSelectionChangeEvent.getType(), this);

    setInSlot(TABLES_PANE, magmaPresenter);
    setInSlot(FILES_PANE, fileExplorerPresenter);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    name = request.getParameter(ParameterTokens.TOKEN_NAME, null);
    tab = Integer.parseInt(request.getParameter(ParameterTokens.TOKEN_TAB, "0"));
    String path = request.getParameter(ParameterTokens.TOKEN_PATH, null);

    if(tab == Display.ProjectTab.TAB_TABLES.ordinal()) {
      getView().setTabData(tab, path);
    }
    else {
      getView().setTabData(tab, null);
    }

    getView().selectTab(tab);

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
            String path = (String) getView().getTabData(tab);
            if(!Strings.isNullOrEmpty(path)) {
              MagmaPath.Parser parser = new MagmaPath.Parser().parse(path);

              if (Strings.isNullOrEmpty(parser.getVariableName())) {
                getEventBus().fireEventFromSource(
                    new TableSelectionChangeEvent(eventSource, parser.getDatasourceName(), parser.getTableName()),
                    eventSource);
              }
              else {
                getEventBus().fireEventFromSource(
                    new VariableSelectionChangeEvent(eventSource, parser.getDatasourceName(), parser.getTableName(),
                      parser.getVariableName()), eventSource);
              }
            } else {
              getEventBus()
                  .fireEventFromSource(new DatasourceSelectionChangeEvent(this, project.getDatasource()), eventSource);
            }

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
    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Arrays.asList(ParameterTokens.TOKEN_NAME));

    builder.with(ParameterTokens.TOKEN_TAB, String.valueOf(index));

    String uriPathParam = (String) getView().getTabData(index);

    if(!Strings.isNullOrEmpty(uriPathParam)) {

      if(index == Display.ProjectTab.TAB_TABLES.ordinal()) {
        MagmaPath.Parser parser = new MagmaPath.Parser().parse(uriPathParam);

        if (Strings.isNullOrEmpty(parser.getVariableName())) {
          getEventBus().fireEventFromSource(
              new TableSelectionChangeEvent(this, parser.getDatasourceName(), parser.getTableName()),
              this);
        }
        else {
          getEventBus().fireEventFromSource(
              new VariableSelectionChangeEvent(this, parser.getDatasourceName(), parser.getTableName(),
                  parser.getVariableName()), this);
        }
      }

      builder.with(ParameterTokens.TOKEN_PATH, uriPathParam);
    }

    placeManager.updateHistory(builder.build(), true);
  }

  @Override
  public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
    if (event.getSource() == this) return;
    updateHistory(null);
  }

  @Override
  public void onTableSelectionChanged(TableSelectionChangeEvent event) {
    if (event.getSource() == this) return;
    updateHistory(
        new MagmaPath.Builder().setDatasourceName(event.getDatasourceName()).setTableName(event.getTableName()).build());
  }

  @Override
  public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
    if (event.getSource() == this) return;
    updateHistory(new MagmaPath.Builder().setDatasourceName(event.getDatasourceName())
        .setTableName(event.getTableName()).setVariableName(event.getVariableName()).build());
  }

  private void updateHistory(String uriPathParam) {
    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Arrays.asList(ParameterTokens.TOKEN_NAME, ParameterTokens.TOKEN_TAB));

    if (!Strings.isNullOrEmpty(uriPathParam)) {
      builder.with(ParameterTokens.TOKEN_PATH, uriPathParam).build();
    }

    placeManager.updateHistory(builder.build(), true);
    getView().setTabData(tab, uriPathParam);
  }
}
