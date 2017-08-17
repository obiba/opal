/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.inject.Singleton;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;
import java.util.Map;

@Singleton
public class CartService {

  private static final String VARIABLES_PREFIX = "/variables/";

  private final Map<String, CartVariableItem> variables = Maps.newHashMap();

  private Storage stockStore = null;

  public CartService() {
    stockStore = Storage.getLocalStorageIfSupported();
    GWT.log("Local storage is supported: " + (stockStore != null));
  }

  public void addVariables(String entityType, List<String> variables) {
    for (String variable : variables)
      addVariable(entityType, variable);
  }

  public void addVariable(String entityType, String datasource, String table, String variable) {
    addVariable(entityType, getVariableFullName(datasource, table, variable));
  }

  public void addVariable(String entityType, String variableFullName) {
    if (Strings.isNullOrEmpty(variableFullName)) return;
    if (isStoreSupported())
      stockStore.setItem(VARIABLES_PREFIX + variableFullName, entityType);
    else
      addVariableInMemory(entityType, variableFullName);
  }

  public void removeVariable(String datasource, String table, String variable) {
    removeVariable(getVariableFullName(datasource, table, variable));
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
        vars.add(new CartVariableItem(key.replace(VARIABLES_PREFIX,""), stockStore.getItem(key)));
    }
    return vars;
  }

  private List<CartVariableItem> getVariablesInStore(String entityType) {
    List<CartVariableItem> vars = Lists.newArrayList();
    for (int i=0; i<stockStore.getLength(); i++) {
      String key = stockStore.key(i);
      if (key.startsWith(VARIABLES_PREFIX)) {
        String type = stockStore.getItem(key);
        if (type.equals(entityType)) vars.add(new CartVariableItem(key.replace(VARIABLES_PREFIX, ""), type));
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

  private void addVariableInMemory(String entityType, String variableFullName) {
    variables.put(variableFullName, new CartVariableItem(variableFullName, entityType));
  }

  private boolean hasVariableInMemory(String variableFullName) {
    return variables.containsKey(variableFullName);
  }
}
