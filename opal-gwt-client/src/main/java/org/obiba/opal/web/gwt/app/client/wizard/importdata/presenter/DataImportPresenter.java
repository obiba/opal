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

import org.obiba.opal.web.gwt.app.client.util.DatasourceDtos;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter.Display.Slots;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class DataImportPresenter extends WizardPresenterWidget<DataImportPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  private DataImportFormatStepPresenter formatStepPresenter;

  private final CsvFormatStepPresenter csvFormatStepPresenter;

  private final XmlFormatStepPresenter xmlFormatStepPresenter;

  private final DestinationSelectionStepPresenter destinationSelectionStepPresenter;

  private final UnitSelectionStepPresenter unitSelectionStepPresenter;

  private final ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  private final DatasourceValuesStepPresenter datasourceValuesStepPresenter;

  private final ArchiveStepPresenter archiveStepPresenter;

  private final ConclusionStepPresenter conclusionStepPresenter;

  private TransientDatasourceHandler transientDatasourceHandler;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public DataImportPresenter(final Display display, final EventBus eventBus, //
  CsvFormatStepPresenter csvFormatStepPresenter, XmlFormatStepPresenter xmlFormatStepPresenter, //
  DestinationSelectionStepPresenter destinationSelectionStepPresenter, UnitSelectionStepPresenter unitSelectionStepPresenter, //
  ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter, ArchiveStepPresenter archiveStepPresenter, //
  ConclusionStepPresenter conclusionStepPresenter, DatasourceValuesStepPresenter datasourceValuesStepPresenter) {
    super(eventBus, display);
    this.csvFormatStepPresenter = csvFormatStepPresenter;
    this.xmlFormatStepPresenter = xmlFormatStepPresenter;
    this.destinationSelectionStepPresenter = destinationSelectionStepPresenter;
    this.unitSelectionStepPresenter = unitSelectionStepPresenter;
    this.comparedDatasourcesReportPresenter = comparedDatasourcesReportPresenter;
    this.archiveStepPresenter = archiveStepPresenter;
    this.conclusionStepPresenter = conclusionStepPresenter;
    this.datasourceValuesStepPresenter = datasourceValuesStepPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();
    csvFormatStepPresenter.bind();
    xmlFormatStepPresenter.bind();
    destinationSelectionStepPresenter.bind();
    comparedDatasourcesReportPresenter.bind();
    conclusionStepPresenter.bind();

    comparedDatasourcesReportPresenter.allowIgnoreAllModifications(false);

    setInSlot(Slots.Unit, unitSelectionStepPresenter);
    setInSlot(Slots.Values, datasourceValuesStepPresenter);
    setInSlot(Slots.Archive, archiveStepPresenter);

    getView().setDestinationSelectionDisplay(destinationSelectionStepPresenter.getDisplay());
    getView().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getDisplay());
    getView().setComparedDatasourcesReportStepInHandler(transientDatasourceHandler = new TransientDatasourceHandler());

    addEventHandlers();
  }

  protected void addEventHandlers() {
    super.registerHandler(getView().addFormatChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent evt) {
        updateFormatStepDisplay();
      }
    }));
    getView().setImportDataInputsHandler(new ImportDataInputsHandlerImpl());

  }

  public interface ImportDataInputsHandler {
    public boolean validateFormat();

    public boolean validateDestination();

    public boolean validateComparedDatasourcesReport();
  }

  @Override
  protected void onFinish() {
    ImportData importData = transientDatasourceHandler.getImportData();
    archiveStepPresenter.updateImportData(importData);
    conclusionStepPresenter.launchImport(importData);
    getView().renderConclusion(conclusionStepPresenter);
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

  //
  // Inner classes and interfaces
  //

  private final class ImportDataInputsHandlerImpl implements ImportDataInputsHandler {
    @Override
    public boolean validateFormat() {
      if(formatStepPresenter.validate()) {
        if(getView().getImportFormat().equals(ImportFormat.CSV)) {
          String name = csvFormatStepPresenter.getSelectedFile();
          name = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
          destinationSelectionStepPresenter.getDisplay().setTable(name);
        } else {
          destinationSelectionStepPresenter.getDisplay().setTable("");
        }
        return true;
      }
      return false;
    }

    @Override
    public boolean validateDestination() {
      return destinationSelectionStepPresenter.validate();
    }

    @Override
    public boolean validateComparedDatasourcesReport() {
      datasourceValuesStepPresenter.setDatasource(transientDatasourceHandler.getImportData().getTransientDatasourceName());
      return comparedDatasourcesReportPresenter.canBeSubmitted();
    }
  }

  public static class Wizard extends WizardProxy<DataImportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<DataImportPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  private final class TransientDatasourceHandler implements StepInHandler {

    private ImportData importData;

    public ImportData getImportData() {
      return importData;
    }

    @Override
    public void onStepIn() {
      comparedDatasourcesReportPresenter.getDisplay().clearDisplay();
      removeTransientDatasource();
      importData = formatStepPresenter.getImportData();
      destinationSelectionStepPresenter.updateImportData(importData);
      unitSelectionStepPresenter.updateImportData(importData);
      createTransientDatasource();
    }

    private void removeTransientDatasource() {
      if(importData == null) return;

      deleteTransientDatasource();
      importData = null;
    }

    private void deleteTransientDatasource() {
      if(importData.getTransientDatasourceName() == null) return;

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          // ignore
        }
      };

      ResourceRequestBuilderFactory.newBuilder().forResource("/datasource/" + importData.getTransientDatasourceName()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler).withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
    }

    private void createTransientDatasource() {
      getView().prepareDatasourceCreation();

      final DatasourceFactoryDto factory = DatasourceDtos.createDatasourceFactoryDto(importData);

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == 201) {
            DatasourceDto datasourceDto = (DatasourceDto) JsonUtils.unsafeEval(response.getText());
            importData.setTransientDatasourceName(datasourceDto.getName());
            datasourceDiff(factory, datasourceDto);
          } else {
            getView().showDatasourceCreationError((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
          }
        }
      };

      ResourceRequestBuilderFactory.<DatasourceFactoryDto> newBuilder().forResource("/transient-datasources").post().withResourceBody(DatasourceFactoryDto.stringify(factory)).withCallback(201, callbackHandler).withCallback(400, callbackHandler).withCallback(500, callbackHandler).send();
    }

    private void datasourceDiff(final DatasourceFactoryDto factory, final DatasourceDto datasourceDto) {
      comparedDatasourcesReportPresenter.compare(importData.getTransientDatasourceName(), //
      importData.getDestinationDatasourceName(), new DatasourceCreatedCallback() {

        @Override
        public void onSuccess(DatasourceFactoryDto factory, DatasourceDto datasource) {
          getView().showDatasourceCreationSuccess();
        }

        @Override
        public void onFailure(DatasourceFactoryDto factory, ClientErrorDto error) {
          getView().showDatasourceCreationError(error);
        }
      },//
      factory, datasourceDto);
      getView().showDatasourceCreationSuccess();
    }
  }

  public interface Display extends WizardView {

    enum Slots {
      Unit, Values, Archive
    }

    ImportFormat getImportFormat();

    void setImportDataInputsHandler(ImportDataInputsHandler handler);

    void prepareDatasourceCreation();

    void showDatasourceCreationSuccess();

    void showDatasourceCreationError(ClientErrorDto errorDto);

    public void setComparedDatasourcesReportStepInHandler(StepInHandler handler);

    void setComparedDatasourcesReportDisplay(WizardStepDisplay display);

    HandlerRegistration addFormatChangeHandler(ChangeHandler handler);

    void setFormatStepDisplay(WizardStepDisplay display);

    void setDestinationSelectionDisplay(WizardStepDisplay display);

    public void renderConclusion(ConclusionStepPresenter presenter);

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

}
