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
import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LabelListView extends Composite implements LabelListPresenter.Display {

  private FlowPanel panel;

  private Grid grid;

  private LocaleDto baseLanguage;

  private Map<LocaleDto, TextBox> languageLabelMap;

  private Translations translations = GWT.create(Translations.class);

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
  public void setLanguages(JsArray<LocaleDto> languages) {
    languageLabelMap = new HashMap<LocaleDto, TextBox>();
    grid = new Grid(languages.length(), 2);
    for(int i = 0; i < languages.length(); i++) {
      if(i == 0) baseLanguage = languages.get(0);
      GWT.log(languages.get(i).getName());
      grid.setWidget(i, 0, new Label(getLabelText(languages.get(i).getName(), i)));
      languageLabelMap.put(languages.get(i), new TextBox());
      grid.setWidget(i, 1, languageLabelMap.get(languages.get(i)));
    }
    panel.add(grid);
  }

  private String getLabelText(String language, int index) {
    StringBuilder sb = new StringBuilder();
    sb.append(translations.labelLabel()).append(" (");
    sb.append(language).append(")");
    if(index == 0) sb.append("*");
    sb.append(":");
    return sb.toString();
  }

  @Override
  public Map<LocaleDto, TextBox> getLanguageLabelMap() {
    return languageLabelMap;
  }

  @Override
  public LocaleDto getBaseLanguage() {
    return baseLanguage;
  }

}
