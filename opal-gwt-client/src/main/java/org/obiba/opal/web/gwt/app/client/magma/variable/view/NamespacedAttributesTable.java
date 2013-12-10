/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.NamespacedAttributesTableUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ListAttributeValueColumn;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class NamespacedAttributesTable extends ViewWithUiHandlers<NamespacedAttributesTableUiHandlers> {

  interface NamespacedAttributesTableViewUiBinder extends UiBinder<Widget, NamespacedAttributesTable> {}

  private static final NamespacedAttributesTableViewUiBinder uiBinder = GWT
      .create(NamespacedAttributesTableViewUiBinder.class);

  protected static final Translations translations = GWT.create(Translations.class);

  private CheckboxColumn<JsArray<AttributeDto>> checkColumn;

  @UiField
  Heading namespaceLabel;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  SimplePager pager;

  @UiField
  Table<JsArray<AttributeDto>> table;

//  private HandlerRegistration registration;

  private final ListDataProvider<JsArray<AttributeDto>> provider = new ListDataProvider<JsArray<AttributeDto>>();

  private final Map<String, JsArray<AttributeDto>> attributesMap = new HashMap<String, JsArray<AttributeDto>>();

  public NamespacedAttributesTable(JsArray<AttributeDto> attributes, String namespace) {

    initWidget(uiBinder.createAndBindUi(this));

    if(!namespace.isEmpty()) {
      namespaceLabel.setText(translations.namespaceLabel() + ": " + namespace);
    }

    initColumns();

    List<AttributeDto> attributesArray = JsArrays.toList(JsArrays.toSafeArray(attributes));
    Collections.sort(attributesArray, new Comparator<AttributeDto>() {
      @Override
      public int compare(AttributeDto attributeDto, AttributeDto attributeDto2) {
        return attributeDto.getLocale().compareTo(attributeDto2.getLocale());
      }
    });

    for(AttributeDto attributeDto : attributesArray) {
      if(namespace.equals(attributeDto.getNamespace())) {
        addAttribute(attributeDto);
      }
    }
    refreshProvider();
  }

  @UiHandler("deleteLink")
  public void onDelete(ClickEvent event) {
    getUiHandlers().onDeleteAttribute(checkColumn.getSelectedItems());
  }

  public void initColumns() {
    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(new InlineLabel(translations.noAttributesLabel()));

    // Add checkcolumn
    checkColumn = new CheckboxColumn<JsArray<AttributeDto>>(new CheckAttributesDisplay());

    table.addColumn(checkColumn, checkColumn.getTableListCheckColumnHeader());
    table.setColumnWidth(checkColumn, 1, Style.Unit.PX);

    table.addColumn(Columns.NAME, translations.nameLabel());
    table.addColumn(new ListAttributeValueColumn(), translations.valueLabel());

    pager.setDisplay(table);
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

  void refreshProvider() {
    JsArray<JsArray<AttributeDto>> rows = JsArrays.create().cast();
    List<String> keys = new ArrayList<String>(attributesMap.keySet());

    Collections.sort(keys);
    for(String key : keys) {
      rows.push(attributesMap.get(key));
    }

    pager.firstPage();
    pager.setVisible(rows.length() > Table.DEFAULT_PAGESIZE);

    provider.setList(JsArrays.toList(rows));
    provider.refresh();
  }

  private static class Columns {

    static final TextColumn<JsArray<AttributeDto>> NAME = new TextColumn<JsArray<AttributeDto>>() {

      @Override
      public String getValue(JsArray<AttributeDto> object) {
        return object.get(0).getName();
      }
    };

    static {
      NAME.setSortable(true);
    }

    static final Comparator<JsArray<AttributeDto>> NAME_COMPARATOR = Ordering.from(String.CASE_INSENSITIVE_ORDER)
        .nullsFirst().onResultOf(new Function<JsArray<AttributeDto>, String>() {

          @Override
          public String apply(JsArray<AttributeDto> input) {
            return input.get(0).getName();
          }
        });

  }

  private class CheckAttributesDisplay implements CheckboxColumn.Display<JsArray<AttributeDto>> {

    @Override
    public Table<JsArray<AttributeDto>> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(JsArray<AttributeDto> item) {
      return item.get(0).getName();
    }

    @Override
    public IconAnchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public IconAnchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public HasText getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public ListDataProvider<JsArray<AttributeDto>> getDataProvider() {
      return provider;
    }

    @Override
    public String getItemNamePlural() {
      return translations.attributesLabel().toLowerCase();
    }

    @Override
    public String getItemNameSingular() {
      return translations.attributeLabel().toLowerCase();
    }

    @Override
    public Alert getAlert() {
      return selectAllItemsAlert;
    }
  }
}
