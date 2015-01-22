/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.presenter;

import java.util.Collection;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VariableTaxonomyModalPresenter
    extends BaseVariableAttributeModalPresenter<VariableTaxonomyModalPresenter.Display> {

  @Inject
  public VariableTaxonomyModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void initialize(TableDto tableDto, Collection<VariableDto> variableDtos) {
    super.initialize(tableDto, variableDtos);
    renderTaxonomies();
  }


  @Override
  public void initialize(TableDto tableDto, VariableDto variableDto, final List<JsArray<AttributeDto>> selectedItems) {
    super.initialize(tableDto, variableDto, selectedItems);
    renderTaxonomies();
  }

  private void renderTaxonomies() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES.create().build()).get()
        .withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
          @Override
          public void onResource(Response response, JsArray<TaxonomyDto> resource) {
            getView().setTaxonomies(JsArrays.toList(resource));
            applySelectedItems();
          }
        }).send();
  }

  public interface Display extends BaseVariableAttributeModalPresenter.Display {

    void setTaxonomies(List<TaxonomyDto> taxonomies);

  }

}
