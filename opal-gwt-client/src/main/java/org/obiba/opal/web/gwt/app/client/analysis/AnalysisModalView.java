package org.obiba.opal.web.gwt.app.client.analysis;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.obiba.opal.web.gwt.app.client.analysis.component.AnalysisPanel;
import org.obiba.opal.web.gwt.app.client.analysis.component.ResultsPanel;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.analysis.support.PluginTemplateVisitor;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

import java.util.Map;

public class AnalysisModalView extends ModalPopupViewWithUiHandlers<AnalysisModalUiHandlers>
  implements AnalysisModalPresenter.Display {

  interface Binder extends UiBinder<Widget, AnalysisModalView> {

  }

  private final AnalysisPanel analysisPanel;

  private final ResultsPanel resultsPanel;

  private OpalAnalysisDto analysis;

  private final Translations translations;

  @UiField
  Modal modal;

  @UiField
  TabPanel tabPanel;

  @UiField
  SimplePanel analysisOnlyContainer;

  @UiField
  SimplePanel analysisContainer;

  @UiField
  SimplePanel resultsContainer;

  @UiField
  Button runButton;

  @UiField
  Button cancelButton;


  @Inject
  public AnalysisModalView(EventBus eventBus, Binder binder, Translations translations, RequestUrlBuilder urlBuilder) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    this.translations = translations;
    analysisPanel = new AnalysisPanel(eventBus);
    resultsPanel = new ResultsPanel(getEventBus(), urlBuilder);
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void initialize(TableDto tableDto,
                         OpalAnalysisDto analysisDto,
                         List<String> existingNames,
                         AnalysisPluginData analysisPluginData) {
    analysis = analysisDto;
    boolean createMode = analysis == null || !analysis.hasName();
    analysisPanel.initialize(analysisDto, tableDto, existingNames, analysisPluginData, createMode);

    if (createMode) {
      initializeForCreate();
    } else {
      initializeForView(tableDto, analysisPluginData);
    }
  }

  private void initializeForCreate() {
    modal.setTitle(translations.analysisAddModalTitle());
    analysisOnlyContainer.add(analysisPanel);
    analysisOnlyContainer.setVisible(true);
  }

  private void initializeForView(TableDto tableDto, AnalysisPluginData analysisPluginData) {
    int analysisResultsCount = analysis.getAnalysisResultsCount();

    if (analysisResultsCount == 0) {
      analysisOnlyContainer.add(analysisPanel);
      analysisOnlyContainer.setVisible(true);
    } else {
      resultsPanel.initialize(tableDto, analysis.getAnalysisResultsArray());
      analysisContainer.add(analysisPanel);
      resultsContainer.add(resultsPanel);
      tabPanel.setVisible(true);
    }

    modal.setTitle(
      analysis.getName()
        + " - " + analysisPluginData.getPluginDto().getTitle()
        + " / " + analysisPluginData.getTemplateDto().getTitle()
    );

    runButton.setVisible(false);
    cancelButton.setText(translations.closeLabel());
  }

  @Override
  public PluginTemplateVisitor getTemplateVisitor() {
    return analysisPanel;
  }

  @Override
  public boolean validate() {
    Map<HasType<ControlGroupType>, String> errors = analysisPanel.validate();

    for (Map.Entry<HasType<ControlGroupType>, String> entry: errors.entrySet()) {
      modal.addAlert(entry.getValue(), AlertType.ERROR, entry.getKey());
    }
    return errors.isEmpty();
  }

  @Override
  public void showError(String message) {
    modal.addAlert(message, AlertType.ERROR);
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
    analysisPanel.ensureAnalysis();
    getUiHandlers().run(analysisPanel.getAnalysis());
  }

}