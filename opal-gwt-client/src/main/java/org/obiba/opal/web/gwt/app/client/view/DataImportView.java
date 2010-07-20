/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * View of the dialog used to import data into Opal.
 */
public class DataImportView extends DataCommonView implements DataImportPresenter.Display {

  @UiTemplate("DataImportView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DataImportView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  SimplePanel filePanel;

  @UiField
  SimplePanel archivePanel;

  @UiField
  CheckBox shouldArchive;

  private FileSelectionPresenter.Display fileSelection;

  private FileSelectionPresenter.Display archiveSelection;

  public DataImportView() {
    initWidget(uiBinder.createAndBindUi(this));
    initWidgets();
  }

  @Override
  protected void initWidgets() {
    super.initWidgets();
    shouldArchive.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        archiveSelection.setEnabled(shouldArchive.getValue());
      }
    });
  }

  @Override
  public String getArchiveDirectory() {
    return this.shouldArchive.getValue() ? this.archiveSelection.getFile() : null;
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setFileWidgetDisplay(Display display) {
    filePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public String getSelectedFile() {
    return fileSelection.getFile();
  }

  @Override
  public void setArchiveWidgetDisplay(Display display) {
    archivePanel.setWidget(display.asWidget());
    archiveSelection = display;
    archiveSelection.setEnabled(shouldArchive.getValue());
    archiveSelection.setFieldWidth("20em");
  }

  @Override
  public void renderConclusionStep(String jobId) {
    super.renderConclusionStep(jobId);
    instructionsLabel.setText(translations.dataImportInstructionsConclusion());
  }

  @Override
  public void renderFormStep() {
    super.renderFormStep();
    instructionsLabel.setText(translations.dataImportInstructions());
  }

}
