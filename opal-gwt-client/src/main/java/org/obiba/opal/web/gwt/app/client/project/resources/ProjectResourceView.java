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

import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import java.util.Map;

public class ProjectResourceView extends ViewWithUiHandlers<ProjectResourceUiHandlers> implements ProjectResourcePresenter.Display {

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final EventBus eventBus;

  interface Binder extends UiBinder<Widget, ProjectResourceView> {
  }

  @UiField
  TabPanel tabPanel;

  @UiField
  Label name;

  @UiField
  Label factoryTitle;

  @UiField
  Label url;

  @UiField
  Label format;

  @UiField
  HelpBlock factoryDescription;

  @UiField
  FlowPanel paramsFormPanel;

  @UiField
  FlowPanel credentialsPanel;

  @UiField
  FlowPanel credentialsFormPanel;

  @UiField
  Panel permissionsPanel;

  private ResourceReferenceDto resource;

  private Map<String, ResourceFactoryDto> resourceFactories;

  @Inject
  public ProjectResourceView(Binder uiBinder, Translations translations, TranslationMessages translationMessages, EventBus eventBus) {
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.eventBus = eventBus;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void renderResource(Map<String, ResourceFactoryDto> resourceFactories, ResourceReferenceDto resource) {
    this.resourceFactories = resourceFactories;
    this.resource = resource;

    credentialsPanel.setVisible(resource.getEditable());
    permissionsPanel.setVisible(resource.getEditable());
    if (!resource.getEditable())
      tabPanel.selectTab(0);
    tabPanel.getWidget(0).setVisible(resource.getEditable());

    initializeResourceFactoryUI();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    permissionsPanel.clear();
    if (content != null) {
      permissionsPanel.add(content.asWidget());
    }
  }

  private void initializeResourceFactoryUI() {
    ResourceFactoryDto factory = resourceFactories.get(resource.getProvider() + ":" + resource.getFactory());
    factoryDescription.setHTML(factory.hasDescription() ? Markdown.parseNoStyle(factory.getDescription()) : "");
    factoryTitle.setText(factory.getTitle());
    name.setText(resource.getName());
    url.setText(resource.getResource().getUrl());
    format.setText(resource.getResource().getFormat());

    applySchemaForm(paramsFormPanel, factory.getParametersSchemaForm(), resource.getParameters());
    applySchemaForm(credentialsFormPanel, factory.getCredentialsSchemaForm(), resource.getCredentials());
  }

  private void applySchemaForm(Panel containerPanel, String schemaForm, String values) {
    containerPanel.clear();
    JSONObject jsonObject = (JSONObject) JSONParser.parseStrict(schemaForm);
    jsonObject.put("readOnly", JSONBoolean.getInstance(true));
    JSONValue description = jsonObject.get("description");
    if (description != null && description.isString() != null) {
      String descriptionStringValue = description.isString().stringValue();
      HelpBlock helpBlock = new HelpBlock();
      helpBlock.setHTML(Markdown.parseNoStyle(descriptionStringValue));
      containerPanel.add(helpBlock);
    }
    //jsonObject.put("readOnly", JSONBoolean.getInstance(!enabled));
    JSONObject jsonObjectValues = values == null ? null : (JSONObject) JSONParser.parseLenient(values);
    JsonSchemaGWT.buildUiIntoPanel(jsonObject, jsonObjectValues, containerPanel, eventBus);
  }
}
