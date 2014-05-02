/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.table.presenter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
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

/**
 *
 */
public class ViewPropertiesModalPresenter extends ModalPresenterWidget<ViewPropertiesModalPresenter.Display>
    implements ViewPropertiesModalUiHandlers {

  private final Translations translations;

  private final PlaceManager placeManager;

  private ViewDto view;

  private final ValidationHandler validationHandler;

  @Inject
  public ViewPropertiesModalPresenter(EventBus eventBus, Display display, Translations translations,
      PlaceManager placeManager) {
    super(eventBus, display);
    this.translations = translations;
    this.placeManager = placeManager;
    validationHandler = new PropertiesValidationHandler();
    getView().setUiHandlers(this);
  }

  /**
   * Will update the view table.
   *
   * @param view
   */

  public void initialize(ViewDto view) {
    this.view = view;
    getView().renderProperties(view);
    renderSelectableTables();
  }

  @Override
  public void onSave(final String name, List<TableDto> referencedTables) {
    if(!validationHandler.validate()) return;

    ViewDto dto = getViewDto(name, referencedTables);

    UriBuilder ub = UriBuilders.DATASOURCE_VIEW.create().query("comment", view.getName().equals(name)
        ? TranslationsUtils.replaceArguments(translations.updateComment(), name)
        : TranslationsUtils.replaceArguments(translations.renameToComment(), view.getName(), name));
    ResourceRequestBuilderFactory.newBuilder().put().forResource(ub.build(view.getDatasourceName(), view.getName()))
        .withResourceBody(ViewDto.stringify(dto)).withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK) {
          getView().hide();
          placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(view.getDatasourceName(), name));
        } else if(response.getStatusCode() == Response.SC_FORBIDDEN) {
          getView().showError(translations.userMessageMap().get("UnauthorizedOperation"), null);
        } else {
          ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
          getView().showError(TranslationsUtils
              .replaceArguments(translations.userMessageMap().get(error.getStatus()), error.getArgumentsArray()), null);
        }
      }
    }, Response.SC_OK, Response.SC_BAD_REQUEST, Response.SC_FORBIDDEN).send();
  }

  private ViewDto getViewDto(String name, List<TableDto> referencedTables) {
    ViewDto v = ViewDto.create();
    v.setName(name);
    JsArrayString tables = JavaScriptObject.createArray().cast();
    for(TableDto tableDto : referencedTables) {
      tables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
    }
    v.setFromArray(tables);
    if(view.hasWhere()) v.setWhere(view.getWhere());

    v.setExtension(VariableListViewDto.ViewDtoExtensions.view,
        view.getExtension(VariableListViewDto.ViewDtoExtensions.view));

    return v;
  }

  private void renderSelectableTables() {
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource("/datasources/tables").get()
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            JsArray<TableDto> tables = JsArrays.toSafeArray(resource);
            TableDto viewTableDto = findViewTabledto(tables);
            getView().addSelectableTables(filterTables(tables, viewTableDto), view.getFromArray());
          }

          /**
           * Search for the table corresponding to the view.
           * @param tables
           * @return
           */
          private TableDto findViewTabledto(JsArray<TableDto> tables) {
            TableDto viewTableDto = null;
            for(TableDto table : JsArrays.toIterable(tables)) {
              if(view.getDatasourceName().equals(table.getDatasourceName()) && view.getName().equals(table.getName())) {
                viewTableDto = table;
                break;
              }
            }
            return viewTableDto;
          }

          /**
           * Remove from selection the view itself and the tables of different entity types.
           * @param tables
           * @param viewTableDto
           * @return
           */
          private JsArray<TableDto> filterTables(JsArray<TableDto> tables, TableDto viewTableDto) {
            if(viewTableDto == null) return tables;

            boolean hasEntityType = hasEntityType(viewTableDto);
            JsArray<TableDto> filteredTables = JsArrays.create();
            for(TableDto table : JsArrays.toIterable(tables)) {
              if(!table.equals(viewTableDto) && (!hasEntityType || hasEntityType(table) && table.getEntityType().equals(viewTableDto.getEntityType()))) {
                filteredTables.push(table);
              }
            }
            return filteredTables;
          }

          private boolean hasEntityType(TableDto dto) {
            return dto.hasEntityType() && !dto.getEntityType().equals("?");
          }

        }).send();
  }

  public interface Display extends PopupView, HasUiHandlers<ViewPropertiesModalUiHandlers> {

    enum FormField {
      NAME,
      TABLES
    }

    void renderProperties(ViewDto view);

    void addSelectableTables(JsArray<TableDto> tables, JsArrayString selections);

    void showError(String message, @Nullable FormField id);

    HasText getName();

    HasCollection<TableDto> getSelectedTables();
  }

  private class PropertiesValidationHandler extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired", Display.FormField.NAME.name()));
      validators.add(
          new MinimumSizeCollectionValidator<TableDto>(getView().getSelectedTables(), 1, "TableSelectionRequired",
              Display.FormField.TABLES.name()));
      validators
          .add(new MatchingTableEntitiesValidator(getView().getSelectedTables(), Display.FormField.TABLES.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(message, Display.FormField.valueOf(id));
    }
  }

}
