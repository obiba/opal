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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

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

//  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  private VariableDto variable;

  private List<String> locales;

  @Inject
  public CategoriesEditorModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  public void initialize(VariableDto variable, List<String> locales) {
    this.variable = variable;
    this.locales = locales;
    getView().setUiHandlers(this);
    getView().renderCategoryRows(variable.getCategoriesArray(), locales);
  }

  @Override
  public void onSave() {
    variable.clearCategoriesArray();
    variable.setCategoriesArray(getView().getCategories());

    saveVariable();
    getView().hide();
  }

  @Override
  public void onDelete() {
    JsArray<CategoryDto> categories = JsArrays.toSafeArray(getView().getSelectedCategories());
    JsArray<CategoryDto> newCategories = JsArrays.create();

    for(int i = 0; i < variable.getCategoriesArray().length(); i++) {
      boolean removed = false;
      for(int j = 0; j < categories.length(); j++) {
        if(categories.get(j).getName().equals(variable.getCategoriesArray().get(i).getName())) {
          removed = true;
          break;
        }
      }

      if(!removed) {
        newCategories.push(variable.getCategoriesArray().get(i));
      }
    }

    variable.clearCategoriesArray();
    variable.setCategoriesArray(newCategories);
    saveVariable();
    getView().renderCategoryRows(variable.getCategoriesArray(), locales);

  }

  private void saveVariable() {
    ResourceRequestBuilderFactory.newBuilder().forResource(variable.getLink()) //
        .put() //
        .withResourceBody(VariableDto.stringify(variable)) //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            GWT.log(response.getStatusText());
            fireEvent(new VariableRefreshEvent());
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  public interface Display extends PopupView, HasUiHandlers<CategoriesEditorModalUiHandlers> {
    void renderCategoryRows(JsArray<CategoryDto> rows, List<String> locales);

    JsArray<CategoryDto> getCategories();

    JsArray<CategoryDto> getSelectedCategories();
  }
}
