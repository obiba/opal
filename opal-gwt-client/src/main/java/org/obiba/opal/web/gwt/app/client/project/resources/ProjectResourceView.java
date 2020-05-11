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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceProviderDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

public class ProjectResourceView extends ViewWithUiHandlers<ProjectResourceUiHandlers> implements ProjectResourcePresenter.Display {

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final ResourceProvidersService resourceProvidersService;

  private final EventBus eventBus;

  interface Binder extends UiBinder<Widget, ProjectResourceView> {
  }

  @UiField
  TabPanel tabPanel;

  @UiField
  Label name;

  @UiField
  SimplePanel descriptionPanel;

  @UiField
  com.google.gwt.user.client.ui.Label providerLabel;

  @UiField
  Anchor providerLink;

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

  @UiField
  Panel buttonsPanel;

  private ResourceReferenceDto resource;

  @Inject
  public ProjectResourceView(Binder uiBinder, Translations translations, TranslationMessages translationMessages, EventBus eventBus, ResourceProvidersService resourceProvidersService) {
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.resourceProvidersService = resourceProvidersService;
    this.eventBus = eventBus;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("remove")
  void onRemove(ClickEvent event) {
    getUiHandlers().onDelete();
  }

  @UiHandler("edit")
  void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  @UiHandler("duplicate")
  void onDuplicate(ClickEvent event) {
    getUiHandlers().onDuplicate();
  }

  @Override
  public void renderResource(ResourceReferenceDto resource) {
    this.resource = resource;

    credentialsPanel.setVisible(resource.getEditable());
    permissionsPanel.setVisible(resource.getEditable());
    if (!resource.getEditable())
      tabPanel.selectTab(0);
    tabPanel.getWidget(0).setVisible(resource.getEditable());
    buttonsPanel.setVisible(resource.getEditable());

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
    ResourceFactoryDto factory = resourceProvidersService.getResourceFactory(resource.getProvider(), resource.getFactory());
    factoryDescription.setHTML(factory.hasDescription() ? Markdown.parseNoStyle(factory.getDescription()) : "");
    factoryTitle.setText(factory.getTitle());
    ResourceProviderDto provider = resourceProvidersService.getResourceProvider(factory.getProvider());
    if (provider.hasWeb()) {
      providerLabel.setVisible(false);
      providerLink.setHref(provider.getWeb());
      providerLink.setTarget("_blank");
      providerLink.setText(provider.getName() + " - " + provider.getTitle());
      providerLink.setTitle(provider.getDescription());
      providerLink.setVisible(true);
    } else {
      providerLink.setVisible(false);
      providerLabel.setText(provider.getName() + " - " + provider.getTitle());
      providerLabel.setTitle(provider.getDescription());
      providerLabel.setVisible(true);
    }
    name.setText(resource.getName());
    descriptionPanel.clear();
    if (resource.hasDescription()) {
      descriptionPanel.setWidget(new HTMLPanel(Markdown.parse(resource.getDescription())));
    }
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
