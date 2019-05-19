/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.edit;

import javax.annotation.Nullable;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditor;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

public class VocabularyEditModalView extends ModalPopupViewWithUiHandlers<VocabularyEditModalUiHandlers>
    implements VocabularyEditModalPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, VocabularyEditModalView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal modal;

  @UiField
  TextBox name;

  @UiField
  LocalizedEditor titles;

  @UiField
  LocalizedEditor descriptions;

  @UiField
  CheckBox repeatable;

  @Inject
  public VocabularyEditModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.addTaxonomy());
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public void setMode(VocabularyEditModalPresenter.EDIT_MODE editionMode) {
    modal.setTitle(editionMode == VocabularyEditModalPresenter.EDIT_MODE.CREATE
        ? translations.addVocabulary()
        : translations.editVocabulary());
  }

  @Override
  public void setVocabulary(VocabularyDto vocabulary, JsArrayString locales) {
    name.setText(vocabulary.getName());
    titles.setLocaleTexts(vocabulary.getTitleArray(), JsArrays.toList(locales));
    descriptions.setLocaleTexts(vocabulary.getDescriptionArray(), JsArrays.toList(locales));
    repeatable.setValue(vocabulary.hasRepeatable() && vocabulary.getRepeatable());
  }

  @UiHandler("save")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave(name.getText(), repeatable.getValue(), titles.getLocaleTexts(), descriptions.getLocaleTexts());
  }

  @UiHandler("cancel")
  void onCancel(ClickEvent event) {
    modal.hide();
  }


  @Override
  public void showError(@Nullable FormField formField, String message) {
    modal.addAlert(message, AlertType.ERROR);
  }

}
