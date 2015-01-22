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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavTabs;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlternateSize;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows edition of one text per language.
 */
public class LocalizedEditor extends FlowPanel {

  public enum Type {
    PLAIN_SHORT,
    PLAIN_LONG,
    MARKDOWN
  }

  private static final Translations translations = GWT.create(Translations.class);

  private Type type;

  private final NavTabs localeTabs;

  private HasText editor;

  private NavLink currentLocale;

  private Map<String, String> localizedTexts;

  public LocalizedEditor() {
    localeTabs = new NavTabs();
    add(localeTabs);
  }

  public void setType(Type type) {
    this.type = type;
    switch(type) {
      case PLAIN_SHORT:
        editor = new TextBox();
        break;
      case PLAIN_LONG:
        TextArea area = new TextArea();
        area.setVisibleLines(5);
        area.setAlternateSize(AlternateSize.XXLARGE);
        editor = area;
        break;
      case MARKDOWN:
        editor = new MarkdownEditor();
        break;
    }
    add((Widget) editor);
  }

  public void setLocaleTexts(JsArray<LocaleTextDto> texts, List<String> locales) {
    setLocalizedTexts(toMap(texts), locales);
  }

  public void setLocalizedTexts(Map<String, String> localizedTexts, List<String> locales) {
    this.localizedTexts = localizedTexts;

    localeTabs.clear();
    NavLink first = null;
    for(String locale : locales) {
      String lang = Strings.isNullOrEmpty(locale) ? translations.defaultLabel() : locale;
      NavLink link = new NavLink(lang);
      link.setName(locale);

      if(first == null) {
        first = link;
        editor.setText(localizedTexts.get(locale));
      }

      localeTabs.add(link);
      link.addClickHandler(new LocaleTabClickHandler(link));
    }

    if(first != null) {
      first.setActive(true);
      currentLocale = first;
    }
  }

  public JsArray<LocaleTextDto> getLocaleTexts() {
    return fromMap(getLocalizedTexts());
  }

  public Map<String, String> getLocalizedTexts() {
    // make sure current edition is applied
    if(localizedTexts != null) localizedTexts.put(currentLocale.getName(), editor.getText());
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
      if(type == Type.MARKDOWN) ((MarkdownEditor) editor).showPreview(false);
      editor.setText(localizedTexts.get(currentLocale.getName()));
    }
  }

  private Map<String, String> toMap(JsArray<LocaleTextDto> texts) {
    Map<String, String> textMap = new HashMap<String, String>();

    for(LocaleTextDto text : JsArrays.toIterable(texts)) {
      textMap.put(text.getLocale(), text.getText());
    }

    return textMap;
  }

  private JsArray<LocaleTextDto> fromMap(Map<String, String> textMap) {
    JsArray<LocaleTextDto> texts = JsArrays.create();

    for(Map.Entry<String, String> entry : textMap.entrySet()) {
      if(!Strings.isNullOrEmpty(entry.getValue())) {
        LocaleTextDto dto = LocaleTextDto.create();
        dto.setLocale(entry.getKey());
        dto.setText(entry.getValue());
        texts.push(dto);
      }
    }

    return texts;
  }

}
