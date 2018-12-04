package org.obiba.opal.web.gwt.app.client.analysis.component;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.analysis.support.PluginTemplateVisitor;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.app.client.ui.SchemaUiContainer;
import org.obiba.opal.web.gwt.app.client.validator.*;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import java.util.*;

public class AnalysisPanel extends Composite implements PluginTemplateVisitor {

  interface Binder extends UiBinder<Widget, AnalysisPanel>  {}

  enum FormField {
    NAME,
    TYPE,
    SCHEMA_FORM,
  }

  private TableDto table;

  private static final Binder uiBinder = GWT.create(Binder.class);

  private final EventBus eventBus;

  private OpalAnalysisDto analysis;

  private PanelValidationHandler validationHandler;

  private PluginTemplateChooser.SelectionData currentSelection;

  private boolean enabled = true;

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

  public AnalysisPanel(EventBus eventBus) {
    initWidget(uiBinder.createAndBindUi(this));
    this.eventBus = eventBus;
  }

  public void initialize(OpalAnalysisDto analysisDto, TableDto tableDto, AnalysisPluginData data, boolean enabled) {
    table = tableDto;
    currentSelection = null;
    analysis = analysisDto;
    validationHandler = new PanelValidationHandler();
    setEnabled(enabled);

    if (analysis == null) return;

    analyseName.setText(analysis.getName());

    String pluginName = data != null && data.hasPluginDto()
      ? analysis.getPluginName()
      : null;

    String templateName = data != null && data.hasTemplateDto()
      ? data.getTemplateDto().getName()
      : null;

    pluginTemplateChooser.setSelectedTemplate(pluginName, templateName);
    updateSchemaForm(pluginTemplateChooser.getSelectedData());
  }

  public void ensureAnalysis() {
    if (analysis == null) {
      analysis = OpalAnalysisDto.create();
    }

    analysis.setName(analyseName.getText());
    analysis.setPluginName(getPluginName());
    analysis.setTemplateName(getTemplateName());
    analysis.setParameters(getSchemaFormModel().toString());
    analysis.setDatasource(table.getDatasourceName());
    analysis.setTable(table.getName());
  }

  public OpalAnalysisDto getAnalysis() {
    return analysis;
  }

  public Map<HasType<ControlGroupType>, String> validate() {
    validationHandler.validate();
    return validationHandler.getErrors();
  }

  @UiHandler("pluginTemplateChooser")
  public void onPluginTemplateSelection(ChosenChangeEvent event) {
    updateSchemaForm(pluginTemplateChooser.getSelectedData());
  }

  @Override
  public void accept(PluginPackageDto plugin, List<AnalysisPluginTemplateDto> templates) {
    pluginTemplateChooser.clear();
    pluginTemplateChooser.addPluginAndTemplates(plugin, templates);
    pluginTemplateChooser.update();
    pluginTemplateChooser.setSelectedIndex(-1);
  }

  private void setEnabled(boolean enabled) {
    this.enabled = enabled;
    analyseName.setEnabled(enabled);
    pluginTemplateChooser.setEnabled(enabled);
    variables.setEnabled(enabled);
  }

  private HasText getName() {
    return analyseName;
  }

  private String getPluginName() {
    return currentSelection == null ? "" : currentSelection.getPlugin().getName();
  }

  private String getTemplateName() {
    return currentSelection == null ? "" : currentSelection.getTemplate().getName();
  }

  private JSONObject getSchemaFormModel() {
    JSONObject jsonObject = new JSONObject();

    for(Widget widget : formPanel) {
      if(widget instanceof SchemaUiContainer) {
        SchemaUiContainer widgetAsSchemaUiContainer = (SchemaUiContainer) widget;
        jsonObject.put(widgetAsSchemaUiContainer.getKey(), widgetAsSchemaUiContainer.getJSONValue());
      }
    }

    return jsonObject;
  }

  private void updateSchemaForm(PluginTemplateChooser.SelectionData data) {
    currentSelection = data;
    AnalysisPluginTemplateDto template = data.getTemplate();
    pluginTemplateHelp.setHTML(Markdown.parseNoStyle(template.getDescription()));

    formPanel.clear();
    JSONObject jsonObject = (JSONObject)JSONParser.parseStrict(template.getSchemaForm());
    jsonObject.put("readOnly", JSONBoolean.getInstance(!enabled));
    JSONObject jsonObjectValues = null;

    if (analysis != null) {
      jsonObjectValues = (JSONObject)JSONParser.parseLenient(analysis.getParameters());
    }

    JsonSchemaGWT.buildUiIntoPanel(jsonObject, jsonObjectValues, formPanel, eventBus);
  }

  private ControlGroup getFieldGroup(String formField) {
    ControlGroup group = null;

    if(formField != null) {

      switch(FormField.valueOf(formField)) {
        case NAME:
          group = analyseGroup;
          break;
        case TYPE:
          group = typeGroup;
          break;
      }
    }

    return group;
  }

  /**
   * Validator class
   */
  private class PanelValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;
    Map<HasType<ControlGroupType>, String> errors = new HashMap<HasType<ControlGroupType>, String>();

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getName(), "NameIsRequired", FormField.NAME.name()));
        validators.add(new ConditionValidator(validateType(), "PluginTypeIsRequired", FormField.TYPE.name()));
      }

      return validators;
    }

    private HasValue<Boolean> validateType() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          String pluginName = getPluginName();
          String templateName = getTemplateName();
          return !(pluginName.isEmpty() && templateName.isEmpty());
        }
      };
    }


    @Override
    public boolean validate() {
      errors = JsonSchemaGWT.validate(formPanel);
      return super.validate();
    }

    @Override
    protected void showMessage(String id, String message) {
      errors.put(getFieldGroup(id), message);
    }

    public Map<HasType<ControlGroupType>, String> getErrors() {
      return errors;
    }
  }

}
