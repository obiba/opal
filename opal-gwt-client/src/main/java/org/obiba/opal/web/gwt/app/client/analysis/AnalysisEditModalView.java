package org.obiba.opal.web.gwt.app.client.analysis;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.obiba.opal.web.gwt.app.client.analysis.component.PluginTemplateChooser;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.SchemaUiContainer;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import javax.annotation.Nullable;
import java.util.List;

public class AnalysisEditModalView extends ModalPopupViewWithUiHandlers<AnalysisEditModalUiHandlers>
  implements AnalysisEditModalPresenter.Display {

  interface Binder extends UiBinder<Widget, AnalysisEditModalView> {

  }

  private OpalAnalysisDto analysis;

  private Translations translations;

  private PluginTemplateChooser.SelectionData currentSelection;

  @UiField
  Modal modal;

  @UiField
  TextBox analyseName;

  @UiField
  ControlGroup analyseGroup;

  @UiField
  ControlGroup typeGroup;

  @UiField
  PluginTemplateChooser pluginTemplateChooser;

  @UiField
  HelpBlock pluginTemplateHelp;
  
  @UiField
  FlowPanel formPanel;

  @UiField
  TextBox variables;


  @Inject
  public AnalysisEditModalView(EventBus eventBus, AnalysisEditModalView.Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void onShow() {
    analysis = null;
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void initialize(OpalAnalysisDto analysisDto, AnalysisPluginData data) {
    currentSelection = null;
    analysis = analysisDto;
    analyseName.setText(analysis.getName());
    modal.setTitle(analysis == null ? translations.analysisAddModalTitle() : translations.analysisEditModalTitle());

    if (analysis == null) return;

    String pluginName = data != null && data.hasPluginDto()
      ? analysis.getPluginName()
      : null;

    String templateName = data != null && data.hasTemplateDto()
      ? data.getTemplateDto().getName()
      : null;

    if (Strings.isNullOrEmpty(pluginName) || Strings.isNullOrEmpty(templateName)) {
      throw new IllegalArgumentException("Plugin or template of an analysis cannot be null");
    }

    pluginTemplateChooser.setSelectedTemplate(pluginName, templateName);
    update(pluginTemplateChooser.getSelectedData());
  }

  @Override
  public HasText getName() {
    return analyseName;
  }

  @Override
  public String getPluginName() {
    return currentSelection == null ? "" : currentSelection.getPlugin().getName();
  }

  @Override
  public String getTemplateName() {
    return currentSelection == null ? "" : currentSelection.getTemplate().getName();
  }

  @Override
  public HasWidgets getSchemaForm() {
    return formPanel;
  }

  @Override
  public JSONObject getSchemaFormModel() {
    JSONObject jsonObject = new JSONObject();

    for(Widget widget : formPanel) {
      if(widget instanceof SchemaUiContainer) {
        SchemaUiContainer widgetAsSchemaUiContainer = (SchemaUiContainer) widget;
        jsonObject.put(widgetAsSchemaUiContainer.getKey(), widgetAsSchemaUiContainer.getJSONValue());
      }
    }

    return jsonObject;
  }

  @Override
  public void showError(@Nullable AnalysisEditModalPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAME:
          group = analyseGroup;
          break;
        case TYPE:
          group = typeGroup;
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
  public boolean validateSchemaForm() {
    Map<HasType<ControlGroupType>, String> errors = new HashMap<>();

    for(Widget widget : formPanel) {
      if (widget instanceof SchemaUiContainer) {
        SchemaUiContainer widgetAsSchemaUiContainer = (SchemaUiContainer) widget;
        String validationError = widgetAsSchemaUiContainer.validate();

        if (validationError.length() > 0) {
          errors.put(widgetAsSchemaUiContainer, widgetAsSchemaUiContainer.getTitle() + ": " + validationError);
        }
      }
    }

    for (Entry<HasType<ControlGroupType>, String> entry: errors.entrySet()) {
      modal.addAlert(entry.getValue(), AlertType.ERROR, entry.getKey());
    }

    return errors.size() == 0;
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    modal.hide();
  }

  @UiHandler("runButton")
  public void runButtonClick(ClickEvent event) {
    getUiHandlers().run();
    currentSelection = pluginTemplateChooser.getSelectedData();

    GWT.log("Selected " + currentSelection.getPlugin().getName() + " " + currentSelection.getTemplate().getName());
    GWT.log("Form model" + getSchemaFormModel());
  }

  @UiHandler("pluginTemplateChooser")
  public void onPluginTemplateSelection(ChosenChangeEvent event) {
    update(pluginTemplateChooser.getSelectedData());
  }

  @Override
  public void accept(PluginPackageDto plugin, List<AnalysisPluginTemplateDto> templates) {
    pluginTemplateChooser.clear();
    pluginTemplateChooser.addPluginAndTemplates(plugin, templates);
    pluginTemplateChooser.update();
    pluginTemplateChooser.setSelectedIndex(-1);
  }

  private void update(PluginTemplateChooser.SelectionData data) {
    currentSelection = data;
    AnalysisPluginTemplateDto template = data.getTemplate();
    pluginTemplateHelp.setHTML(Markdown.parseNoStyle(template.getDescription()));

    formPanel.clear();
    JSONObject jsonObject = (JSONObject)JSONParser.parseStrict(template.getSchemaForm());
    JSONObject jsonObjectValues = null;

    if (analysis != null) {
      jsonObjectValues = (JSONObject)JSONParser.parseLenient(analysis.getParameters());
    }

    JsonSchemaGWT.buildUiIntoPanel(jsonObject, jsonObjectValues, formPanel, getEventBus());
  }


}
