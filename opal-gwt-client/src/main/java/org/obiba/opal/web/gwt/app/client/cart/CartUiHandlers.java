/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart;

import com.gwtplatform.mvp.client.UiHandlers;
import org.obiba.opal.web.gwt.app.client.cart.service.CartVariableItem;

import java.util.List;

public interface CartUiHandlers extends UiHandlers {

  void onClearVariables();

  void onRemoveVariable(String variableFullName);

  void onRemoveVariables(List<CartVariableItem> selectedVariables);

  void onSearchEntities(List<CartVariableItem> selectedVariables);

  void onApplyAnnotation(List<CartVariableItem> selectedVariables);

  void onDeleteAnnotation(List<CartVariableItem> selectedVariables);

  void onAddToView(List<CartVariableItem> selectedVariables);
}
