/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import java.util.Comparator;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.AttributeValueColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 *
 */
public class AttributesTable extends Table<AttributeDto> {

  private static Translations translations = GWT.create(Translations.class);

  private static class Columns {

    private static abstract class SortableTextColumn extends TextColumn<AttributeDto> {

      protected SortableTextColumn() {
        setSortable(true);
      }
    }

    static Column<AttributeDto, String> name = new SortableTextColumn() {

      @Override
      public String getValue(AttributeDto object) {
        return object.getName();
      }
    };

    static Column<AttributeDto, String> namespace = new SortableTextColumn() {

      @Override
      public String getValue(AttributeDto object) {
        return object.getNamespace();
      }
    };

    static Comparator<AttributeDto> nameComparator = Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsFirst().onResultOf(new Function<AttributeDto, String>() {

      @Override
      public String apply(AttributeDto input) {
        return input.getName();
      }
    });

    static Comparator<AttributeDto> namespaceComparator = Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsFirst().onResultOf(new Function<AttributeDto, String>() {

      @Override
      public String apply(AttributeDto input) {
        return input.getNamespace();
      }
    });

  }

  private HandlerRegistration registration;

  public AttributesTable() {
    super();
    initColumns();
  }

  public AttributesTable(VariableDto variableDto) {
    super();
    initColumns();

    if(variableDto != null) {
      initProvider(variableDto.getAttributesArray());
    }
  }

  public AttributesTable(CategoryDto categoryDto) {
    super();
    initColumns();

    if(categoryDto != null) {
      initProvider(categoryDto.getAttributesArray());
    }
  }

  public void setupSort(JsArrayDataProvider<AttributeDto> provider) {
    if(registration != null) {
      registration.removeHandler();
      registration = null;
    }
    ColumnSortEvent.ListHandler<AttributeDto> handler = provider.newSortHandler();
    handler.setComparator(Columns.name, Columns.nameComparator);
    handler.setComparator(Columns.namespace, Columns.namespaceComparator);
    registration = addColumnSortHandler(handler);
  }

  private void initProvider(JsArray<AttributeDto> attributes) {
    JsArrayDataProvider<AttributeDto> provider = new JsArrayDataProvider<AttributeDto>();
    provider.setArray(JsArrays.toSafeArray(attributes));
    provider.addDataDisplay(this);
    setupSort(provider);
  }

  private void initColumns() {
    setPageSize(NavigatorView.PAGE_SIZE);
    setEmptyTableWidget(new InlineLabel(translations.noAttributesLabel()));
    addColumn(Columns.namespace, translations.namespaceLabel());
    addColumn(Columns.name, translations.nameLabel());
    addColumn(new AttributeValueColumn(), translations.valueLabel());

  }
}
