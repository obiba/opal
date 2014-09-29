/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.celltable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;

public abstract class LocaleTextColumn<T> extends Column<T, String> {

  public LocaleTextColumn() {
    super(new TextCell(new SafeHtmlRenderer<String>() {

      @Override
      public SafeHtml render(String object) {
        return object == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromTrustedString(object);
      }

      @Override
      public void render(String object, SafeHtmlBuilder appendable) {
        appendable.append(SafeHtmlUtils.fromTrustedString(object));
      }
    }));
  }

  @Override
  public String getValue(T object) {
    return getLabels(object);
  }

  protected abstract JsArray<LocaleTextDto> getLocaleText(T term);

  private String getLabels(T object) {
    JsArray<LocaleTextDto> texts = JsArrays.toSafeArray(getLocaleText(object));
    LocaleTextDto text;
    StringBuilder labels = new StringBuilder();

    for(int i = 0; i < texts.length(); i++) {
      text = texts.get(i);
      appendLabel(text, labels);
    }

    return labels.toString();
  }

  private void appendLabel(LocaleTextDto attr, StringBuilder labels) {
    labels.append("<div class=\"attribute-value\">");
    if(attr.hasLocale() && attr.getLocale().trim().length() > 0) {
      labels.append("<span class=\"label\">").append(attr.getLocale()).append("</span> ");
    }
    String value = attr.getText();
    String safeValue = SafeHtmlUtils.fromString(value).asString().replaceAll("\\n", "<br />");
    try {
      if(UriUtils.extractScheme(value) != null && UriUtils.isSafeUri(value)) {
        labels.append("<a href=").append(value).append(" target=\"_blank\">").append(safeValue).append("</a>");
      } else {
        labels.append(safeValue);
      }
    } catch(Exception e) {
      labels.append(safeValue);
    }
    labels.append("</div>");
  }
}
