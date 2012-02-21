/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;

public abstract class AttributeColumn<T> extends Column<T, String> {

  private String attributeName;

  public AttributeColumn(String attributeName) {
    super(new TextCell(new SafeHtmlRenderer<String>() {

      @Override
      public SafeHtml render(String object) {
        return (object == null) ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromTrustedString(object);
      }

      @Override
      public void render(String object, SafeHtmlBuilder appendable) {
        appendable.append(SafeHtmlUtils.fromTrustedString(object));
      }
    }));
    this.attributeName = attributeName;
  }

  protected abstract JsArray<AttributeDto> getAttributes(T object);

  @Override
  public String getValue(T object) {
    return getLabels(object);
  }

  private String getLabels(T object) {
    JsArray<AttributeDto> attributes = JsArrays.toSafeArray(getAttributes(object));
    AttributeDto attribute = null;
    StringBuilder labels = new StringBuilder();

    for(int i = 0; i < attributes.length(); i++) {
      attribute = attributes.get(i);
      if(attribute.getName().equals(attributeName)) {
        appendLabel(attribute, labels);
      }
    }

    return labels.toString();
  }

  private void appendLabel(AttributeDto attr, StringBuilder labels) {
    if(attr.hasValue() && attr.getValue().trim().length() > 0) {
      labels.append("<div>");
      if(attr.hasLocale() && attr.getLocale().trim().length() > 0) {
        labels.append("<span class=\"label\">").append(attr.getLocale()).append("</span> ");
      }
      labels.append(SafeHtmlUtils.fromString(attr.getValue()).asString()).append("</div>");
    }
  }
}