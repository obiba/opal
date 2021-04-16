/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.configuration.edit;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.CharacterSetView;
import org.obiba.opal.web.gwt.app.client.ui.LocaleChooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
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

  @UiField
  LocaleChooser locales;

  @UiField
  CharacterSetView characterSet;

  @UiField
  ControlGroup nameGroup;

  @UiField
  ControlGroup languagesGroup;

  @UiField
  ControlGroup defaultCharsetGroup;

  @UiField
  TextBox publicUrl;

  @Inject
  public GeneralConfModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.editProperties());
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
  public HasText getPublicUrl() {
    return publicUrl;
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
    int length = languages.length();
    for(int i = 0; i < length; i++) {
      locales.setSelectedValue(languages.get(i));
    }
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

  @Override
  public void showError(@Nullable GeneralConfModalPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAME:
          group = nameGroup;
          break;
        case DEFAULT_CHARSET:
          group = defaultCharsetGroup;
          break;
        case LANGUAGES:
          group = languagesGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }
}