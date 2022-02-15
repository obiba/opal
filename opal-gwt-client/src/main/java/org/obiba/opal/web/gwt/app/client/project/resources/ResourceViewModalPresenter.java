/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project.resources;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.validator.*;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.ResourceViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.gwt.http.client.Response.*;

/**
 *
 */
public class ResourceViewModalPresenter extends ModalPresenterWidget<ResourceViewModalPresenter.Display>
    implements ResourceViewModalUiHandlers {

  private final Translations translations;

  private final PlaceManager placeManager;

  private ViewDto view;

  private String datasourceName;

  private String resourceName;

  private final ValidationHandler validationHandler;

  private JsArray<DatasourceDto> datasources;

  @Inject
  public ResourceViewModalPresenter(EventBus eventBus, Display display, Translations translations,
                                    PlaceManager placeManager) {
    super(eventBus, display);
    this.translations = translations;
    this.placeManager = placeManager;
    validationHandler = new PropertiesValidationHandler();
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    refreshDatasources();
  }

  /**
   * Will update the view table.
   *
   * @param view
   */

  public void initialize(ViewDto view) {
    this.view = view;
    getView().renderProperties(view);
  }

  public void initialize(String datasourceName, String resourceName) {
    this.datasourceName = datasourceName;
    this.resourceName = resourceName;
    getView().setName(resourceName);
  }

  private void refreshDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            datasources = JsArrays.toSafeArray(resource);
            for (int i = 0; i < datasources.length(); i++) {
              DatasourceDto d = datasources.get(i);
              d.setViewArray(JsArrays.toSafeArray(d.getViewArray()));
            }
            // pre select the first datasource
            getView().setDatasources(datasources, Strings.isNullOrEmpty(datasourceName) ? datasources.get(0).getName() : datasourceName);
          }
        }).send();
  }

  @Override
  public void onSave(String destinationDatasourceName, final String name, String entityType, String idColumn) {
    if (!validationHandler.validate()) return;

    ViewDto dto = getViewDto(name, entityType, idColumn);
    if (view == null) createView(destinationDatasourceName, dto);
    else updateView(dto);
  }

  private void updateView(ViewDto dto) {
    ResponseCodeCallback completed = new CompletedCallback(getDatasourceName(), dto.getName());

    UriBuilder ub = UriBuilders.DATASOURCE_VIEW.create().query("comment", view.getName().equals(dto.getName())
        ? TranslationsUtils.replaceArguments(translations.updateComment(), dto.getName())
        : TranslationsUtils.replaceArguments(translations.renameToComment(), view.getName(), dto.getName()));

    ResourceRequestBuilderFactory.newBuilder().put().forResource(ub.build(getDatasourceName(), view.getName()))
        .withResourceBody(ViewDto.stringify(dto)).withCallback(completed, Response.SC_OK, Response.SC_BAD_REQUEST, Response.SC_FORBIDDEN).send();
  }

  private void createView(String destinationDatasourceName, ViewDto dto) {
    ResponseCodeCallback completed = new CompletedCallback(destinationDatasourceName, dto.getName());

    UriBuilder ub = UriBuilder.create().segment("datasource", destinationDatasourceName, "views");

    getView().setInProgress(true);
    ResourceRequestBuilderFactory.newBuilder()//
        .post()//
        .forResource(ub.build())//
        .withResourceBody(ViewDto.stringify(dto))//
        .withCallback(completed, SC_CREATED, SC_OK, SC_BAD_REQUEST, SC_NOT_FOUND, SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED,
            SC_INTERNAL_SERVER_ERROR).send();//
  }

  private String getDatasourceName() {
    return view == null ? datasourceName : view.getDatasourceName();
  }

  private ViewDto getViewDto(String name, String entityType, String idColumn) {
    ViewDto updatedView = ViewDto.create();
    updatedView.setName(name);
    JsArrayString resources = JavaScriptObject.createArray().cast();
    resources.push(datasourceName + "." + resourceName);
    updatedView.setFromArray(resources);

    ResourceViewDto resDto = ResourceViewDto.create();
    resDto.setEntityType(entityType);
    if (!Strings.isNullOrEmpty(idColumn))
      resDto.setIdColumn(idColumn);
    updatedView.setExtension(ResourceViewDto.ViewDtoExtensions.view, resDto);

    return updatedView;
  }

  public interface Display extends PopupView, HasUiHandlers<ResourceViewModalUiHandlers> {

    void setName(String resourceName);

    enum FormField {
      NAME
    }

    void setDatasources(JsArray<DatasourceDto> datasources, String name);

    void renderProperties(ViewDto view);

    void showError(String message, @Nullable FormField id);

    void setInProgress(boolean progress);

    HasText getName();
  }

  private class CompletedCallback implements ResponseCodeCallback {

    private final String destinationDatasourceName;

    private final String viewName;

    private CompletedCallback(String destinationDatasourceName, String viewName) {
      this.destinationDatasourceName = destinationDatasourceName;
      this.viewName = viewName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().setInProgress(false);
      if (response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
        getView().hide();
        placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(destinationDatasourceName, viewName));
      } else if (response.getStatusCode() == Response.SC_FORBIDDEN) {
        getView().showError(translations.userMessageMap().get("UnauthorizedOperation"), null);
      } else {
        ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
        getView().showError(TranslationsUtils
            .replaceArguments(translations.userMessageMap().get(error.getStatus()), error.getArgumentsArray()), null);
      }
    }
  }

  private class PropertiesValidationHandler extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<>();
      addViewNameValidators(validators);
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(message, Display.FormField.valueOf(id));
    }

    private void addViewNameValidators(Collection<FieldValidator> validators) {
      validators.add(
          new RequiredTextValidator(getView().getName(), "ViewNameRequired", Display.FormField.NAME.name()));
      validators.add(
          new DisallowedCharactersValidator(getView().getName(), new char[]{'.', ':'}, "ViewNameDisallowedChars",
              Display.FormField.NAME.name()));
    }
  }

}
