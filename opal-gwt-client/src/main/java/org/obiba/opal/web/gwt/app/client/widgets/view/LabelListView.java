/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import java.util.HashMap;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

public class LabelListView extends Composite implements LabelListPresenter.Display {

  private static final Translations translations = GWT.create(Translations.class);

  private final FlowPanel panel;

  private LocaleDto baseLanguage;

  private final Map<String, TextBoxBase> languageLabelMap = new HashMap<String, TextBoxBase>();

  private boolean useTextArea;

  public LabelListView() {
    panel = new FlowPanel();
    initWidget(panel);
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setUseTextArea(boolean useTextArea) {
    this.useTextArea = useTextArea;
  }

  @SuppressWarnings({"PMD.NcssMethodCount", "OverlyLongMethod"})
  @Override
  public void setLanguages(JsArray<LocaleDto> languages) {
    int nbLanguages = languages.length();
    if(nbLanguages > 0) {
      baseLanguage = languages.get(0);
    }
    panel.clear();
    languageLabelMap.clear();

    Grid grid = new Grid(useTextArea ? nbLanguages * 2 : nbLanguages, useTextArea ? 1 : 2);
    grid.addStyleName("full-width");
    int row = 0;
    for(LocaleDto localeDto : JsArrays.toList(languages)) {
      String localeName = localeDto.getName();
      Label label = makeLabel(localeName);

      TextBoxBase box = useTextArea ? new TextArea() : new TextBox();
      box.addStyleName(useTextArea ? "full-width" : "not-so-full-width");
      languageLabelMap.put(localeName, box);

      if(useTextArea) {
        label.addStyleName("full-width");
        grid.setWidget(row++, 0, label);
        grid.setWidget(row++, 0, box);
      } else {
        grid.setWidget(row, 0, label);
        grid.setWidget(row++, 1, box);
      }

    }
    panel.add(grid);
  }

  private Label makeLabel(String language) {
    Label label;
    if("".equals(language)) {
      label = new Label(translations.noLocale());
    } else {
      label = new InlineLabel(language);
      label.addStyleName("label");
    }
    return label;
  }

  @Override
  public Map<String, TextBoxBase> getLanguageLabelMap() {
    return languageLabelMap;
  }

  @Override
  public LocaleDto getBaseLanguage() {
    return baseLanguage;
  }

  @Override
  public void displayAttributes(String namespace, String name, JsArray<AttributeDto> attributes) {
    String safeNamespace = Strings.nullToEmpty(namespace);
    JsArray<AttributeDto> nonNullAttributes = JsArrays.toSafeArray(attributes);
    for(int i = 0; i < nonNullAttributes.length(); i++) {
      AttributeDto dto = nonNullAttributes.get(i);
      if(Objects.equal(safeNamespace, dto.getNamespace()) && Objects.equal(name, dto.getName()) && languageLabelMap
          .containsKey(dto.getLocale())) {
        TextBoxBase textBox = languageLabelMap.get(dto.getLocale());
        textBox.setValue(dto.getValue());
      }
    }
  }

  @Override
  public void clearAttributes() {
    for(TextBoxBase textBox : languageLabelMap.values()) {
      textBox.setText("");
    }
  }

}
