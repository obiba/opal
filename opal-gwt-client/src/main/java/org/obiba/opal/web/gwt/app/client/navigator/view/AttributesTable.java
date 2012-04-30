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
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 *
 */
public class AttributesTable extends Table<AttributeDto> {

  private static Translations translations = GWT.create(Translations.class);

  private static class Columns {

    private static abstract class SortableTextColumn extends Column<AttributeDto, String> {

      protected SortableTextColumn() {
        super(new TextCell(new SafeHtmlRenderer<String>() {

          @Override
          public SafeHtml render(String object) {
            return (object == null) ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromTrustedString(object);
          }

          @Override
          public void render(String object, SafeHtmlBuilder appendable) {
            appendable.append(SafeHtmlUtils.fromTrustedString(object));
          }
        }));
        setSortable(true);
      }
    }

    static Column<AttributeDto, String> name = new SortableTextColumn() {

      @Override
      public String getValue(AttributeDto object) {
        StringBuilder label = new StringBuilder();
        if(object.hasNamespace()) {
          label.append("<span class=\"label\">").append(object.getNamespace()).append("</span> ");
        }
        label.append(object.getName());
        return label.toString();
      }
    };

    static Comparator<AttributeDto> nameComparator = Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsFirst().onResultOf(new Function<AttributeDto, String>() {

      @Override
      public String apply(AttributeDto input) {
        return input.getName();
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
    addColumn(Columns.name, translations.nameLabel());
    addColumn(new AttributeValueColumn(), translations.valueLabel());
  }
}
