/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.view;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Request;
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
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.bookmark.icon.BookmarkIconPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.magma.copy.DataExportFolderService;
import org.obiba.opal.web.gwt.app.client.magma.event.MagmaPathSelectionEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.project.admin.ProjectAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectHiddenEvent;
import org.obiba.opal.web.gwt.app.client.project.genotypes.ProjectGenotypesPresenter;
import org.obiba.opal.web.gwt.app.client.project.permissions.ProjectPermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.project.resources.ProjectResourcesPresenter;
import org.obiba.opal.web.gwt.app.client.report.list.ReportsPresenter;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.app.client.task.presenter.TasksPresenter;
import org.obiba.opal.web.gwt.app.client.ui.HasTabPanel;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ProjectDto;

public class ProjectPresenter extends Presenter<ProjectPresenter.Display, ProjectPresenter.Proxy>
    implements ProjectUiHandlers, FolderUpdatedEvent.FolderUpdatedHandler {

  public interface Display extends View, HasUiHandlers<ProjectUiHandlers>, HasTabPanel {

    enum ProjectTab {
      TABLES,
      RESOURCES,
      GENOTYPES,
      FILES,
      REPORTS,
      TASKS,
      PERMISSIONS,
      ADMINISTRATION
    }

    boolean isTabVisible(int index);

    void setProject(ProjectDto project);

    HasAuthorization getTablesAuthorizer();

    HasAuthorization getGenotypesAuthorizer();

    HasAuthorization getPermissionsAuthorizer();
  }

  @ProxyStandard
  @NameToken(Places.PROJECT)
  public interface Proxy extends ProxyPlace<ProjectPresenter> {
  }

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> BOOKMARK_ICON = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> TABLES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> FILES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> RESOURCES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> GENOTYPES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> REPORTS_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> TASKS_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> ADMIN_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> PERMISSION_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  private final Provider<MagmaPresenter> magmaPresenterProvider;

  private final Provider<FileExplorerPresenter> fileExplorerPresenterProvider;

  private final Provider<ProjectResourcesPresenter> projectResourcesPresenterProvider;

  private final Provider<ProjectGenotypesPresenter> projectGenotypesPresenterProvider;

  private final Provider<ReportsPresenter> reportsPresenterProvider;

  private final Provider<TasksPresenter> tasksPresenterProvider;

  private final Provider<ProjectPermissionsPresenter> projectResourcePermissionsProvider;

  private final Provider<ProjectAdministrationPresenter> projectAdministrationPresenterProvider;

  private final PlaceManager placeManager;

  private String projectName;

  private Display.ProjectTab tab = Display.ProjectTab.TABLES;

  private String path;

  private ProjectDto project;

  private MagmaPresenter magmaPresenter;

  private FileExplorerPresenter fileExplorerPresenter;

  private ProjectResourcesPresenter projectResourcesPresenter;

  private ProjectGenotypesPresenter projectGenotypesPresenter;

  private ReportsPresenter reportsPresenter;

  private TasksPresenter tasksPresenter;

  private ProjectPermissionsPresenter projectPermissionsPresenter;

  private ProjectAdministrationPresenter projectAdministrationPresenter;

  private final Provider<BookmarkIconPresenter> bookmarkIconPresenterProvider;

  private final DataExportFolderService dataExportFolderService;

  private BookmarkIconPresenter bookmarkIconPresenter;

  @Inject
  @SuppressWarnings({"PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters"})
  public ProjectPresenter(EventBus eventBus, Display display, Proxy proxy, Provider<ProjectResourcesPresenter> projectResourcesPresenterProvider, PlaceManager placeManager,
                          Provider<MagmaPresenter> magmaPresenterProvider, Provider<FileExplorerPresenter> fileExplorerPresenterProvider,
                          Provider<ProjectGenotypesPresenter> projectGenotypesPresenterProvider,
                          Provider<ReportsPresenter> reportsPresenterProvider, Provider<TasksPresenter> tasksPresenterProvider,
                          Provider<ProjectAdministrationPresenter> projectAdministrationPresenterProvider,
                          Provider<ProjectPermissionsPresenter> projectResourcePermissionsProvider,
                          Provider<BookmarkIconPresenter> bookmarkIconPresenterProvider,
                          DataExportFolderService dataExportFolderService) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    this.magmaPresenterProvider = magmaPresenterProvider;
    this.fileExplorerPresenterProvider = fileExplorerPresenterProvider;
    this.projectResourcesPresenterProvider = projectResourcesPresenterProvider;
    this.projectGenotypesPresenterProvider = projectGenotypesPresenterProvider;
    this.reportsPresenterProvider = reportsPresenterProvider;
    this.tasksPresenterProvider = tasksPresenterProvider;
    this.projectAdministrationPresenterProvider = projectAdministrationPresenterProvider;
    this.projectResourcePermissionsProvider = projectResourcePermissionsProvider;
    this.bookmarkIconPresenterProvider = bookmarkIconPresenterProvider;
    this.dataExportFolderService = dataExportFolderService;
  }

  @Override
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(FolderUpdatedEvent.getType(), this);
  }

  @Override
  protected void onHide() {
    fireEvent(new ProjectHiddenEvent(project));
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    String oldProject = Strings.nullToEmpty(projectName);

    projectName = request.getParameter(ParameterTokens.TOKEN_NAME, null);
    tab = validateTab(request.getParameter(ParameterTokens.TOKEN_TAB, null));
    path = Strings.nullToEmpty(request.getParameter(ParameterTokens.TOKEN_PATH, null));

    if (!projectName.equals(oldProject) && path.isEmpty()) {
      if (fileExplorerPresenter != null) fileExplorerPresenter.reset();
      getView().clearTabsData();
    }

    refresh();
  }

  private void authorize() {
    if (projectName == null) return;

    new DefaultTabAuthorizer().authorizeTables();

    // permissions tab
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_PERMISSIONS_SUBJECTS.create().build(projectName)).get()
        .authorize(getView().getPermissionsAuthorizer())
        .send();
  }

  public void refresh() {
    // TODO handle wrong or missing project name
    if (projectName == null) return;

    // reset
    ResourceRequestBuilderFactory.<ProjectDto>newBuilder()
        .forResource(UriBuilders.PROJECT.create().build(projectName))
        .withCallback(new ResourceCallback<ProjectDto>() {
          @Override
          public void onResource(Response response, ProjectDto resource) {
            project = resource;
            dataExportFolderService.setProjectExportFolder(project.getExportFolder());

            getView().setProject(project);
            authorize();

            if (tab != Display.ProjectTab.TABLES && tab != Display.ProjectTab.GENOTYPES) {
              // these two tabs will be selected upon their authorization
              onTabSelected(tab.ordinal());
              getView().selectTab(tab.ordinal());
            }

            if (bookmarkIconPresenter == null) {
              bookmarkIconPresenter = bookmarkIconPresenterProvider.get();
              bookmarkIconPresenter.addStyleName("small-indent");
              setInSlot(BOOKMARK_ICON, bookmarkIconPresenter);
            }
            bookmarkIconPresenter.setBookmarkable(UriBuilders.DATASOURCE.create().build(projectName));
          }
        })
        .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().warn("NoSuchProject").args(projectName).build());
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(Places.PROJECTS).build());
          }
        }).get().send();

  }

  @Override
  public void onProjectsSelection(String tag) {
    PlaceRequest.Builder place = new PlaceRequest.Builder().nameToken(Places.PROJECTS);
    if (tag != null) place = place.with(ParameterTokens.TOKEN_TAG, tag);
    placeManager.revealPlace(place.build());
  }

  @Override
  public void onTabSelected(int index) {
    String queryPathParam = (String) getView().getTabData(index);
    selectTab(index, queryPathParam);

    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Lists.newArrayList(ParameterTokens.TOKEN_NAME))
        .with(ParameterTokens.TOKEN_TAB, tab.toString());
    if (!Strings.isNullOrEmpty(queryPathParam)) {
      builder.with(ParameterTokens.TOKEN_PATH, queryPathParam);
    }
    placeManager.updateHistory(builder.build(), true);
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private void selectTab(int index, String queryPathParam) {
    tab = Display.ProjectTab.values()[index];
    switch (tab) {
      case TABLES:
        onTablesTabSelected(queryPathParam);
        break;
      case FILES:
        onFilesTabSelected(queryPathParam);
        break;
      case RESOURCES:
        onResourcesTabSelected();
        break;
      case GENOTYPES:
        onGenotypesTabSelected();
        break;
      case REPORTS:
        onReportsTabSelected();
        break;
      case TASKS:
        onTasksTabSelected();
        break;
      case PERMISSIONS:
        onPermissionsTabSelected();
        break;
      case ADMINISTRATION:
        onAdminTabSelected();
        break;
    }
  }

  private void onTablesTabSelected(String path) {
    if (magmaPresenter == null) {
      magmaPresenter = magmaPresenterProvider.get();
      setInSlot(TABLES_PANE, magmaPresenter);
    }
    fireEvent(Strings.isNullOrEmpty(path)
        ? new MagmaPathSelectionEvent(this, project.getDatasource().getName())
        : new MagmaPathSelectionEvent(this, path));
  }

  private void onFilesTabSelected(String path) {
    if (fileExplorerPresenter == null) {
      fileExplorerPresenter = fileExplorerPresenterProvider.get();
      setInSlot(FILES_PANE, fileExplorerPresenter);
    }
    fileExplorerPresenter.showProject(projectName);
    fireEvent(Strings.isNullOrEmpty(path)
        ? new FolderRequestEvent(FileDtos.project(projectName))
        : new FolderRequestEvent(FileDtos.create(path.split("/"))));
  }

  private void onResourcesTabSelected() {
    if (projectResourcesPresenter == null) {
      projectResourcesPresenter = projectResourcesPresenterProvider.get();
      setInSlot(RESOURCES_PANE, projectResourcesPresenter);
    }

    projectResourcesPresenter.initialize(project);
  }

  private void onGenotypesTabSelected() {
    if (projectGenotypesPresenter == null) {
      projectGenotypesPresenter = projectGenotypesPresenterProvider.get();
      setInSlot(GENOTYPES_PANE, projectGenotypesPresenter);
    }

    projectGenotypesPresenter.initialize(project);
  }

  private void onReportsTabSelected() {
    if (reportsPresenter == null) {
      reportsPresenter = reportsPresenterProvider.get();
      setInSlot(REPORTS_PANE, reportsPresenter);
    }
    reportsPresenter.showProject(projectName);
  }

  private void onTasksTabSelected() {
    if (tasksPresenter == null) {
      tasksPresenter = tasksPresenterProvider.get();
      setInSlot(TASKS_PANE, tasksPresenter);
    }
    tasksPresenter.showProject(projectName);
  }

  private void onPermissionsTabSelected() {
    if (projectPermissionsPresenter == null) {
      projectPermissionsPresenter = projectResourcePermissionsProvider.get();
      setInSlot(PERMISSION_PANE, projectPermissionsPresenter);
    }
    projectPermissionsPresenter.initialize(project);
  }

  private void onAdminTabSelected() {
    if (projectAdministrationPresenter == null) {
      projectAdministrationPresenter = projectAdministrationPresenterProvider.get();
      setInSlot(ADMIN_PANE, projectAdministrationPresenter);
    }
    projectAdministrationPresenter.setProject(project);
  }

  @Override
  public void onFolderUpdated(FolderUpdatedEvent event) {
    if (fileExplorerPresenter == null || !fileExplorerPresenter.isVisible()) return;
    if (tab == Display.ProjectTab.FILES) updateHistory(event.getFolder().getPath());
  }

  private void updateHistory(String queryPathParam) {
    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Lists.newArrayList(ParameterTokens.TOKEN_NAME, ParameterTokens.TOKEN_TAB));

    if (!Strings.isNullOrEmpty(queryPathParam)) {
      builder.with(ParameterTokens.TOKEN_PATH, queryPathParam).build();
    }

    placeManager.updateHistory(builder.build(), true);
    getView().setTabData(tab.ordinal(), queryPathParam);
  }

  private Display.ProjectTab validateTab(String tabName) {
    if (!Strings.isNullOrEmpty(tabName)) {
      try {
        return Display.ProjectTab.valueOf(tabName);
      } catch (IllegalArgumentException ignored) {
      }
    }
    return Display.ProjectTab.TABLES;
  }

  private String validatePath(String name, String path) {
    if (tab == Display.ProjectTab.TABLES && !Strings.isNullOrEmpty(path)) {
      String datasourceName = MagmaPath.Parser.parse(path).getDatasource();
      if (!Strings.isNullOrEmpty(datasourceName) && name.equals(datasourceName)) {
        return path;
      }
    }
    updateHistory(null);
    return null;
  }

  /**
   * Helper class to show TABLES or GENOTYPES TABs based on authorization results
   */
  private class DefaultTabAuthorizer {
    public void authorizeTables() {
      ResourceAuthorizationRequestBuilderFactory.newBuilder()
          .forResource(UriBuilders.DATASOURCE_TABLES.create().build(projectName)).get()
          .authorize(new HasAuthorization() {
            HasAuthorization viewAuthorizer = getView().getTablesAuthorizer();

            @Override
            public void beforeAuthorization() {
              viewAuthorizer.beforeAuthorization();
            }

            @Override
            public void authorized() {
              viewAuthorizer.authorized();
              getView().setTabData(tab.ordinal(), tab == Display.ProjectTab.TABLES ? validatePath(projectName, path) : null);
              onTabSelected(tab.ordinal());
              getView().selectTab(tab.ordinal());
              authorizeGenotypes();
            }

            @Override
            public void unauthorized() {
              viewAuthorizer.unauthorized();
              authorizeGenotypes();
            }
          })
          .send();
    }

    void authorizeGenotypes() {
      // tables tab
      ResourceAuthorizationRequestBuilderFactory.newBuilder()
          .forResource(UriBuilders.PROJECT_VCF_STORE.create().build(projectName)).get()
          .authorize(new HasAuthorization() {
            HasAuthorization viewAuthorizer = getView().getGenotypesAuthorizer();

            @Override
            public void beforeAuthorization() {
              viewAuthorizer.beforeAuthorization();
            }

            @Override
            public void authorized() {
              viewAuthorizer.authorized();
              if (!getView().isTabVisible(Display.ProjectTab.TABLES.ordinal()) &&
                  tab == Display.ProjectTab.TABLES ||
                  tab == Display.ProjectTab.GENOTYPES) {

                tab = Display.ProjectTab.GENOTYPES;
                onTabSelected(tab.ordinal());
                getView().selectTab(tab.ordinal());
                getView().setTabData(tab.ordinal(), tab == Display.ProjectTab.GENOTYPES ? validatePath(projectName, path) : null);
                PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.PROJECT)
                    .with(ParameterTokens.TOKEN_NAME, projectName)
                    .with(ParameterTokens.TOKEN_TAB, Display.ProjectTab.GENOTYPES.toString())
                    .with(ParameterTokens.TOKEN_PATH, path);

                // Prevent recursive placement changes by stopping when new and current places are identical
                PlaceRequest newPlace = builder.build();
                PlaceRequest currPlace = placeManager.getCurrentPlaceRequest();
                if (!currPlace.hasSameNameToken(newPlace) ||
                    !currPlace.getParameter(ParameterTokens.TOKEN_NAME, "").equals(projectName) ||
                    !currPlace.getParameter(ParameterTokens.TOKEN_TAB, "").equals(Display.ProjectTab.GENOTYPES.toString())) {

                  placeManager.revealPlace(builder.build());
                }
              }
            }

            @Override
            public void unauthorized() {
              viewAuthorizer.unauthorized();
            }
          })
          .send();
    }
  }
}
