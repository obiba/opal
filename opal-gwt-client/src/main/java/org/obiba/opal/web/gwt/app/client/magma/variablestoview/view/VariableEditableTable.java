/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variablestoview.view;

import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.user.cellview.client.TextColumn;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsVariableCopyColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.EditableColumn;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.ProvidesKey;

/**
 *
 */
public class VariableEditableTable extends Table<VariableDto> {

  private static final int SCRIPT_MAX_LENGTH = 100;

  private final TextInputCell cell = new TextInputCell();

  private ActionsVariableCopyColumn<VariableDto> actionsColumn;

  @SuppressWarnings("UnusedDeclaration")
  public VariableEditableTable() {
    this(DEFAULT_PAGESIZE);
  }

  public VariableEditableTable(int pageSize) {
    super(pageSize, new ProvidesKey<VariableDto>() {
      @Override
      public Object getKey(VariableDto item) {
        return item.getName();
      }
    });
    Image loading = new Image("image/loading.gif");
    setLoadingIndicator(loading);
  }

  public void initialize(ActionHandler<VariableDto> actionHandler) {
    while(getColumnCount()>0) {
      removeColumn(0);
    }
    EditableColumn<VariableDto> editColumn = new EditableColumn<VariableDto>(cell) {
      @Override
      public String getValue(VariableDto object) {
        return object.getName();
      }
    };

    addColumn(editColumn, translations.nameLabel());
    addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto object) {
        return getTruncatedScript(object);
      }

      private String getTruncatedScript(VariableDto object) {
        String script = "";
        for(int i = 0; i < JsArrays.toSafeArray(object.getAttributesArray()).length(); i++) {
          if("script".equals(object.getAttributesArray().get(i).getName())) {
            script = object.getAttributesArray().get(i).getValue();
            if(script.length() > SCRIPT_MAX_LENGTH) {
              script = script.substring(0, Math.min(script.length(), SCRIPT_MAX_LENGTH)) + " ...";
            }
            break;
          }
        }
        return script;
      }
    }, translations.scriptLabel());

    actionsColumn = new ActionsVariableCopyColumn<VariableDto>(
        new ConstantActionsProvider<VariableDto>(ActionsVariableCopyColumn.REMOVE_ACTION));
    actionsColumn.setActionHandler(actionHandler);
    addColumn(actionsColumn, translations.actionsLabel());
  }

  public TextInputCell.ViewData getViewData(String name) {
    return cell.getViewData(name);
  }

  public void clearViewData(String name) {
    cell.clearViewData(name);
  }
}
