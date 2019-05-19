/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.support;

import javax.validation.constraints.NotNull;

import com.google.common.base.*;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Label;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.LocaleInfo;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;

import java.util.Map;

/**
 *
 */
public class AttributeHelper {

  private static final String DEFAULT_LOCALE_NAME = "default";

  private static final String LABEL_ATTRIBUTE_NAME = "label";

  private AttributeHelper() {}

  /**
   * Get the 'label' attribute value in current language.
   *
   * @param attributes
   * @return
   * @see #getAttributeValue(JsArray, String)
   */
  public static String getLabelValue(JsArray<AttributeDto> attributes) {
    return getAttributeValue(attributes, LABEL_ATTRIBUTE_NAME);
  }

  /**
   * Get the attribute value for the current language. If no attribute is defined for this language, get the value from
   * the attribute with no locale defined.
   *
   * @param attributes
   * @param name the attribute name
   * @return empty string if attribute was not found
   */
  @NotNull
  public static String getAttributeValue(JsArray<AttributeDto> attributes, String name) {
    AttributeDto attribute = null;

    JsArray<AttributeDto> notNull = JsArrays.toSafeArray(attributes);
    String currentLanguage = getCurrentLanguage();
    for(int i = 0; i < notNull.length(); i++) {
      AttributeDto att = notNull.get(i);
      if(att.getName().equals(name)) {
        if(!att.hasLocale()) {
          attribute = att;
        } else if(att.getLocale().equals(currentLanguage)) {
          attribute = att;
          break;
        }
      }
    }

    return attribute == null ? "" : attribute.getValue();
  }

  /**
   * Get current language of the application.
   *
   * @return
   */
  public static String getCurrentLanguage() {
    String currentLocaleName = LocaleInfo.getCurrentLocale().getLocaleName();
    if(DEFAULT_LOCALE_NAME.equals(currentLocaleName)) {
      // No locale has been specified so the current locale is "default". Return English as the current language.
      return "en";
    }
    int separatorIndex = currentLocaleName.indexOf('_');
    return separatorIndex == -1 ? currentLocaleName : currentLocaleName.substring(0, separatorIndex);
  }

  public static String getLocaleText(JsArray<LocaleTextDto> texts) {
    String currentLanguage = getCurrentLanguage();
    for (LocaleTextDto text : JsArrays.toIterable(texts)) {
      if (currentLanguage.equals(text.getLocale())) return text.getText();
    }
    // fallback in english
    for (LocaleTextDto text : JsArrays.toIterable(texts)) {
      if ("en".equals(text.getLocale())) return text.getText();
    }
    return "";
  }

  public static String getLabelsAsString(JsArray<AttributeDto> attributes) {
    // find labels
    Map<String, String> labelsMap = Maps.newHashMap();
    for (AttributeDto attribute : JsArrays.toIterable(attributes)) {
      if (attribute.getName().equals("label")) {
        if (attribute.hasLocale())
          labelsMap.put(attribute.getLocale(), attribute.getValue());
        else
          labelsMap.put("", attribute.getValue());
      }
    }
    String labels = "";
    if (labelsMap.containsKey(""))
      labels = labelsMap.get("");
    for (String key : labelsMap.keySet()) {
      if (!com.google.common.base.Strings.isNullOrEmpty(key)) {
        labels = (com.google.common.base.Strings.isNullOrEmpty(labels) ? "" : labels + " ") + "(" + key + ") " + labelsMap.get(key);
      }
    }
    return labels;
  }

}
