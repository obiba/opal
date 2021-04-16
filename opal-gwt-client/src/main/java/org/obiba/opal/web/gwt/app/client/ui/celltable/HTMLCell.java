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

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;

public class HTMLCell extends TextCell {

  public HTMLCell() {
    super(new SafeHtmlRenderer<String>() {

      @Override
      public SafeHtml render(String object) {
        return object == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromTrustedString(object);
      }

      @Override
      public void render(String object, SafeHtmlBuilder appendable) {
        appendable.append(SafeHtmlUtils.fromTrustedString(object));
      }
    });
  }

}
