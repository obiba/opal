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

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;

/**
 * A table for handling a list of table references.
 */
public class TableReferencesTable extends Table<TableDto> {

  @SuppressWarnings("UnusedDeclaration")
  public TableReferencesTable() {
    this(DEFAULT_PAGESIZE);
  }

  public TableReferencesTable(int pageSize) {
    super(pageSize, new ProvidesKey<TableDto>() {
      @Override
      public Object getKey(TableDto item) {
        return item.getDatasourceName() + "." + item.getName();
      }
    });
    Image loading = new Image("image/loading.gif");
    setLoadingIndicator(loading);
  }



}
