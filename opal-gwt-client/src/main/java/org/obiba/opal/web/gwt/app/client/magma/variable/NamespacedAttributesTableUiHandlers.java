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

import java.util.List;

import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.core.client.JsArray;
import com.gwtplatform.mvp.client.UiHandlers;

public interface NamespacedAttributesTableUiHandlers extends UiHandlers {

  void onDeleteAttribute(List<JsArray<AttributeDto>> selectedItems);

  void onEditAttributes(List<JsArray<AttributeDto>> selectedItems);
}
