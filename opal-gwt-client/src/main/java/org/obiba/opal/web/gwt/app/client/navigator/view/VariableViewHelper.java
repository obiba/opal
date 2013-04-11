/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.LocaleInfo;

/**
 *
 */
public class VariableViewHelper {

  private static final String DEFAULT_LOCALE_NAME = "default";

  private static final String LABEL_ATTRIBUTE_NAME = "label";

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

    return attribute != null ? attribute.getValue() : "";
  }

  /**
   * Get current language of the application.
   *
   * @return
   */
  public static String getCurrentLanguage() {
    String currentLocaleName = LocaleInfo.getCurrentLocale().getLocaleName();
    if(currentLocaleName.equals(DEFAULT_LOCALE_NAME)) {
      // No locale has been specified so the current locale is "default". Return English as the current language.
      return "en";
    }
    int separatorIndex = currentLocaleName.indexOf('_');

    return separatorIndex != -1 ? currentLocaleName.substring(0, separatorIndex) : currentLocaleName;
  }

}
