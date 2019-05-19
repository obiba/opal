/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.LanguageLocale;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.uibinder.client.UiConstructor;

/**
 * Selector of locales.
 */
public class LocaleChooser extends Chooser {

  private static final Translations translations = GWT.create(Translations.class);

  private final Map<String, String> localeMap = new HashMap<String, String>();

  @UiConstructor
  public LocaleChooser(boolean isMultipleSelect) {
    super(isMultipleSelect);
    initWidget();
  }

  private void initWidget() {
    setPlaceholderText(translations.selectLanguages());

    int i = 0;
    for(String locale : LanguageLocale.getAllLocales()) {
      insertItem(LanguageLocale.getDisplayName(locale), locale, i++);
      localeMap.put(locale, locale);
    }

    // TODO: Sort by display name
  }

  @Override
  public void clear() {
    super.clear();
    localeMap.clear();
  }

  public void selectLocales(JsArrayString locales) {
    for(String l : JsArrays.toIterable(locales)) {
      for(int i = 0; i < getItemCount(); i++) {
        if(getValue(i).equals(l)) {
          setItemSelected(i, true);
          break;
        }
      }
    }
  }

  public List<String> getSelectedLocales() {
    List<String> locales = new ArrayList<String>();
    for(int i = 0; i < getItemCount(); i++) {
      if(isItemSelected(i)) {
        locales.add(localeMap.get(getValue(i)));
      }
    }
    return locales;
  }
}
