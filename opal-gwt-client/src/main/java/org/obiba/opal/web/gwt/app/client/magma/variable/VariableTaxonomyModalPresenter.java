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

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.JsArrayString;
import org.obiba.opal.web.gwt.app.client.support.OpalSystemCache;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VariableTaxonomyModalPresenter
    extends BaseVariableAttributeModalPresenter<VariableTaxonomyModalPresenter.Display> {

  @Inject
  public VariableTaxonomyModalPresenter(Display display, EventBus eventBus, OpalSystemCache opalSystemCache) {
    super(eventBus, display, opalSystemCache);
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
    renderLocales();
    renderTaxonomies();
  }

  private void renderTaxonomies() {
    opalSystemCache.requestTaxonomies(new OpalSystemCache.TaxonomiesHandler() {
      @Override
      public void onTaxonomies(List<TaxonomyDto> taxonomies) {
        getView().setTaxonomies(taxonomies);
        applySelectedItems();
      }
    });
  }

  private void renderLocales() {
    opalSystemCache.requestLocales(new OpalSystemCache.LocalesHandler() {
      @Override
      public void onLocales(JsArrayString locales) {
        getView().setLocales(locales);
      }
    });
  }

  public interface Display extends BaseVariableAttributeModalPresenter.Display {

    void setTaxonomies(List<TaxonomyDto> taxonomies);

    void setLocales(JsArrayString locales);
  }

}
