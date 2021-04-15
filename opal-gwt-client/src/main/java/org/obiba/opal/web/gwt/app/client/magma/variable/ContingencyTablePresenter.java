/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.variable;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ContingencyTablePresenter extends PresenterWidget<ContingencyTablePresenter.Display> {

  public static final String TOTAL_FACET = "_total";

  @Inject
  public ContingencyTablePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  public void initialize(TableDto table, final VariableDto variableDto, final VariableDto crossWithVariable) {
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder() //
        .forResource(
            UriBuilders.DATASOURCES_ENTITIES_CONTINGENCY.create()
                .query("v0", MagmaPath.Builder.datasource(table.getDatasourceName()).table(table.getName()).variable(variableDto.getName()).build())
                .query("v1", MagmaPath.Builder.datasource(table.getDatasourceName()).table(table.getName()).variable(crossWithVariable.getName()).build())
                .build()) //
        .get() //
        .withCallback(new ResourceCallback<QueryResultDto>() {
          @Override
          public void onResource(Response response, QueryResultDto resource) {
            List<String> variableCategories = getCategories(variableDto);
            List<String> crossWithCategories = getCategories(crossWithVariable);
            getView().show(resource, variableDto, variableCategories, crossWithVariable, crossWithCategories);
          }
        }).send();
  }

  private List<String> getCategories(VariableDto variable) {
    List<String> categories = new ArrayList<String>();

    if("boolean".equals(variable.getValueType())) {
      categories.add("true");
      categories.add("false");
    } else {
      for(CategoryDto categoryDto : JsArrays.toIterable(variable.getCategoriesArray())) {
        categories.add(categoryDto.getName());
      }
    }

    return categories;
  }

  public interface Display extends View {

    void show(QueryResultDto resource, VariableDto variableDto, List<String> variableCategories,
        VariableDto crossWithVariable, List<String> crossWithCategories);

  }

}