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
import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.inject.Singleton;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;

@Singleton
public class CartService {

  private static final String VARIABLE_PREFIX = "variable-";

  private final List<String> variables = Lists.newArrayList();

  private Storage stockStore = null;

  public CartService() {
    stockStore = Storage.getLocalStorageIfSupported();
    GWT.log("Local storage is supported: " + (stockStore != null));
  }

  public void addVariable(String datasource, String table, String variable) {
    addVariable(getVariableFullName(datasource, table, variable));
  }

  public void addVariable(String variableFullName) {
    if (Strings.isNullOrEmpty(variableFullName)) return;
    if (!isStoreSupported()) addVariableInMemory(variableFullName);
    else if (!hasVariableInStore(variableFullName)){
      stockStore.setItem(VARIABLE_PREFIX + stockStore.getLength(), variableFullName);
    }
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

  public List<String> getVariables() {
    return !isStoreSupported() ? variables : getVariablesInStore();
  }

  public void clear() {
    if (!isStoreSupported()) variables.clear();
    else stockStore.clear();
  }

  public void clearVariables() {
    if (!isStoreSupported()) variables.clear();
    else clearVariablesInStore();
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
    return stockMap.containsValue(variableFullName);
  }

  private List<String> getVariablesInStore() {
    List<String> vars = Lists.newArrayList();
    for (int i=0; i<stockStore.getLength(); i++) {
      vars.add(stockStore.getItem(stockStore.key(i)));
    }
    return vars;
  }

  private void removeVariableInStore(String variableFullName) {
    StorageMap stockMap = new StorageMap(stockStore);
    if (stockMap.containsValue(variableFullName)) {
      for (int i=stockStore.getLength()-1; i>=0; i--) {
        String key = stockStore.key(i);
        if (key.startsWith(VARIABLE_PREFIX)) {
          if (variableFullName.equals(stockStore.getItem(key))) stockStore.removeItem(key);
        }
      }
    }
  }

  private void clearVariablesInStore() {
    for (int i=stockStore.getLength()-1; i>=0; i--) {
      String key = stockStore.key(i);
      if (key.startsWith(VARIABLE_PREFIX)) stockStore.removeItem(key);
    }
  }

  //
  // In memory
  //

  private void addVariableInMemory(String variableFullName) {
    if (!variables.contains(variableFullName)) variables.add(variableFullName);
  }

  private boolean hasVariableInMemory(String variableFullName) {
    return variables.contains(variableFullName);
  }
}
