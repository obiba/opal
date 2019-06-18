package org.obiba.opal.web.gwt.app.client.magma.datasource.presenter;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_METHOD_NOT_ALLOWED;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.File;
import elemental.html.FileReader;
import java.util.List;
import javax.inject.Inject;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

public class RestoreViewsModalPresenter extends ModalPresenterWidget<RestoreViewsModalPresenter.Display> implements RestoreViewsUiHandlers {

  private String projectName;

  private List<String> currentViews;

  @Inject
  public RestoreViewsModalPresenter(
    EventBus eventBus,
    Display view) {
    super(eventBus, view);

    getView().setUiHandlers(this);
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void onSubmitFiles(List<File> files) {
    if (files.size() > 0) {

      for (int i = 0; i < files.size(); i++) {
        File file = files.get(i);

        final FileReader fileReader = Browser.getWindow().newFileReader();

        fileReader.setOnloadend(new EventListener() {
          @Override
          public void handleEvent(Event evt) {
            String view = fileReader.getResult().toString();

            ViewDto dto = ViewDto.parse(view);
            if (!currentViews.contains(dto.getName())) {
              createView(view);
            } else {
              if (getView().canOverride()) {
                overrideView(dto.getName(), view);
              }
            }
          }
        });

        fileReader.readAsText(file);
      }

    }

    getView().hideDialog();
  }

  private void createView(String view) {
    ResourceRequestBuilderFactory.newBuilder()
      .post()
      .forResource(UriBuilders.DATASOURCE_VIEWS.create().query("comment", "restore-view").build(projectName))
      .withResourceBody(view)
      .withCallback(new RestoreViewsModalResponseCodeCallback(), SC_OK, SC_CREATED, SC_BAD_REQUEST, SC_NOT_FOUND, SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED, SC_INTERNAL_SERVER_ERROR).send();
  }

  private void overrideView(String viewName, String view) {
    ResourceRequestBuilderFactory.newBuilder()
      .put()
      .forResource(UriBuilders.DATASOURCE_VIEW.create().query("comment", "restore-view").build(projectName, viewName))
      .withResourceBody(view)
      .withCallback(new RestoreViewsModalResponseCodeCallback(), SC_OK, SC_CREATED, SC_BAD_REQUEST, SC_NOT_FOUND, SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED, SC_INTERNAL_SERVER_ERROR).send();
  }

  public void initialize(String projectName) {
    this.projectName = projectName;
    currentViews = Lists.newArrayList();

    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
      .forResource(UriBuilders.DATASOURCE_TABLES.create().build(projectName)).withCallback(
      new ResourceCallback<JsArray<TableDto>>() {
        @Override
        public void onResource(Response response, JsArray<TableDto> resource) {
          for (int i = 0; i < resource.length(); i++) {
            TableDto tableDto = resource.get(i);
            if (tableDto.hasViewLink()) {
              currentViews.add(tableDto.getName());
            }
          }
        }
      }).get().send();
  }

  public interface Display extends PopupView, HasUiHandlers<RestoreViewsUiHandlers> {

    void hideDialog();

    boolean canOverride();
  }

  public class RestoreViewsModalResponseCodeCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if (response.getStatusCode() != SC_CREATED || response.getStatusCode() != SC_OK) {
        fireEvent(NotificationEvent.newBuilder().error((ClientErrorDto) JsonUtils.unsafeEval(response.getText())).build());
      }
    }
  }
}
