package org.obiba.opal.web.gwt.app.client.analysis;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.obiba.opal.web.gwt.app.client.analysis.event.RunAnalysisRequestEvent;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginsRepository;
import org.obiba.opal.web.gwt.app.client.analysis.support.PluginTemplateVisitor;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

public class AnalysisEditModalPresenter extends ModalPresenterWidget<AnalysisEditModalPresenter.Display>
  implements AnalysisEditModalUiHandlers {


  private final Translations translations;

  private AnalysisPluginsRepository plugins;

  private final ValidationHandler validationHandler;

  private TableDto valueTable;

  private OpalAnalysisDto analysis;


  @Inject
  public AnalysisEditModalPresenter(AnalysisEditModalPresenter.Display display,
                                    EventBus eventBus,
                                    Translations translations) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.translations = translations;
    validationHandler = new ModalValidationHandler();
  }

  @Override
  protected void onBind() {
  }

  public void initialize(TableDto valueTable, OpalAnalysisDto analysis, List<PluginPackageDto> plugins) {
    this.valueTable = valueTable;
    this.analysis = analysis;
    this.plugins = new AnalysisPluginsRepository(plugins);
    this.plugins.visitPlugins(getView());
    getView().initialize(analysis, this.plugins.findAnalysisPluginData(analysis));
  }

  @Override
  public void run() {
    getView().clearErrors();
    if (validationHandler.validate()) {
      updateAnalysis();
      fireEvent(new RunAnalysisRequestEvent(analysis));
      getView().hideDialog();
    }
  }

  private void updateAnalysis() {
    if (analysis == null) {
      analysis = OpalAnalysisDto.create();
    }

    analysis.setName(getView().getName().getText());
    analysis.setPluginName(getView().getPluginName());
    analysis.setTemplateName(getView().getTemplateName());
    String s = getView().getSchemaFormModel().toString();
    analysis.setParameters(s);
    analysis.setDatasource(valueTable.getDatasourceName());
    analysis.setTable(valueTable.getName());
  }

  /**
   * Validator class
   */
  private class ModalValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();

        validators.add(
          new RequiredTextValidator(getView().getName(),
          "NameIsRequired",
          AnalysisEditModalPresenter.Display.FormField.NAME.name())
        );

        validators.add(
          new ConditionValidator(validateType(),
            "PluginTypeIsRequired",
            Display.FormField.TYPE.name())
        );

      }

      return validators;
    }

    private HasValue<Boolean> validateType() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          String pluginName = getView().getPluginName();
          String templateName = getView().getTemplateName();
          return !(pluginName.isEmpty() && templateName.isEmpty());
        }
      };
    }

    @Override
    public boolean validate() {
      boolean validSchemaForm = getView().validateSchemaForm();
      return super.validate() && validSchemaForm;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(AnalysisEditModalPresenter.Display.FormField.valueOf(id), message);
    }

  }

  /**
   * View
   */
  public interface Display extends PopupView, HasUiHandlers<AnalysisEditModalUiHandlers>, PluginTemplateVisitor {

    enum FormField {
      NAME,
      TYPE,
      SCHEMA_FORM,
    }

    void hideDialog();

    void initialize(OpalAnalysisDto dto, AnalysisPluginData pluginData);

    HasText getName();

    String getPluginName();

    String getTemplateName();

    HasWidgets getSchemaForm();

    JSONObject getSchemaFormModel();

    void showError(@Nullable FormField formField, String message);

    boolean validateSchemaForm();

    void clearErrors();

  }


}
