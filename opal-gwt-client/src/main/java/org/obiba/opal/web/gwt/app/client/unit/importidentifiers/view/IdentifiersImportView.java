/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.importidentifiers.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.ui.WizardModalBox;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.unit.importidentifiers.presenter.IdentifiersImportPresenter;
import org.obiba.opal.web.gwt.app.client.ui.ModalViewImpl;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

public class IdentifiersImportView extends ModalViewImpl implements IdentifiersImportPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, IdentifiersImportView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardModalBox dialog;

  @UiField
  WizardStep formatSelectionStep;

  @UiField
  SimplePanel selectFilePanel;

  @UiField
  ListBox formatListBox;

  @UiField
  WizardStep formatStep;

  @UiField
  Panel xmlOptions;

  @UiField
  CsvOptionsView csvOptions;

  private FileSelectionPresenter.Display fileSelection;

  private WizardStepChain stepChain;

  @Inject
  public IdentifiersImportView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
  }

  private void initWidgets() {
    formatListBox.addItem(translations.csvLabel(), ImportFormat.CSV.name());
    formatListBox.addItem(translations.opalXmlLabel(), ImportFormat.XML.name());
    formatListBox.addItem(translations.spssLabel(), ImportFormat.SPSS.name());
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//
        .append(formatSelectionStep)//
        .title(translations.selectFileAndDataFormatLabel())//

        .append(formatStep)//
        .title(translations.dataImportFileStep())//

        .onNext().onPrevious().build();

  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected Modal asModal() {
    return dialog;
  }

  @Override
  public void show() {
    stepChain.reset();
    super.show();
  }

  @Override
  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return dialog.addNextClickHandler(handler);
  }

  @Override
  public HandlerRegistration addPreviousClickHandler(ClickHandler handler) {
    return dialog.addPreviousClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return dialog.addCloseClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  @Override
  public void setFileSelectorWidgetDisplay(Display display) {
    selectFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public HasText getSelectedFile() {
    return fileSelection.getFileText();
  }

  @Override
  public boolean isIdentifiersOnly() {
    return false;
  }

  @Override
  public boolean isIdentifiersPlusData() {
    return true;
  }

  @Override
  public void setCsvOptionsFileSelectorWidgetDisplay(Display display) {
    csvOptions.setCsvFileSelectorWidgetDisplay(display);
  }

  @Override
  public ImportFormat getImportFormat() {
    return ImportFormat.valueOf(formatListBox.getValue(formatListBox.getSelectedIndex()));
  }

  @Override
  public void setNoFormatOptions() {
    xmlOptions.setVisible(true);
    csvOptions.setVisible(false);
  }

  @Override
  public void setCsvFormatOptions() {
    xmlOptions.setVisible(false);
    csvOptions.setVisible(true);
  }

  @Override
  public void renderPendingConclusion() {
    dialog.setProgress(true);
    dialog.setFinishEnabled(false);
    dialog.setCancelEnabled(false);
  }

  @Override
  public void renderCompletedConclusion() {
    dialog.setProgress(false);
    dialog.hide();
  }

  @Override
  public void renderFailedConclusion() {
    dialog.setProgress(false);
    dialog.setFinishEnabled(true);
    dialog.setCancelEnabled(true);
  }

  @Override
  public CsvOptionsView getCsvOptions() {
    return csvOptions;
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    csvOptions.setDefaultCharset(defaultCharset);
  }

}
