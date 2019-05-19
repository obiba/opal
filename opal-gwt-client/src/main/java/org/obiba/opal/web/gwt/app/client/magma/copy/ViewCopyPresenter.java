/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.copy;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import java.util.ArrayList;
import java.util.List;

public class ViewCopyPresenter extends ModalPresenterWidget<ViewCopyPresenter.Display> implements CopyUiHandlers {

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final PlaceManager placeManager;

  private TableDto table;

  private ViewDto view;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public ViewCopyPresenter(Display display, EventBus eventBus, Translations translations,
                           TranslationMessages translationMessages, PlaceManager placeManager) {
    super(eventBus, display);
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.placeManager = placeManager;

    getView().setUiHandlers(this);
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
  }

  @Override
  protected void onBind() {
    initDatasources();
  }

  @Override
  public void onReveal() {

  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void onSubmit(final String destination, final String newName) {
    if (view == null) return;
    view.setName(newName);
    view.setDatasourceName(destination);
    UriBuilder uriBuilder = UriBuilder.create();
    uriBuilder.segment("datasource", destination, "views");
    ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(uriBuilder.build()).post()
        .withResourceBody(ViewDto.stringify(view))//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(destination, newName));
          }
        }, Response.SC_OK, Response.SC_CREATED)//
        .send();
  }

  private void initDatasources() {
    ResourceRequestBuilderFactory.<JsArray<ProjectDto>>newBuilder()
        .forResource(UriBuilders.PROJECTS.create().query("digest", "true").build()).get()
        .withCallback(new ResourceCallback<JsArray<ProjectDto>>() {
          @Override
          public void onResource(Response response, JsArray<ProjectDto> resource) {
            List<String> datasourceNames = Lists.newArrayList();
            for (ProjectDto project : JsArrays.toIterable(resource)) {
              datasourceNames.add(project.getName());
            }
            getView().setDatasources(datasourceNames);
          }
        }).send();
  }

  public void setView(TableDto table) {
    this.table = table;
    getView().setView(table);
    ResourceRequestBuilderFactory.<ViewDto>newBuilder()
        .forResource(table.getViewLink())
        .withCallback(new ResourceCallback<ViewDto>() {
          @Override
          public void onResource(Response response, ViewDto resource) {
            view = resource;
          }
        }).get().send();
  }

  //
  // Interfaces and classes
  //

  public interface Display extends PopupView, HasUiHandlers<CopyUiHandlers> {

    enum FormField {
      NEW_TABLE_NAME
    }

    /**
     * Set a collection of datasources retrieved from Opal.
     */
    void setDatasources(List<String> datasourceNames);

    void setView(TableDto view);

    void hideDialog();

    void showError(FormField field, String message);

  }

}
