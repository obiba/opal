/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.support;

import java.util.List;

import org.obiba.opal.web.model.client.magma.JavaScriptViewDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Builder for (client-side) ViewDtos.
 */
public class ViewDtoBuilder {

  private ViewDto viewDto;

  public static ViewDtoBuilder newBuilder() {
    return new ViewDtoBuilder();
  }

  public ViewDtoBuilder() {
    viewDto = ViewDto.create();
  }

  public ViewDtoBuilder fromTables(List<TableDto> tableDtos) {
    JsArrayString fromTables = JavaScriptObject.createArray().cast();
    for(TableDto tableDto : tableDtos) {
      fromTables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
    }
    viewDto.setFromArray(fromTables);

    return this;
  }

  public ViewDtoBuilder defaultJavaScriptView() {
    JavaScriptViewDto jsDto = JavaScriptViewDto.create();
    viewDto.setExtension(JavaScriptViewDto.ViewDtoExtensions.view, jsDto);

    return this;
  }

  public ViewDtoBuilder defaultVariableListView() {
    VariableListViewDto listDto = VariableListViewDto.create();
    viewDto.setExtension(VariableListViewDto.ViewDtoExtensions.view, listDto);

    return this;
  }

  public ViewDto build() {
    return viewDto;
  }
}
