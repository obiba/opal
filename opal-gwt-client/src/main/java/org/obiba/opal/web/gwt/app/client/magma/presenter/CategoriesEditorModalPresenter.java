/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.gwt.core.client.GWT;
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

  private static final Translations translations = GWT.create(Translations.class);

  private VariableDto variable;

  private TableDto tableDto;

  private List<LocaleDto> locales;

  @Inject
  public CategoriesEditorModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  public void initialize(VariableDto variable, TableDto table) {
    this.variable = variable;
    tableDto = table;
    locales = new ArrayList<LocaleDto>();
    getView().setUiHandlers(this);

//    // Fetch locales and render categories
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>>newBuilder()
        .forResource(UriBuilder.URI_DATASOURCE_TABLE_LOCALES.build(table.getDatasourceName(), table.getName())).get()
        .withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
          @Override
          public void onResource(Response response, JsArray<LocaleDto> resource) {
            locales = JsArrays.toList(JsArrays.toSafeArray(resource));
            getView().renderCategoryRows(CategoriesEditorModalPresenter.this.variable.getCategoriesArray(), locales);
          }
        }).send();

  }

  @Override
  public void onSave() {
    // Validate category names
    Set<String> names = new HashSet<String>();
    JsArray<CategoryDto> categories = JsArrays.toSafeArray(getView().getCategories());
    for(int i = 0; i < categories.length(); i++) {
      if(!names.add(categories.get(i).getName())) {
        getView().showError(
            TranslationsUtils.replaceArguments(translations.categoryNameDuplicated(), categories.get(i).getName()),
            null);
        return;
      }
    }

    variable.clearCategoriesArray();
    variable.setCategoriesArray(categories);

    ResourceRequestBuilderFactory.newBuilder().forResource(variable.getLink()) //
        .put() //
        .withResourceBody(VariableDto.stringify(variable)) //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() != Response.SC_OK) {
              getView().showError(response.getText(), null);
            } else {
              getView().hide();
            }
            fireEvent(new VariableRefreshEvent());
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();

  }

  public interface Display extends PopupView, HasUiHandlers<CategoriesEditorModalUiHandlers> {
    void renderCategoryRows(JsArray<CategoryDto> rows, List<LocaleDto> locales);

    JsArray<CategoryDto> getCategories();

    void showError(String message, @Nullable ControlGroup group);
  }
}
