/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.identifiers.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.ImportIdentifiersMappingModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.ImportIdentifiersMappingModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ImportIdentifiersMappingModalView
    extends ModalPopupViewWithUiHandlers<ImportIdentifiersMappingModalUiHandlers>
    implements ImportIdentifiersMappingModalPresenter.Display {

  interface Binder extends UiBinder<Widget, ImportIdentifiersMappingModalView> {}

  private final Translations translations;

  @UiField
  Modal dialog;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @UiField
  ControlGroup variableGroup;

  @UiField
  Typeahead variableTypeahead;

  @UiField
  TextBox variableName;

  @UiField
  ControlGroup systemIdsGroup;

  @UiField
  ControlGroup idsGroup;

  @UiField
  TextArea systemIdentifiers;

  @UiField
  TextArea identifiers;

  @Inject
  public ImportIdentifiersMappingModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.importIdentifiersMappingTitle());
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    getUiHandlers().onSubmit(variableName.getText(), getSystemIdentifiers().getText(), getIdentifiers().getText());
  }

  @Override
  public void showError(String message, @Nullable FormField group) {
    if(Strings.isNullOrEmpty(message)) return;

    String msg = translateErrorMessage(message);

    if(group == null) {
      dialog.addAlert(msg, AlertType.ERROR);
    } else if(group == FormField.NAME) {
      dialog.addAlert(msg, AlertType.ERROR, variableGroup);
    } else if(group == FormField.IDENTIFIERS) {
      dialog.addAlert(msg, AlertType.ERROR, idsGroup);
    } else dialog.addAlert(msg, AlertType.ERROR, systemIdsGroup);
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables) {
    MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) variableTypeahead.getSuggestOracle();
    oracle.clear();
    for(VariableDto var : JsArrays.toIterable(variables)) {
      oracle.add(var.getName());
    }
  }

  @Override
  public void setBusy(boolean busy) {
    if(busy) {
      dialog.setBusy(busy);
      dialog.setCloseVisible(false);
      saveButton.setEnabled(false);
      closeButton.setEnabled(false);
    } else {
      dialog.setBusy(busy);
      dialog.setCloseVisible(true);
      saveButton.setEnabled(true);
      closeButton.setEnabled(true);
    }
  }

  @Override
  public HasText getSystemIdentifiers() {
    return systemIdentifiers;
  }

  @Override
  public HasText getIdentifiers() {
    return identifiers;
  }

  @Override
  public HasText getVariableName() {
    return variableName;
  }

  //
  // Private methods
  //

  private String translateErrorMessage(String message) {
    String msg = message;
    try {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(message);
      msg = errorDto.getStatus();
      if(translations.userMessageMap().containsKey(msg)) msg = translations.userMessageMap().get(errorDto.getStatus());
    } catch(Exception ignored) {
      if(translations.userMessageMap().containsKey(message)) msg = translations.userMessageMap().get(message);
    }

    return msg;
  }
}
