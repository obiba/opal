/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.Wizard;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ConclusionStepPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class IdentifiersImportPresenter extends WidgetPresenter<IdentifiersImportPresenter.Display> implements Wizard {

  @Inject
  public IdentifiersImportPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCloseClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousClickHandler(ClickHandler handler);

    HandlerRegistration addFinishClickHandler(ClickHandler handler);

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    void setCsvOptionsFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    HasText getSelectedFile();

    void setFileValidator(ValidationHandler handler);

    void setCsvValidator(ValidationHandler handler);

    boolean isIdentifiersOnly();

    boolean isIdentifiersPlusData();

    void setUnits(JsArray<FunctionalUnitDto> units);

    String getSelectedUnit();

    ImportFormat getImportFormat();

    boolean isCsvFormatOptionsStep();

    /** Display no format options in the Format Options Step. The format chosen has no options. */
    void setNoFormatOptions();

    /** Display the CSV format options in the Format Options Step. */
    void setCsvFormatOptions();

    void renderPendingConclusion();

    void renderCompletedConclusion();

    void renderFailedConclusion();

    CsvOptionsView getCsvOptions();

    void setDefaultCharset(String defaultCharset);

  }

  @Inject
  private FileSelectionPresenter fileSelectionPresenter;

  @Inject
  private FileSelectionPresenter csvOptionsFileSelectionPresenter;

  private ImportData importData;

  private List<String> availableCharsets = new ArrayList<String>();

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
  }

  @Override
  public void revealDisplay() {
    initUnits();
    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  protected void onBind() {
    getDefaultCharset();
    getAvailableCharsets();

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    getDisplay().setFileSelectorWidgetDisplay(fileSelectionPresenter.getDisplay());

    csvOptionsFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    csvOptionsFileSelectionPresenter.bind();
    getDisplay().setCsvOptionsFileSelectorWidgetDisplay(csvOptionsFileSelectionPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addEventHandlers() {
    getDisplay().setFileValidator(new FileValidator());
    getDisplay().setCsvValidator(new CsvValidator());
    super.registerHandler(getDisplay().addCancelClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getDisplay().hideDialog();
      }
    }));
    super.registerHandler(getDisplay().addCloseClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getDisplay().hideDialog();
      }
    }));
    super.registerHandler(getDisplay().addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        update();
      }
    }));
    super.registerHandler(getDisplay().addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        finish();
      }
    }));
  }

  private void update() {
    if(fileSelectionPresenter.getSelectedFile() != null && !fileSelectionPresenter.getSelectedFile().equals("")) {
      csvOptionsFileSelectionPresenter.setSelectedFile(fileSelectionPresenter.getSelectedFile());
    }
    if(getDisplay().getImportFormat().equals(ImportFormat.CSV)) {
      getDisplay().setCsvFormatOptions();
    } else {
      getDisplay().setNoFormatOptions();
    }
  }

  class FileValidator extends AbstractValidationHandler {

    public FileValidator() {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getDisplay().getSelectedFile(), "NoFileSelected"));
      return validators;
    }
  }

  class CsvValidator extends AbstractValidationHandler {

    public CsvValidator() {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      if(getDisplay().getImportFormat().equals(ImportFormat.CSV)) {
        validators.add(new RequiredTextValidator(getSelectedCsvFile(), "NoDataFileSelected"));
        validators.add(new RegExValidator(getDisplay().getCsvOptions().getRowText(), "^[1-9]\\d*$", "RowMustBePositiveInteger"));
        validators.add(new ConditionalValidator(getDisplay().getCsvOptions().isCharsetSpecify(), new RequiredTextValidator(getDisplay().getCsvOptions().getCharsetSpecifyText(), "SpecificCharsetNotIndicated")));
        validators.add(new ConditionalValidator(getDisplay().getCsvOptions().isCharsetSpecify(), new ConditionValidator(isSpecificCharsetAvailable(), "CharsetNotAvailable")));
      }

      return validators;
    }
  }

  public void initUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
      @Override
      public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
        getDisplay().setUnits(units);
      }
    }).send();
  }

  private void finish() {
    getDisplay().renderPendingConclusion();
    populateImportData();
    importIdentifiers();
  }

  private void populateImportData() {
    importData = new ImportData();
    importData.setFormat(getDisplay().getImportFormat());
    importData.setDestinationDatasourceName("opal-keys");
    importData.setDestinationTableName("keys");
    importData.setCsvFile(csvOptionsFileSelectionPresenter.getSelectedFile());
    importData.setXmlFile(fileSelectionPresenter.getSelectedFile());
    importData.setUnit(getDisplay().getSelectedUnit());
    importData.setCharacterSet(getDisplay().getCsvOptions().getSelectedCharacterSet());
    importData.setRow(Integer.parseInt(getDisplay().getCsvOptions().getRowText().getText()));
    importData.setQuote(getDisplay().getCsvOptions().getQuote());
    importData.setField(getDisplay().getCsvOptions().getFieldSeparator());
  }

  private void importIdentifiers() {
    final DatasourceFactoryDto factory = ConclusionStepPresenter.createDatasourceFactoryDto(importData);

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          getDisplay().renderCompletedConclusion();
        } else {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          if(errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
            getDisplay().renderFailedConclusion();
          } else {
            eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "fileReadError", null));
          }
        }
      }
    };

    String unit = "s"; // "functional-units" (plural) when the file doesn't contain data.
    if(getDisplay().isIdentifiersPlusData()) unit = "/" + getDisplay().getSelectedUnit();
    ResourceRequestBuilderFactory.<DatasourceFactoryDto> newBuilder().forResource("/functional-unit" + unit + "/entities").post().withResourceBody(DatasourceFactoryDto.stringify(factory)).withCallback(200, callbackHandler).withCallback(400, callbackHandler).withCallback(500, callbackHandler).send();

  }

  public void getDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/default").get().withCallback(new ResourceCallback<JsArrayString>() {

      @Override
      public void onResource(Response response, JsArrayString resource) {
        String charset = resource.get(0);
        getDisplay().setDefaultCharset(charset);
      }
    }).send();

  }

  public void getAvailableCharsets() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/available").get().withCallback(new ResourceCallback<JsArrayString>() {
      @Override
      public void onResource(Response response, JsArrayString datasources) {
        for(int i = 0; i < datasources.length(); i++) {
          availableCharsets.add(datasources.get(i));
        }
      }
    }).send();
  }

  private HasText getSelectedCsvFile() {
    HasText result = new HasText() {

      public String getText() {
        return csvOptionsFileSelectionPresenter.getSelectedFile();
      }

      public void setText(String text) {
        // do nothing
      }
    };
    return result;
  }

  private HasValue<Boolean> isSpecificCharsetAvailable() {
    HasValue<Boolean> result = new HasValue<Boolean>() {

      public Boolean getValue() {
        return availableCharsets.contains(getDisplay().getCsvOptions().getCharsetSpecifyText().getText());
      }

      public void setValue(Boolean arg0) {
      }

      public void setValue(Boolean arg0, boolean arg1) {
      }

      public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> arg0) {
        return null;
      }

      public void fireEvent(GwtEvent<?> arg0) {
      }
    };
    return result;
  }

}
