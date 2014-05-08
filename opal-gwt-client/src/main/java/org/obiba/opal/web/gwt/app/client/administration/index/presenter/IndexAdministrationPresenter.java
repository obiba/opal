/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.index.event.TableIndicesRefreshEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.magma.event.TableIndexStatusRefreshEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ServiceDto;
import org.obiba.opal.web.model.client.opal.ServiceStatus;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class IndexAdministrationPresenter
    extends ItemAdministrationPresenter<IndexAdministrationPresenter.Display, IndexAdministrationPresenter.Proxy>
    implements IndexAdministrationUiHandlers {

  @ProxyStandard
  @NameToken(Places.INDEX)
  public interface Proxy extends ProxyPlace<IndexAdministrationPresenter> {}

  private static final int DELAY_MILLIS = 1500;

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<IndexAdministrationUiHandlers> {

    enum Slots {
      Drivers, Permissions
    }

    enum Status {
      Startable, Stoppable, Pending
    }

    String INDEX_ACTION = "Index now";

    String REMOVE_ACTION = "Remove Index";

    void setServiceStatus(Status status);

    void setEnabled(boolean enabled);

    void unselectIndex(TableIndexStatusDto object);

    void renderRows(JsArray<TableIndexStatusDto> rows);

    void clear();

    List<TableIndexStatusDto> getSelectedIndices();

    HasData<TableIndexStatusDto> getIndexTable();
  }

  private final ModalProvider<IndexPresenter> indexModalProvider;

  private final ModalProvider<IndexConfigurationPresenter> indexConfigurationProvider;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public IndexAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<IndexPresenter> indexModalProvider,
      ModalProvider<IndexConfigurationPresenter> indexConfigurationProvider,
      DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.indexModalProvider = indexModalProvider.setContainer(this);
    this.indexConfigurationProvider = indexConfigurationProvider.setContainer(this);
    this.breadcrumbsHelper = breadcrumbsHelper;
    getView().setUiHandlers(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.indices()).get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListIndicesAuthorization())).send();
  }

  @Override
  public String getName() {
    return translations.indicesLabel();
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    // stop start search service
    ResourceRequestBuilderFactory.<ServiceDto>newBuilder().forResource(Resources.searchService()) //
        .withCallback(new ResourceCallback<ServiceDto>() {
          @Override
          public void onResource(Response response, ServiceDto resource) {
            if(response.getStatusCode() == Response.SC_OK) {
              getView().setServiceStatus(resource.getStatus().isServiceStatus(ServiceStatus.RUNNING)
                  ? Display.Status.Stoppable
                  : Display.Status.Startable);
            }
          }
        }) //
        .get().send();

    ResourceRequestBuilderFactory.newBuilder().forResource(Resources.indicesEnabled()) //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setEnabled(response.getStatusCode() == Response.SC_OK);
          }
        }, Response.SC_OK, Response.SC_SERVICE_UNAVAILABLE, Response.SC_UNAUTHORIZED)//
        .get().send();

    getView().getIndexTable().setVisibleRange(0, 10);
    refresh();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.indices()).get().authorize(authorizer)
        .send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageSearchIndexTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();

    addRegisteredHandler(TableIndicesRefreshEvent.getType(), new TableIndicesRefreshEvent.TableIndicesRefreshHandler() {
      @Override
      public void onTableIndicesRefresh(TableIndicesRefreshEvent event) {
        refresh();
      }
    });
  }

  @Override
  public void start() {
    // Start service
    getView().setServiceStatus(Display.Status.Pending);
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder() //
        .forResource(Resources.searchServiceEnabled()).accept("application/json") //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setServiceStatus(Display.Status.Stoppable);
            refresh();
            fireEvent(new TableIndexStatusRefreshEvent());
          }
        }) //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setServiceStatus(Display.Status.Startable);

            ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
            if(error.getStatus() != null) {
              fireEvent(NotificationEvent.newBuilder()
                  .error(TranslationsUtils.replaceArguments(translations.searchSettingsError(), error.getStatus()))
                  .build());
            } else {
              fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
          }
        }, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_BAD_REQUEST) //
        .put().send();
  }

  @Override
  public void stop() {
    // Stop service
    getView().setServiceStatus(Display.Status.Pending);
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder() //
        .forResource(Resources.searchServiceEnabled()).accept("application/json") //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setServiceStatus(Display.Status.Startable);
            getView().clear();
            fireEvent(new TableIndexStatusRefreshEvent());
          }
        }) //
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().clear();
            getView().setServiceStatus(Display.Status.Stoppable);
            ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
            fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).args(error.getArgumentsArray()).build());
          }
        }) //
        .delete().send();
  }

  @Override
  public void resume() {
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder() //
        .forResource(Resources.indicesEnabled()).accept("application/json") //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setEnabled(true);
            getView().clear();
            refresh();
          }
        }) //
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().clear();
            getView().setEnabled(false);
            ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
            fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).args(error.getArgumentsArray()).build());
          }
        }) //
        .put().send();
  }

  @Override
  public void suspend() {
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder() //
        .forResource(Resources.indicesEnabled()).accept("application/json") //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setEnabled(false);
            getView().clear();
            refresh();
          }
        }) //
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().clear();
            getView().setEnabled(true);
            ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
            fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).args(error.getArgumentsArray()).build());
          }
        }) //
        .delete().send();
  }

  @Override
  public void refresh() {
    refresh(true);
  }

  @Override
  public void configure() {
    indexConfigurationProvider.get();
  }

  @Override
  public void delete(List<TableIndexStatusDto> statusDtos) {
    for(TableIndexStatusDto statusDto : statusDtos) {
      ResponseCodeCallback callback = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          refresh();
        }

      };
      ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
          .forResource(Resources.index(statusDto.getDatasource(), statusDto.getTable())) //
          .accept("application/json") //
          .withCallback(callback, Response.SC_OK, Response.SC_SERVICE_UNAVAILABLE) //
          .delete().send();
      getView().unselectIndex(statusDto);
    }
  }

  @Override
  public void schedule(List<TableIndexStatusDto> statusDtos) {
    IndexPresenter dialog = indexModalProvider.get();
    dialog.updateSchedules(statusDtos);
  }

  @Override
  public void indexNow(List<TableIndexStatusDto> statusDtos) {
    for(TableIndexStatusDto statusDto : statusDtos) {
      ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder() //
          .forResource(Resources.index(statusDto.getDatasource(), statusDto.getTable())) //
          .accept("application/json") //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              // Wait a few seconds for the task to launch before checking its status
              Timer t = new Timer() {
                @Override
                public void run() {
                  refresh(false);
                }
              };
              // Schedule the timer to run once in X seconds.
              t.schedule(DELAY_MILLIS);
            }
          }).put().send();
    }
  }

  private void refresh(boolean clearIndices) {
    // Fetch all indices
    ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder()//
        .forResource(Resources.indices())//
        .withCallback(new TableIndexStatusResourceCallback(getView().getIndexTable().getVisibleRange(), clearIndices))//
        .withCallback(Response.SC_SERVICE_UNAVAILABLE, ResponseCodeCallback.NO_OP) //
        .get().send();
  }

  private class TableIndexStatusResourceCallback implements ResourceCallback<JsArray<TableIndexStatusDto>> {
    private final Range r;

    private final boolean clearIndices;

    private TableIndexStatusResourceCallback(Range r, boolean clearIndices) {
      this.r = r;
      this.clearIndices = clearIndices;
    }

    @Override
    public void onResource(Response response, JsArray<TableIndexStatusDto> resource) {
      getView().renderRows(resource);
      getView().getIndexTable().setVisibleRangeAndClearData(r, true);
      if(clearIndices) getView().getSelectedIndices().clear();
    }
  }

  private final class ListIndicesAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {

      // Fetch all indices
      ResourceRequestBuilderFactory.<JsArray<TableIndexStatusDto>>newBuilder() //
          .forResource(Resources.indices()) //
          .withCallback(new ResourceCallback<JsArray<TableIndexStatusDto>>() {
            @Override
            public void onResource(Response response, JsArray<TableIndexStatusDto> resource) {
              getView().renderRows(resource);
            }
          }) //
          .withCallback(Response.SC_SERVICE_UNAVAILABLE, ResponseCodeCallback.NO_OP) //
          .get().send();

    }

    @Override
    public void unauthorized() {
    }

  }

}
