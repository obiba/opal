/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.configuration.view;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.GeneralConfModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.GeneralConfModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.CharacterSetView;
import org.obiba.opal.web.gwt.app.client.ui.LocaleChooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class GeneralConfModalView extends ModalPopupViewWithUiHandlers<GeneralConfModalUiHandlers>
    implements GeneralConfModalPresenter.Display {

  private final Translations translations;

  @UiTemplate("GeneralConfModalView.ui.xml")
  interface Binder extends UiBinder<Widget, GeneralConfModalView> {}

  @UiField
  Modal modal;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox name;

  @UiField(provided = true)
  final LocaleChooser locales;

  @UiField
  CharacterSetView characterSet;

//  @UiField
//  FlowPanel taxonomiesPanel;

  @Inject
  public GeneralConfModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;

    locales = new LocaleChooser(true);

    initWidget(uiBinder.createAndBindUi(this));

    modal.setTitle(translations.generalSettings());
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    modal.hide();
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasText getDefaultCharSet() {
    return characterSet.getCharsetText();
  }

  @Override
  public JsArrayString getLanguages() {
    JsArrayString languages = JsArrayString.createArray().cast();
    for(String locale : locales.getSelectedLocales()) {
      languages.push(locale);
    }

    return languages;
  }

  @Override
  public void setSelectedCharset(String charset) {
    characterSet.setDefaultCharset(charset);
  }

  @Override
  public void setSelectedLanguages(JsArrayString languages) {
    if(languages.length() > 0) {
      for(int i = 0; i < languages.length(); i++) {
        locales.setSelectedValue(languages.get(i));
      }
    }
  }
}