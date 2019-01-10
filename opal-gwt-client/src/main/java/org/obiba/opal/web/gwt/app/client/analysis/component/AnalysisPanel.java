package org.obiba.opal.web.gwt.app.client.analysis.component;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead.UpdaterCallback;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.analysis.support.PluginTemplateVisitor;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.app.client.ui.SuggestListBox;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle.VariableSuggestion;
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

  private boolean enabled;

  private final VariableSuggestOracle oracle;

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

  @UiField(provided = true)
  SuggestListBox variables;

  public AnalysisPanel(EventBus eventBus) {
    this.eventBus = eventBus;
    enabled = true;

    oracle = new VariableSuggestOracle(eventBus);
    oracle.setLimit(10);

    variables = new SuggestListBox(oracle);

    initSuggestBox();
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void initialize(OpalAnalysisDto analysisDto,
                         TableDto tableDto, List<String> existingNames,
                         AnalysisPluginData data,
                         boolean enabled) {
    table = tableDto;
    analysis = analysisDto;
    validationHandler = new PanelValidationHandler(existingNames);
    setEnabled(enabled);

    oracle.setDatasource(table.getDatasourceName());
    oracle.setTable(table.getName());

    if (analysis == null) return;

    analyseName.setText(analysis.getName());

    if (data != null && data.hasPluginDto() && data.hasTemplateDto()) {
      pluginTemplateChooser.setSelectedTemplate(data.getPluginDto().getName(), data.getTemplateDto().getName());
      updateSchemaForm(pluginTemplateChooser.getSelectedData());
    }
    currentSelection = pluginTemplateChooser.getSelectedData();

    JsArrayString variablesArray = analysisDto.getVariablesArray();
    if (variablesArray != null) {
      for(int i = 0; i < variablesArray.length(); i++) {
        variables.addItem(variablesArray.get(i));
      }

      variables.getTextBox().setVisible(enabled);
    }
  }

  public void ensureAnalysis() {
    if (analysis == null) {
      analysis = OpalAnalysisDto.create();
    }

    analysis.setName(analyseName.getText());
    analysis.setPluginName(getPluginName());
    analysis.setTemplateName(getTemplateName());
    analysis.clearVariablesArray();

    Set<String> set = new HashSet<>(variables.getSelectedItemsTexts());

    for (String variable: set) {
      analysis.addVariables(variable);
    }

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
    pluginTemplateChooser.addPluginAndTemplates(plugin, templates);
    pluginTemplateChooser.update();
    pluginTemplateChooser.setSelectedIndex(0);
    updateSchemaForm(pluginTemplateChooser.getSelectedData());
  }

  private void setEnabled(boolean enabled) {
    this.enabled = enabled;
    analyseName.setEnabled(enabled);
    pluginTemplateChooser.setEnabled(enabled);
    variables.setReadOnly(!enabled);
    variables.getTextBox().setEnabled(enabled);
  }

  private void initSuggestBox() {
    variables.setUpdaterCallback(new UpdaterCallback() {
      @Override
      public String onSelection(Suggestion selectedSuggestion) {
        String variable = ((VariableSuggestion) selectedSuggestion).getVariable();
        if (!variables.getSelectedItemsTexts().contains(variable)) {
          variables.addItem(variable);
        }
        variables.getTextBox().setText("");
        variables.getTextBox().setFocus(true);
        return "";
      }
    });
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
    return JsonSchemaGWT.getModel(formPanel);
  }

  private void updateSchemaForm(PluginTemplateChooser.SelectionData data) {
    currentSelection = data;
    AnalysisPluginTemplateDto template = data.getTemplate();
    pluginTemplateHelp.setHTML(Markdown.parseNoStyle(template.getDescription()));

    formPanel.clear();
    JSONObject jsonObject = (JSONObject)JSONParser.parseStrict(template.getSchemaForm());
    jsonObject.put("readOnly", JSONBoolean.getInstance(!enabled));
    JSONObject jsonObjectValues = null;

    if (analysis != null && analysis.hasParameters()) {
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
    private List<String> existingNames;
    Map<HasType<ControlGroupType>, String> errors = new HashMap<HasType<ControlGroupType>, String>();

    List<String> charactersToAvoid;

    public PanelValidationHandler(List<String> existingNames) {
      this.existingNames = existingNames;

      charactersToAvoid = new ArrayList<String>();
      charactersToAvoid.add("#");
      charactersToAvoid.add("%");
      charactersToAvoid.add("&");
      charactersToAvoid.add("{");
      charactersToAvoid.add("}");
      charactersToAvoid.add("\\");
      charactersToAvoid.add("<");
      charactersToAvoid.add(">");
      charactersToAvoid.add("*");
      charactersToAvoid.add("?");
      charactersToAvoid.add("/");
      charactersToAvoid.add("$");
      charactersToAvoid.add("!");
      charactersToAvoid.add("'");
      charactersToAvoid.add("\"");
      charactersToAvoid.add(":");
      charactersToAvoid.add("@");
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getName(), "NameIsRequired", FormField.NAME.name()));
        validators.add(new ConditionValidator(nameIsUnique(), "NameIsUnique", FormField.NAME.name()));
        validators.add(new ConditionValidator(nameIsValidFileName(), "NameIsValidFileName", FormField.NAME.name()));
        validators.add(new ConditionValidator(validateType(), "PluginTypeIsRequired", FormField.TYPE.name()));
      }

      return validators;
    }

    private HasValue<Boolean> nameIsUnique() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return existingNames.indexOf(getName().getText()) == -1;
        }
      };
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

    private HasValue<Boolean> nameIsValidFileName() {

      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          boolean isOk = true;

          for (String character: charactersToAvoid) {
            isOk = isOk && !getName().getText().contains(character);
          }

          return isOk;
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
