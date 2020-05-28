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

import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersMappingModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersMappingModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.AttributeHelper;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class IdentifiersMappingModalView extends ModalPopupViewWithUiHandlers<IdentifiersMappingModalUiHandlers>
    implements IdentifiersMappingModalPresenter.Display {

  private final Translations translations;

  interface Binder extends UiBinder<Widget, IdentifiersMappingModalView> {}

  @UiField
  Modal dialog;

  @UiField
  ControlGroup variableGroup;

  @UiField
  TextBox variableName;

  @UiField
  TextBox description;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @Inject
  public IdentifiersMappingModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.editIdentifiersMapping());
    dialog.setResizable(true);
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave();
  }

  @Override
  public void renderProperties(VariableDto variable, boolean modifyName) {
    if(variable != null) {
      variableName.setText(variable.getName());
      description.setText(AttributeHelper.getAttributeValue(variable.getAttributesArray(), "description"));
    } else {
      dialog.setTitle(translations.addIdentifiersMapping());
    }

    variableName.setEnabled(modifyName);
  }

  @Override
  public void showError(String message, @Nullable FormField group) {
    if(Strings.isNullOrEmpty(message)) return;

    String msg = message;
    try {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(message);
      msg = errorDto.getStatus();
    } catch(Exception ignored) {
    }
    if(group == null) {
      dialog.addAlert(msg, AlertType.ERROR);
    } else {
      dialog.addAlert(msg, AlertType.ERROR, variableGroup);
    }
  }

  @Override
  public HasText getVariableName() {
    return variableName;
  }

  @Override
  public String getName() {
    return variableName.getText();
  }

  @Override
  public String getDescription() {
    return description.getText();
  }
}
