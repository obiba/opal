/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.HashMap;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.view.NamespacedAttributesTable;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.FlowPanel;

public class AttributesTablesPanel extends FlowPanel {

  protected final Translations translations = GWT.create(Translations.class);

  private Map<String, NamespacedAttributeTableProvider> namespacedTableProviderMap;

  public AttributesTablesPanel() {
  }

  public void init() {
  }

  public void renderRows(VariableDto variableDto) {
    namespacedTableProviderMap = new HashMap<String, NamespacedAttributeTableProvider>();
    JsArray<AttributeDto> attributesArray = JsArrays.toSafeArray(variableDto.getAttributesArray());

    for(int i = 0; i < attributesArray.length(); i++) {
      String namespace = attributesArray.get(i).getNamespace();
      if(!namespacedTableProviderMap.containsKey(namespace)) {
        namespacedTableProviderMap.put(namespace, new NamespacedAttributeTableProvider());
      }

      namespacedTableProviderMap.get(namespace).addAttribute(attributesArray.get(i));
    }

    refreshAllTables();
  }

  private void refreshAllTables() {
    clear();
    for(String key : namespacedTableProviderMap.keySet()) {
      NamespacedAttributeTableProvider attributeTableProvider = namespacedTableProviderMap.get(key);
      attributeTableProvider.refreshProvider();

      if(!key.isEmpty()) {
        add(new Heading(5, translations.namespaceLabel() + ": " + key));
      }

      add(attributeTableProvider.getPager());
      add(attributeTableProvider.getTable());
    }
  }

  private class NamespacedAttributeTableProvider {
    private NamespacedAttributesTable table = new NamespacedAttributesTable();

    private final SimplePager pager = new SimplePager();

    private final JsArrayDataProvider<JsArray<AttributeDto>> provider
        = new JsArrayDataProvider<JsArray<AttributeDto>>();

    private final Map<String, JsArray<AttributeDto>> attributesMap = new HashMap<String, JsArray<AttributeDto>>();

    NamespacedAttributeTableProvider() {
      table.setPageSize(Table.DEFAULT_PAGESIZE);
      pager.setDisplay(table);
      pager.addStyleName("pull-right");
      provider.addDataDisplay(table);
    }

    void addAttribute(AttributeDto attributeDto) {
      JsArray<AttributeDto> attributes = attributesMap.get(attributeDto.getName());

      if(attributes == null) {
        attributes = JsArrays.create().cast();
      }

      attributes.push(attributeDto);
      attributesMap.put(attributeDto.getName(), attributes);
    }

    NamespacedAttributesTable getTable() {
      return table;
    }

    void refreshProvider() {
      // TODO: Sort column names and values
      JsArray<JsArray<AttributeDto>> rows = JsArrays.create().cast();
      for(String key : attributesMap.keySet()) {
        rows.push(attributesMap.get(key));
      }

      pager.firstPage();
      pager.setVisible(rows.length() > Table.DEFAULT_PAGESIZE);

      provider.setArray(rows);
      provider.refresh();
    }

    public SimplePager getPager() {
      return pager;
    }
  }
}

