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
import java.util.List;

import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VariableAttributeModalPresenter
    extends BaseVariableAttributeModalPresenter<VariableAttributeModalPresenter.Display>
    implements VariableAttributeModalUiHandlers {

  @Inject
  public VariableAttributeModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void initialize(TableDto tableDto, Collection<VariableDto> variableDtos) {
    super.initialize(tableDto, variableDtos);
    getView().setNamespaceSuggestions(variables);
  }

  public void initialize(TableDto tableDto, VariableDto variableDto, final List<JsArray<AttributeDto>> selectedItems) {
    super.initialize(tableDto, variableDto, selectedItems);
    getView().setNamespaceSuggestions(variables);
    applySelectedItems();
  }

  public interface Display extends BaseVariableAttributeModalPresenter.Display {

    void setNamespaceSuggestions(List<VariableDto> variableDto);

  }

}
