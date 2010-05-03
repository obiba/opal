/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.spring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VariableResource {

  @RequestMapping(value = "/datasource/{datasource}/{table}/{variable}/values.json", method = RequestMethod.GET)
  @ResponseBody
  public String getTables(@PathVariable String datasource, @PathVariable String table, @PathVariable String variable) {
    ValueTable valueTable = MagmaEngine.get().getDatasource(datasource).getValueTable(table);
    Map<String, Object> response = new HashMap<String, Object>();

    List<Object> values = new LinkedList<Object>();
    VariableValueSource vvs = valueTable.getVariableValueSource(variable);
    for(ValueSet vs : valueTable.getValueSets()) {
      values.add(vvs.getValue(vs).getValue());
    }
    response.put(vvs.getVariable().getName(), values);

    return new JSONObject(response).toString();
  }
}
