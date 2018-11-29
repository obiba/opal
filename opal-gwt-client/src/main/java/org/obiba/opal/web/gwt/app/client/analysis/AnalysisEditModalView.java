package org.obiba.opal.web.gwt.app.client.analysis;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.analysis.component.PluginTypeChooser;
import org.obiba.opal.web.gwt.app.client.analysis.service.OpalAnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import java.util.List;

public class AnalysisEditModalView extends ModalPopupViewWithUiHandlers<AnalysisEditModalUiHandlers>
  implements AnalysisEditModalPresenter.Display {


  interface Binder extends UiBinder<Widget, AnalysisEditModalView> {
  }

  private OpalAnalysisDto analysis;

  private Translations translations;

  @UiField
  Modal dialog;

  @UiField
  TextBox analyseName;

  @UiField
  ControlGroup analyseGroup;

  @UiField
  ControlGroup typesGroup;

  @UiField
  PluginTypeChooser typeChooser;

  @UiField
  HelpBlock typeHelp;


  @Inject
  public AnalysisEditModalView(EventBus eventBus, AnalysisEditModalView.Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    this.translations = translations;
    dialog.setTitle("AnalysisEditModalView");
  }

  @Override
  public void onShow() {
    analysis = null;
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void initialize(OpalAnalysisDto dto, OpalAnalysisPluginData data) {
    GWT.log("AnalysisEditModalView.initialize()");
    analysis = dto;
    analyseName.setEnabled(analysis == null);
    analyseName.setText(analysis.getName());

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

    typeChooser.setSelectedTemplate(pluginName, templateName);
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("runButton")
  public void runButtonClick(ClickEvent event) {
    PluginTypeChooser.SelectedData selectedData = typeChooser.getSelectedData();
    GWT.log("Selected " + selectedData.getPlugin().getName() + " " + selectedData.getTemplate().getName());
  }

  @UiHandler("typeChooser")
  public void onTypeSelection(ChosenChangeEvent event) {
    PluginTypeChooser.SelectedData selectedData = typeChooser.getSelectedData();
    GWT.log("Selected " + selectedData.getPlugin().getName() + " " + selectedData.getTemplate().getName());
  }

  @Override
  public void accept(PluginPackageDto plugin, List<AnalysisPluginTemplateDto> templates) {
    GWT.log("AnalysisEditModalView.accept()");
    typeChooser.clear();
    typeChooser.addPluginAndTemplates(plugin, templates);
    typeChooser.update();
    typeChooser.setSelectedIndex(-1);
  }
}
