/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CategoryAttributeColumn;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.IconCell;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 *
 */
public class CategoriesTable extends Table<CategoryDto> {

  private static final Translations translations = GWT.create(Translations.class);

  public CategoriesTable() {
    this(null);
  }

  public CategoriesTable(VariableDto variableDto) {
    initColumns();

    if(variableDto != null) {
      JsArrayDataProvider<CategoryDto> provider = new JsArrayDataProvider<CategoryDto>();
      provider.setArray(JsArrays.toSafeArray(variableDto.getCategoriesArray()));
      provider.addDataDisplay(this);
    }
  }

  private void initColumns() {
    setPageSize(Table.DEFAULT_PAGESIZE);
    setEmptyTableWidget(new InlineLabel(translations.noCategoriesLabel()));

    addColumn(new TextColumn<CategoryDto>() {
      @Override
      public String getValue(CategoryDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    addColumn(new CategoryAttributeColumn("label"), translations.labelLabel());

    Column<CategoryDto, Boolean> missings = new Column<CategoryDto, Boolean>(new IconCell<Boolean>() {
      @Override
      public IconType getIconType(Boolean value) {
        //return value ? IconType.CHECK : IconType.CHECK_EMPTY;
        return value ? IconType.OK : null;
      }
    }) {
      @Override
      public Boolean getValue(CategoryDto object) {
        return object.getIsMissing();
      }
    };
    addColumn(missings, translations.missingLabel());
    setColumnWidth(missings,"100px");
  }
}
