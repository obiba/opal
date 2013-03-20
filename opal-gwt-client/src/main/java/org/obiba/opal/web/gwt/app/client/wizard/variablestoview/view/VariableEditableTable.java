/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.variablestoview.view;

import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.ProvidesKey;

/**
 *
 */
public class VariableEditableTable extends Table<VariableDto> {

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

    setStyleName(resources.cellTableStyle().cellTableWidget());
    addStyleName("obiba-Table");
    Image loading = new Image("image/loading.gif");
    loading.addStyleName("loading");
    setLoadingIndicator(loading);
  }
}
