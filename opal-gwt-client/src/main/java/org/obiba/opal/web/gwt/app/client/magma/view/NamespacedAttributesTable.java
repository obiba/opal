/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.util.Comparator;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ListAttributeValueColumn;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 *
 */
public class NamespacedAttributesTable extends Table<JsArray<AttributeDto>> {

  protected static final Translations translations = GWT.create(Translations.class);

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

  private HandlerRegistration registration;

  public NamespacedAttributesTable() {
    initColumns();
  }

  public void setupSort(JsArrayDataProvider<JsArray<AttributeDto>> provider) {
    if(registration != null) {
      registration.removeHandler();
      registration = null;
    }
    ColumnSortEvent.ListHandler<JsArray<AttributeDto>> handler = provider.newSortHandler();
    handler.setComparator(Columns.NAME, Columns.NAME_COMPARATOR);
    registration = addColumnSortHandler(handler);
  }

  protected void initColumns() {
    setPageSize(Table.DEFAULT_PAGESIZE);
    setEmptyTableWidget(new InlineLabel(translations.noAttributesLabel()));
    addColumn(Columns.NAME, translations.nameLabel());
    addColumn(new ListAttributeValueColumn(), translations.valueLabel());
  }
}
