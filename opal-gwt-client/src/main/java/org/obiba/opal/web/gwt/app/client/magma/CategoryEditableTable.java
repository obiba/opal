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

import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.model.client.magma.CategoryDto;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.ProvidesKey;

/**
 *
 */
public class CategoryEditableTable extends Table<CategoryDto> {

  @SuppressWarnings("UnusedDeclaration")
  public CategoryEditableTable() {
    this(DEFAULT_PAGESIZE);
  }

  public CategoryEditableTable(int pageSize) {
    super(pageSize, new ProvidesKey<CategoryDto>() {
      @Override
      public Object getKey(CategoryDto item) {
        return item.getName();
      }
    });
    Image loading = new Image("image/loading.gif");
    setLoadingIndicator(loading);
  }
}
