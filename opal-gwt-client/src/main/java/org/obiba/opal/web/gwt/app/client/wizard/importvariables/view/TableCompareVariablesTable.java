/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.VariableAttributeColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

/**
 *
 */
public class TableCompareVariablesTable extends Table<VariableDto> {

  private static Translations translations = GWT.create(Translations.class);

  private Column<VariableDto, String> variableNameColumn;

  public TableCompareVariablesTable() {
    super();
    initColumns();
  }

  public Column<VariableDto, String> getVariableNameColumn() {
    return variableNameColumn;
  }

  private void initColumns() {

    addColumn(variableNameColumn = new ClickableColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto variable) {
        return variable.getName();
      }
    }, translations.nameLabel());

    addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto variable) {
        return variable.getValueType();
      }
    }, translations.valueTypeLabel());

    addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto variable) {
        return variable.getUnit();
      }
    }, translations.unitLabel());

    addColumn(new VariableAttributeColumn("label"), translations.labelLabel());
  }

}
