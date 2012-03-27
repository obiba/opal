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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.AttributeValueColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 *
 */
public class AttributesTable extends Table<AttributeDto> {

  private static Translations translations = GWT.create(Translations.class);

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

  private void initProvider(JsArray<AttributeDto> attributes) {
    JsArrayDataProvider<AttributeDto> provider = new JsArrayDataProvider<AttributeDto>();
    provider.setArray(JsArrays.toSafeArray(attributes));
    provider.addDataDisplay(this);
  }

  private void initColumns() {
    setPageSize(NavigatorView.PAGE_SIZE);
    setEmptyTableWidget(new InlineLabel(translations.noAttributesLabel()));

    addColumn(new TextColumn<AttributeDto>() {
      @Override
      public String getValue(AttributeDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    addColumn(new AttributeValueColumn(), translations.valueLabel());
  }
}
