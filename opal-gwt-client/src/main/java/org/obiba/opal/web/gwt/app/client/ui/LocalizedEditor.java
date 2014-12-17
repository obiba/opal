/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavTabs;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Allows edition of one text per language.
 */
public class LocalizedEditor extends FlowPanel {

  private static final Translations translations = GWT.create(Translations.class);

  private final NavTabs localeTabs;

  private MarkdownEditor editor;

  private NavLink currentLocale;

  private Map<String, String> localizedTexts;

  public LocalizedEditor() {
    localeTabs = new NavTabs();
    add(localeTabs);
    editor = new MarkdownEditor();
    add(editor);
  }

  public void setLocalizedTexts(Map<String, String> localizedTexts, List<String> locales) {
    this.localizedTexts = localizedTexts;

    localeTabs.clear();
    NavLink first = null;
    for (String locale : locales) {
      String lang = Strings.isNullOrEmpty(locale) ? translations.defaultLabel() : locale;
      NavLink link = new NavLink(lang);
      link.setName(locale);

      if (first == null) {
        first = link;
        editor.setText(localizedTexts.get(locale));
      }

      localeTabs.add(link);
      link.addClickHandler(new LocaleTabClickHandler(link));
    }

    if (first != null) {
      first.setActive(true);
      currentLocale = first;
    }
  }

  public Map<String, String> getLocalizedTexts() {
    // make sure current edition is applied
    localizedTexts.put(currentLocale.getName(), editor.getText());
    return localizedTexts;
  }

  private class LocaleTabClickHandler implements ClickHandler {

    private final NavLink link;

    public LocaleTabClickHandler(NavLink link) {
      this.link = link;
    }

    @Override
    public void onClick(ClickEvent event) {
      localizedTexts.put(currentLocale.getName(), editor.getText());
      currentLocale.setActive(false);
      link.setActive(true);
      currentLocale = link;
      editor.showPreview(false);
      editor.setText(localizedTexts.get(currentLocale.getName()));
    }
  }

}
