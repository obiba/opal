/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.AttributeDtos;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;

import java.util.List;

public abstract class AttributeColumn<T> extends Column<T, String> {

  protected final String attributeNamespace;

  protected final String attributeName;

  public AttributeColumn(String attributeName) {
    this(null, attributeName);
  }

  public AttributeColumn(String attributeNamespace, String attributeName) {
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
    this.attributeNamespace = attributeNamespace;
    this.attributeName = attributeName;
  }

  protected abstract JsArray<AttributeDto> getAttributes(T object);

  public String getAttributeNamespace(T object) {
    return attributeNamespace;
  }

  protected String getAttributeName(T object) {
    return attributeName;
  }

  protected boolean isMarkdown() {
    return false;
  }

  @Override
  public String getValue(T object) {
    return getLabels(object);
  }

  private String getLabels(T object) {
    JsArray<AttributeDto> attributes = JsArrays.toSafeArray(getAttributes(object));
    AttributeDto attribute;
    StringBuilder labels = new StringBuilder();

    String namespace = getAttributeNamespace(object);
    String name = getAttributeName(object);
    for(int i = 0; i < attributes.length(); i++) {
      attribute = attributes.get(i);
      if(isForAttribute(namespace, name, attribute)) {
        appendLabel(attribute, labels);
      }
    }

    return labels.toString();
  }

  private boolean isForAttribute(String namespace, String name, AttributeDto attribute) {
    if (Strings.isNullOrEmpty(namespace) && (attribute.hasNamespace() && !Strings.isNullOrEmpty(attribute.getNamespace())))
      return false;
    if ("*".equals(namespace)) {
      if (!attribute.hasNamespace()) return false;
    }
    else if (!Strings.isNullOrEmpty(namespace) && (!attribute.hasNamespace() || !namespace.equals(attribute.getNamespace())))
      return false;
    if ("*".equals(name)) return true; // any attribute, most likely filtered by namespace
    return attribute.getName().equals(name);
  }

  protected void appendLabel(AttributeDto attr, StringBuilder labels) {
    if(attr.hasValue() && attr.getValue().trim().length() > 0) {
      labels.append("<div class=\"attribute-value\">");
      if (isMarkdown()) {
        if (attr.hasLocale() && attr.getLocale().trim().length() > 0) {
          labels.append("<span class=\"label\">").append(attr.getLocale()).append("</span> ");
        }
        String value = attr.getValue();
        labels.append(Markdown.parse(value));
      } else {
        if (AttributeDtos.SCRIPT_ATTRIBUTE.equals(attr.getName())) {
          labels.append("<pre>");
        }
        if (attr.hasLocale() && attr.getLocale().trim().length() > 0) {
          labels.append("<span class=\"label\">").append(attr.getLocale()).append("</span> ");
        }
        String value = attr.getValue();
        String safeValue = SafeHtmlUtils.fromString(value).asString().replaceAll("\\n", "<br/>");
        try {
          List<String> valueStrs = Splitter.on(" ").splitToList(safeValue);
          for (int i = 0; i < valueStrs.size(); i++) {
            if (i > 0) labels.append(" ");
            String valueStr = valueStrs.get(i);
            if (UriUtils.extractScheme(valueStr) != null && UriUtils.isSafeUri(valueStr)) {
              labels.append("<a href=").append(valueStr).append(" target=\"_blank\">").append(valueStr).append("</a>");
            } else {
              labels.append(valueStr);
            }
          }
        } catch (Exception e) {
          labels.append(safeValue);
        }
        if (AttributeDtos.SCRIPT_ATTRIBUTE.equals(attr.getName())) {
          labels.append("</pre>");
        }
      }
      labels.append("</div>");
    }
  }
}