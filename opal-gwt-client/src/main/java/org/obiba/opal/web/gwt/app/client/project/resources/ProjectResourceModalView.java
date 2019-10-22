/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import java.util.Map;

public class ProjectResourceModalView extends ModalPopupViewWithUiHandlers<ProjectResourceModalUiHandlers> implements ProjectResourceModalPresenter.Display {

  private final Translations translations;

  private ResourceReferenceDto originalResource;

  interface Binder extends UiBinder<Widget, ProjectResourceModalView> {
  }

  @UiField
  Modal modal;

  @UiField
  ResourceFactoryChooser factoryChooser;

  @UiField
  HelpBlock factoryDescription;

  @UiField
  TabPanel tabPanel;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox nameText;

  @UiField
  FlowPanel paramsFormPanel;

  @UiField
  FlowPanel credentialsFormPanel;

  @UiField
  ModalFooter viewFooter;

  @UiField
  ModalFooter editFooter;

  private boolean readOnly;

  @Inject
  public ProjectResourceModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    this.translations = translations;
    modal.setTitle(translations.addResourceModalTitle());
    factoryChooser.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent event) {
        initializeResourceFactoryUI(readOnly);
      }
    });
  }

  @Override
  public void initialize(Map<String, ResourceFactoryDto> resourceFactories, ResourceReferenceDto resource, boolean readOnly) {
    this.readOnly = readOnly;
    factoryChooser.setResourceFactories(resourceFactories);
    factoryChooser.setEnabled(!readOnly);

    if (resource != null) {
      nameText.setText(resource.getName());
      modal.setTitle(readOnly ? translations.viewResourceModalTitle() : translations.editResourceModalTitle());
      factoryChooser.setSelectedFactory(resource);
    }
    this.originalResource = resource;
    nameText.setEnabled(resource == null);
    viewFooter.setVisible(readOnly);
    editFooter.setVisible(!readOnly);

    initializeResourceFactoryUI(readOnly);
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @UiHandler("closeButton")
  public void onCloseButton(ClickEvent event) {
    modal.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    modal.hide();
  }

  @UiHandler("saveButton")
  public void onSaveButton(ClickEvent event) {
    if (Strings.isNullOrEmpty(nameText.getText())) {
      modal.addAlert(translations.userMessageMap().get("NameIsRequired"), AlertType.ERROR, nameGroup);
      return;
    }
    getUiHandlers().onSave(nameText.getText(), factoryChooser.getSelectedValue(),
        getSchemaFormModel(paramsFormPanel), getSchemaFormModel(credentialsFormPanel));
  }

  private void initializeResourceFactoryUI(boolean readOnly) {
    ResourceFactoryDto factory = factoryChooser.getSelectedFactory();
    factoryDescription.setHTML(factory.hasDescription() ? Markdown.parseNoStyle(factory.getDescription()) : "");

    if (readOnly)
      tabPanel.getWidget(0).setVisible(false);

    applySchemaForm(paramsFormPanel, factory.getParametersSchemaForm(), originalResource == null ? null : originalResource.getParameters(), readOnly);
    applySchemaForm(credentialsFormPanel, factory.getCredentialsSchemaForm(), originalResource == null ? null : originalResource.getCredentials(), readOnly);
  }

  private void applySchemaForm(Panel containerPanel, String schemaForm, String values, boolean readOnly) {
    containerPanel.clear();
    JSONObject jsonObject = (JSONObject) JSONParser.parseStrict(schemaForm);
    jsonObject.put("readOnly", JSONBoolean.getInstance(readOnly));
    JSONValue description = jsonObject.get("description");
    if (description != null && description.isString() != null) {
      String descriptionStringValue = description.isString().stringValue();
      HelpBlock helpBlock = new HelpBlock();
      helpBlock.setHTML(Markdown.parseNoStyle(descriptionStringValue));
      containerPanel.add(helpBlock);
    }
    //jsonObject.put("readOnly", JSONBoolean.getInstance(!enabled));
    JSONObject jsonObjectValues = values == null ? null : (JSONObject) JSONParser.parseLenient(values);
    JsonSchemaGWT.buildUiIntoPanel(jsonObject, jsonObjectValues, containerPanel, getEventBus());
  }

  private JSONObject getSchemaFormModel(Panel containerPanel) {
    return JsonSchemaGWT.getModel(containerPanel);
  }
}
