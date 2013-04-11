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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.FileViewDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Builder for (client-side) ViewDtos.
 */
public class ViewDtoBuilder {

  private final ViewDto viewDto;

  public static ViewDtoBuilder newBuilder() {
    return new ViewDtoBuilder();
  }

  public ViewDtoBuilder() {
    viewDto = ViewDto.create();
  }

  public ViewDtoBuilder setName(String name) {
    viewDto.setName(name);
    return this;
  }

  public ViewDtoBuilder fromTables(TableDto... tableDtos) {
    if(tableDtos != null) {
      List<TableDto> list = new ArrayList<TableDto>();
      for(TableDto tableDto : tableDtos) {
        list.add(tableDto);
      }
      fromTables(list);
    }
    return this;
  }

  public ViewDtoBuilder fromTables(List<TableDto> tableDtos) {
    JsArrayString fromTables = JavaScriptObject.createArray().cast();
    for(TableDto tableDto : tableDtos) {
      fromTables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
    }
    viewDto.setFromArray(fromTables);
    return this;
  }

  public ViewDtoBuilder fromTables(JsArrayString tableFullNames) {
    viewDto.setFromArray(tableFullNames);
    return this;
  }

  public ViewDtoBuilder defaultVariableListView() {
    VariableListViewDto listDto = VariableListViewDto.create();
    listDto.setVariablesArray(JsArrays.<VariableDto>create());
    viewDto.setExtension(VariableListViewDto.ViewDtoExtensions.view, listDto);

    return this;
  }

  public ViewDtoBuilder fileView(FileViewDto dto) {
    viewDto.setExtension(FileViewDto.ViewDtoExtensions.view, dto);

    return this;
  }

  public ViewDto build() {
    return viewDto;
  }
}
