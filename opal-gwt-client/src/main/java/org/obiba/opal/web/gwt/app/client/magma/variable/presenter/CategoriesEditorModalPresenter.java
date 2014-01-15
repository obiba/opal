/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.presenter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

/**
 *
 */
public class CategoriesEditorModalPresenter extends ModalPresenterWidget<CategoriesEditorModalPresenter.Display>
    implements CategoriesEditorModalUiHandlers {

  private final TranslationMessages translationMessages;

  private VariableDto variable;

  private TableDto tableDto;

  @Inject
  public CategoriesEditorModalPresenter(EventBus eventBus, Display display, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
  }

  public void initialize(@SuppressWarnings("ParameterHidesMemberVariable") final VariableDto variable, TableDto table) {
    this.variable = variable;
    tableDto = table;

    getView().setUiHandlers(this);

    // Fetch locales and render categories
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCE_TABLE_LOCALES.create().build(table.getDatasourceName(), table.getName()))
        .withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
          @Override
          public void onResource(Response response, JsArray<LocaleDto> locales) {
            getView().renderCategoryRows(variable.getCategoriesArray(), JsArrays.toList(locales));
          }
        }).get().send();
  }

  @Override
  public void onSave() {
    // Validate category names
    JsArray<CategoryDto> categories = JsArrays.toSafeArray(getView().getCategories());
    Collection<String> names = new HashSet<String>();
    for(CategoryDto categoryDto : JsArrays.toIterable(categories)) {
      if(!names.add(categoryDto.getName())) {
        getView().showError(translationMessages.categoryNameDuplicated(categoryDto.getName()), null);
        return;
      }
    }
    VariableDto dto = getVariableDto(categories);
    String uri = Strings.isNullOrEmpty(tableDto.getViewLink())
        ? UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
        .build(tableDto.getDatasourceName(), tableDto.getName(), variable.getName())
        : UriBuilder.create().segment("datasource", "{}", "view", "{}", "variable", "{}")
            .query("comment", translationMessages.updateVariableCategories(variable.getName()))
            .build(tableDto.getDatasourceName(), tableDto.getName(), variable.getName());

    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(uri) //
        .withResourceBody(VariableDto.stringify(dto)).accept("application/json") //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_OK) {
              getView().hide();
            } else {
              getView().showError(response.getText(), null);
            }
            fireEvent(new VariableRefreshEvent());
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK) //
        .put() //
        .send();
  }

  private VariableDto getVariableDto(JsArray<CategoryDto> categories) {
    VariableDto dto = VariableDto.create();
    dto.setLink(variable.getLink());
    dto.setIndex(variable.getIndex());
    dto.setIsNewVariable(variable.getIsNewVariable());
    dto.setParentLink(variable.getParentLink());
    dto.setName(variable.getName());
    dto.setEntityType(variable.getEntityType());
    dto.setValueType(variable.getValueType());
    dto.setIsRepeatable(variable.getIsRepeatable());
    dto.setUnit(variable.getUnit());
    dto.setReferencedEntityType(variable.getReferencedEntityType());
    dto.setMimeType(variable.getMimeType());
    dto.setOccurrenceGroup(variable.getOccurrenceGroup());
    dto.setAttributesArray(variable.getAttributesArray());
    dto.setCategoriesArray(categories);
    return dto;
  }

  public interface Display extends PopupView, HasUiHandlers<CategoriesEditorModalUiHandlers> {

    void renderCategoryRows(JsArray<CategoryDto> rows, List<LocaleDto> locales);

    JsArray<CategoryDto> getCategories();

    void showError(String message, @Nullable ControlGroup group);

  }
}
