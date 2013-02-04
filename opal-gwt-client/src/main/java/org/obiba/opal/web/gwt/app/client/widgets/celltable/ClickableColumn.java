/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;

public abstract class ClickableColumn<T> extends Column<T, String> implements HasFieldUpdater<T, String> {

  public ClickableColumn() {
    super(cell);
  }

  @Override
  public FieldUpdater<T, String> getFieldUpdater() {
    return super.getFieldUpdater();
  }

  @Override
  public void setFieldUpdater(FieldUpdater<T, String> fieldUpdater) {
    super.setFieldUpdater(fieldUpdater);
  }

  private static final Cell<String> cell = new ClickableTextCell() {
    @Override
    protected void render(Cell.Context context, SafeHtml value, SafeHtmlBuilder sb) {
      if(value != null) {
        sb.appendHtmlConstant("<a>").append(value).appendHtmlConstant("</a>");
      }
    }
  };

}
