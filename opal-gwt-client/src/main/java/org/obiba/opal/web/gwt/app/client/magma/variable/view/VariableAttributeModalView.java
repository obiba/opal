/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.BaseVariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditor;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalPresenter.Display;

/**
 *
 */
public class VariableAttributeModalView extends ModalPopupViewWithUiHandlers<VariableAttributeModalUiHandlers>
    implements Display {

  private final Translations translations;

  interface Binder extends UiBinder<Widget, VariableAttributeModalView> {}

  @UiField
  Modal modal;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  ControlGroup nameGroup;

  @UiField
  Typeahead namespaceTypeahead;

  @UiField
  TextBox namespace;

  @UiField
  TextBox name;

  @UiField
  ControlGroup valuesGroup;

  @UiField
  LocalizedEditor editor;

  @UiField
  ControlGroup namespaceGroup;

  @UiField
  Paragraph editAttributeHelp;

  @Inject
  public VariableAttributeModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.addCustomAttribute());
    new ConstrainedModal(modal);
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().save(namespace.getText(), name.getText(), editor.getLocalizedTexts());
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public void setNamespace(String namespace) {
    this.namespace.setText(namespace);
  }

  @Override
  public void setName(String name) {
    this.name.setText(name);
  }

  @Override
  public void setNamespaceSuggestions(List<VariableDto> variableDtos) {
    MultiWordSuggestOracle oracle = (MultiWordSuggestOracle) namespaceTypeahead.getSuggestOracle();
    oracle.clear();
    for(VariableDto dto : variableDtos) {
      for(AttributeDto attributeDto : JsArrays.toIterable(dto.getAttributesArray())) {
        oracle.add(attributeDto.getNamespace());
      }
    }
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void setDialogMode(BaseVariableAttributeModalPresenter.Mode mode) {
    editAttributeHelp.setVisible(false);
    switch(mode) {
      case APPLY:
        modal.setTitle(translations.applyAttribute());
        editAttributeHelp.setText(translations.applyAttributeHelp());
        editAttributeHelp.setVisible(true);
        break;
      case UPDATE_MULTIPLE:
        valuesGroup.setVisible(false);
        nameGroup.setVisible(false);
        modal.setTitle(translations.editAttributes());
        editAttributeHelp.setText(translations.editAttributesHelp());
        editAttributeHelp.setVisible(true);
        break;
      case DELETE:
        valuesGroup.setVisible(false);
        modal.setTitle(translations.removeAttributes());
        break;
      case UPDATE_SINGLE:
        modal.setTitle(translations.editAttribute());
        break;
    }
  }

  @Override
  public void setLocalizedTexts(Map<String, String> localizedTexts, List<String> locales) {
    editor.setLocalizedTexts(localizedTexts, locales);
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAMESPACE:
          group = namespaceGroup;
          break;
        case NAME:
          group = nameGroup;
          break;
        case VALUE:
          group = valuesGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

}
