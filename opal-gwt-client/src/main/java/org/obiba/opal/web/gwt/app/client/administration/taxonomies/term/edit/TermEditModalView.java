/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.term.edit;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditor;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.TermDto;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TermEditModalView extends ModalPopupViewWithUiHandlers<TermEditModalUiHandlers>
    implements TermEditModalPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, TermEditModalView> {}

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

  @Inject
  public TermEditModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.addTaxonomy());
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @Override
  public void setMode(TermEditModalPresenter.EDIT_MODE editionMode) {
    modal.setTitle(editionMode == TermEditModalPresenter.EDIT_MODE.CREATE
        ? translations.addTerm()
        : translations.editTerm());
  }

  @Override
  public void setTerm(TermDto term, JsArrayString locales) {
    name.setText(term.getName());
    titles.setLocaleTexts(term.getTitleArray(), JsArrays.toList(locales));
    descriptions.setLocaleTexts(term.getDescriptionArray(), JsArrays.toList(locales));
  }

  @UiHandler("save")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave(name.getText(), titles.getLocaleTexts(), descriptions.getLocaleTexts());
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
