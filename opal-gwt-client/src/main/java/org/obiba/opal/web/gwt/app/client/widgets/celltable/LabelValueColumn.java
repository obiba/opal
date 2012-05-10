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

import com.google.common.base.Strings;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;

/**
 *
 */
public abstract class LabelValueColumn<T> extends Column<T, String> {

  private String css;

  public LabelValueColumn() {
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
  public String getValue(T t) {
    StringBuilder label = new StringBuilder(getCss() == null ? "<div>" : "<div class=\"" + getCss() + "\">");
    if(!Strings.isNullOrEmpty(getLabel(t))) {
      label.append("<span class=\"label\">").append(getLabel(t)).append("</span> ");
    }
    label.append(getContent(t));
    label.append("</div>");
    return label.toString();
  }

  public abstract String getLabel(T t);

  public abstract String getContent(T t);

  public String getCss() {
    return css;
  }

  public void setCss(String css) {
    this.css = css;
  }
}