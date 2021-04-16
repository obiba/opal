/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public abstract class EditableTabableColumn<T> extends EditableColumn<T> {

  public EditableTabableColumn() {
    super(new TabableTextInputCell());
  }

  public static class TabableTextInputCell extends TextInputCell {

    interface Template extends SafeHtmlTemplates {
      @Template("<input type=\"text\" value=\"{0}\"></input>")
      SafeHtml input(String value);
    }

    private final Template template = GWT.create(Template.class);

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
      // Get the view data.
      Object key = context.getKey();
      ViewData viewData = getViewData(key);
      if(viewData != null && viewData.getCurrentValue().equals(value)) {
        clearViewData(key);
        viewData = null;
      }

      String s = viewData != null ? viewData.getCurrentValue() : value;
      if(s != null) {
        sb.append(template.input(s));
      } else {
        sb.appendHtmlConstant("<input type=\"text\"></input>");
      }
    }
  }
}
