/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.inject.Singleton;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.search.ItemFieldsDto;
import org.obiba.opal.web.model.client.search.ItemResultDto;

import java.util.List;
import java.util.Map;

@Singleton
public class CartService {

  private static final String VARIABLES_PREFIX = "/variables/";

  private final Map<String, CartVariableItem> variables = Maps.newHashMap();

  private Storage stockStore = null;

  public CartService() {
    stockStore = Storage.getLocalStorageIfSupported();
    GWT.log("Local storage is supported: " + isStoreSupported());
  }

  public void addVariableItems(Map<String, List<ItemResultDto>> tableVariableItems) {
    for (String tableRef : tableVariableItems.keySet()) {
      MagmaPath.Parser refParser = MagmaPath.Parser.parse(tableRef);
      for (ItemResultDto item : tableVariableItems.get(tableRef)) {
        addVariable(refParser.getDatasource(), refParser.getTable(), asVariable(item));
      }
    }
  }

  public void addVariables(Map<String, List<VariableDto>> tableVariables) {
    for (String tableRef : tableVariables.keySet()) {
      MagmaPath.Parser refParser = MagmaPath.Parser.parse(tableRef);
      for (VariableDto variable : tableVariables.get(tableRef)) {
        addVariable(refParser.getDatasource(), refParser.getTable(), stripVariable(variable));
      }
    }
  }

  public void addVariable(String datasource, String table, VariableDto variable) {
    addVariable(getVariableFullName(datasource, table, variable.getName()), stripVariable(variable));
  }

  private void addVariable(String variableFullName, VariableDto variable) {
    if (Strings.isNullOrEmpty(variableFullName)) return;
    String variableStr = VariableDto.stringify(variable);
    if (isStoreSupported())
      stockStore.setItem(VARIABLES_PREFIX + variableFullName, variableStr);
    else
      addVariableInMemory(variableFullName, variableStr);
  }

  public void removeVariable(String variableFullName) {
    if (Strings.isNullOrEmpty(variableFullName)) return;
    if (isStoreSupported()) removeVariableInStore(variableFullName);
    else variables.remove(variableFullName);
  }

  public int getVariablesCount() {
    return !isStoreSupported() ? variables.size() : stockStore.getLength();
  }

  public boolean hasVariable(TableDto tableDto, VariableDto variableDto) {
    return hasVariable(tableDto.getDatasourceName(), tableDto.getName(), variableDto.getName());
  }

  public boolean hasVariable(String datasource, String table, String variable) {
    String variableFullName = getVariableFullName(datasource, table, variable);
    return !isStoreSupported() ? hasVariableInMemory(variableFullName)
        : hasVariableInStore(variableFullName);
  }

  public List<CartVariableItem> getVariables() {
    return isStoreSupported() ? getVariablesInStore() : Lists.newArrayList(variables.values());
  }

  public void clear() {
    if (isStoreSupported()) stockStore.clear();
    else variables.clear();
  }

  public void clearVariables() {
    // only variables in the store for now
    clear();
  }
  
  private boolean isStoreSupported() {
    return stockStore != null;
  }

  private String getVariableFullName(String datasource, String table, String variable) {
    return MagmaPath.Builder.datasource(datasource).table(table).variable(variable).build();
  }

  private VariableDto asVariable(ItemResultDto item) {
    ItemFieldsDto fields = (ItemFieldsDto) item.getExtension("Search.ItemFieldsDto.item");
    VariableDto variable = VariableDto.create();
    JsArray<AttributeDto> attributes = JsArrays.create();
    for (EntryDto entry : JsArrays.toIterable(fields.getFieldsArray())) {
      if ("name".equals(entry.getKey())) variable.setName(entry.getValue());
      else if ("entityType".equals(entry.getKey())) variable.setEntityType(entry.getValue());
      else if ("label".equals(entry.getKey())) {
        AttributeDto attr = AttributeDto.create();
        attr.setName("label");
        attr.setValue(entry.getValue());
        attributes.push(attr);
      }
      else if (entry.getKey().startsWith("label-")) {
        AttributeDto attr = AttributeDto.create();
        attr.setName("label");
        attr.setLocale(entry.getKey().substring(6));
        attr.setValue(entry.getValue());
        attributes.push(attr);
      }
    }
    variable.setAttributesArray(attributes);
    return variable;
  }

  /**
   * Remove unnecessary information to save space in the store.
   *
   * @param variable
   * @return
   */
  private VariableDto stripVariable(VariableDto variable) {
    VariableDto stripped = VariableDto.create();
    stripped.setName(variable.getName());
    stripped.setEntityType(variable.getEntityType());
    JsArray<AttributeDto> attributes = JsArrays.create();
    for (AttributeDto attr : JsArrays.toIterable(variable.getAttributesArray())) {
      if (!attr.hasNamespace() && "label".equals(attr.getName()))
        attributes.push(attr);
    }
    stripped.setAttributesArray(attributes);
    return stripped;
  }

  //
  // Local store
  //

  private boolean hasVariableInStore(String variableFullName) {
    StorageMap stockMap = new StorageMap(stockStore);
    return stockMap.containsValue(VARIABLES_PREFIX + variableFullName);
  }

  private List<CartVariableItem> getVariablesInStore() {
    List<CartVariableItem> vars = Lists.newArrayList();
    for (int i=0; i<stockStore.getLength(); i++) {
      String key = stockStore.key(i);
      if (key.startsWith(VARIABLES_PREFIX))
        vars.add(new CartVariableItem(getVariableFullName(key), stockStore.getItem(key)));
    }
    return vars;
  }

  private List<CartVariableItem> getVariablesInStore(String entityType) {
    List<CartVariableItem> vars = Lists.newArrayList();
    for (int i=0; i<stockStore.getLength(); i++) {
      String key = stockStore.key(i);
      if (key.startsWith(VARIABLES_PREFIX)) {
        String type = stockStore.getItem(key);
        if (type.equals(entityType)) vars.add(new CartVariableItem(getVariableFullName(key), type));
      }
    }
    return vars;
  }

  private void removeVariableInStore(String variableFullName) {
    stockStore.removeItem(VARIABLES_PREFIX + variableFullName);
  }

  //
  // In memory
  //

  private void addVariableInMemory(String variableFullName, String variableStr) {
    variables.put(variableFullName, new CartVariableItem(variableFullName, variableStr));
  }

  private boolean hasVariableInMemory(String variableFullName) {
    return variables.containsKey(variableFullName);
  }

  private String getVariableFullName(String storeKey) {
    return storeKey.replace(VARIABLES_PREFIX, "");
  }
}
