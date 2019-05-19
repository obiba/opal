/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entity;

import com.google.common.base.Strings;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

public class VariableValueRow {

  private final String datasource;

  private final String table;

  private final String variable;

  private final ValueSetsDto.ValueDto valueDto;

  private final VariableDto variableDto;

  public VariableValueRow(String variable, ValueSetsDto.ValueDto valueDto, VariableDto variableDto) {
    this("","",variable, valueDto, variableDto);
  }

  public VariableValueRow(String datasource, String table, String variable, ValueSetsDto.ValueDto valueDto, VariableDto variableDto) {
    this.datasource = datasource;
    this.table = table;
    this.variable = variable;
    this.valueDto = valueDto;
    this.variableDto = variableDto;
  }

  public String getDatasource() {
    return datasource;
  }

  public String getTable() {
    return table;
  }

  public String getVariable() {
    return variable;
  }

  public ValueSetsDto.ValueDto getValueDto() {
    return valueDto;
  }

  public VariableDto getVariableDto() {
    return variableDto;
  }

  public boolean hasEmptyValue() {
    if (variableDto.getIsRepeatable())
      return valueDto.getValuesArray() == null || valueDto.getValuesArray().length() == 0;
    else
      return !valueDto.hasValue() || Strings.isNullOrEmpty(valueDto.getValue());
  }

}
