/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PopupView;

public class DataImportPresenter extends WizardPresenterWidget<DataImportPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  public static class Wizard extends WizardProxy<DataImportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<DataImportPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  private final CsvFormatStepPresenter csvFormatStepPresenter;

  private final XmlFormatStepPresenter xmlFormatStepPresenter;

  private final DestinationSelectionStepPresenter destinationSelectionStepPresenter;

  private final IdentityArchiveStepPresenter identityArchiveStepPresenter;

  private final ConclusionStepPresenter conclusionStepPresenter;

  private DataImportFormatStepPresenter formatStepPresenter;

  @Inject
  public DataImportPresenter(final Display display, final EventBus eventBus, CsvFormatStepPresenter csvFormatStepPresenter, XmlFormatStepPresenter xmlFormatStepPresenter, DestinationSelectionStepPresenter destinationSelectionStepPresenter, IdentityArchiveStepPresenter identityArchiveStepPresenter, ConclusionStepPresenter conclusionStepPresenter) {
    super(eventBus, display);
    this.csvFormatStepPresenter = csvFormatStepPresenter;
    this.xmlFormatStepPresenter = xmlFormatStepPresenter;
    this.destinationSelectionStepPresenter = destinationSelectionStepPresenter;
    this.identityArchiveStepPresenter = identityArchiveStepPresenter;
    this.conclusionStepPresenter = conclusionStepPresenter;
  }

  @Override
  protected void onBind() {
    csvFormatStepPresenter.bind();
    xmlFormatStepPresenter.bind();
    destinationSelectionStepPresenter.bind();
    identityArchiveStepPresenter.bind();
    conclusionStepPresenter.bind();

    getView().setDestinationSelectionDisplay(destinationSelectionStepPresenter.getDisplay());
    getView().setIdentityArchiveStepDisplay(identityArchiveStepPresenter.getDisplay());

    addEventHandlers();
  }

  protected void addEventHandlers() {
    super.registerHandler(getView().addCancelClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getView().hide();
      }
    }));
    super.registerHandler(getView().addCloseClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getView().hide();
      }
    }));
    super.registerHandler(getView().addImportClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        ImportData importData = formatStepPresenter.getImportData();
        identityArchiveStepPresenter.updateImportData(importData);
        destinationSelectionStepPresenter.updateImportData(importData);
        conclusionStepPresenter.launchImport(importData);
        getView().renderConclusion(conclusionStepPresenter);
      }
    }));

    super.registerHandler(getView().addFormatChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent evt) {
        updateFormatStepDisplay();
      }
    }));
    getView().setFormatStepValidator(new ValidationHandler() {

      @Override
      public boolean validate() {
        return formatStepPresenter.validate();
      }
    });

  }

  private void updateFormatStepDisplay() {
    destinationSelectionStepPresenter.setImportFormat(getView().getImportFormat());
    if(getView().getImportFormat().equals(ImportFormat.CSV)) {
      csvFormatStepPresenter.clear();
      this.formatStepPresenter = csvFormatStepPresenter;
      getView().setFormatStepDisplay(csvFormatStepPresenter.getDisplay());
    } else if(getView().getImportFormat().equals(ImportFormat.XML)) {
      this.formatStepPresenter = xmlFormatStepPresenter;
      getView().setFormatStepDisplay(xmlFormatStepPresenter.getDisplay());
    } else {
      this.formatStepPresenter = null;
      throw new IllegalStateException("Unknown format: " + getView().getImportFormat());
    }
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    csvFormatStepPresenter.unbind();
    xmlFormatStepPresenter.unbind();
    destinationSelectionStepPresenter.unbind();
  }

  @Override
  public void onReveal() {
    destinationSelectionStepPresenter.refreshDisplay(); // to refresh the datasources
    updateFormatStepDisplay();
  }

  public interface Display extends PopupView {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    ImportFormat getImportFormat();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCloseClickHandler(ClickHandler clickHandler);

    HandlerRegistration addFormatChangeHandler(ChangeHandler handler);

    void setFormatStepDisplay(DataImportStepDisplay display);

    void setFormatStepValidator(ValidationHandler handler);

    void setDestinationSelectionDisplay(DataImportStepDisplay display);

    void setIdentityArchiveStepDisplay(DataImportStepDisplay display);

    public void renderConclusion(ConclusionStepPresenter presenter);

    HandlerRegistration addImportClickHandler(ClickHandler handler);

  }

  public interface DataImportFormatStepPresenter {

    /**
     * Get the import data as collected.
     * @return
     */
    public ImportData getImportData();

    /**
     * Validate the import data were correctly provided, and send notification error messages if any.
     * @return
     */
    public boolean validate();

  }

  public interface DataImportStepDisplay {

    public Widget asWidget();

    public Widget getStepHelp();

  }

}
