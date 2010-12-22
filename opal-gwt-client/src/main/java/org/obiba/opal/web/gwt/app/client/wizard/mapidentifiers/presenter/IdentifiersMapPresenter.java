/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.presenter;

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
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
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
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
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

public class IdentifiersMapPresenter extends WidgetPresenter<IdentifiersMapPresenter.Display> implements Wizard {

  @Inject
  public IdentifiersMapPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCloseClickHandler(ClickHandler handler);

    HandlerRegistration addFinishClickHandler(ClickHandler handler);

    void setCsvOptionsFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    void renderPendingConclusion();

    void renderCompletedConclusion(String count);

    void renderFailedConclusion();

    CsvOptionsView getCsvOptions();

    void setDefaultCharset(String defaultCharset);

    void renderMappedUnits(JsArray<FunctionalUnitDto> units);

    void renderMappedUnitsFailed();

    void setFileSelectionValidator(ValidationHandler handler);

    HandlerRegistration addFileSelectedClickHandler(ClickHandler clickHandler);

    String getSelectedUnitName();

  }

  @Inject
  private FileSelectionPresenter csvOptionsFileSelectionPresenter;

  private List<String> availableCharsets = new ArrayList<String>();

  protected TableDto identifiersTable;

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    // nothing to do
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  protected void onBind() {
    getIdentifiersTable();
    getDefaultCharset();
    getAvailableCharsets();

    csvOptionsFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    csvOptionsFileSelectionPresenter.bind();
    getDisplay().setCsvOptionsFileSelectorWidgetDisplay(csvOptionsFileSelectionPresenter.getDisplay());

    getDisplay().setFileSelectionValidator(new FileValidator());

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

  private void getIdentifiersTable() {
    ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource("/functional-units/entities/table").get().withCallback(new ResourceCallback<TableDto>() {

      @Override
      public void onResource(Response response, TableDto resource) {
        if(resource != null) {
          identifiersTable = resource;
        }
      }
    }).send();
  }

  private void addEventHandlers() {
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

    super.registerHandler(getDisplay().addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        finish();
      }
    }));

    super.registerHandler(getDisplay().addFileSelectedClickHandler(new FileSelectedHandler()));
  }

  class FileValidator extends AbstractValidationHandler {

    public FileValidator() {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new RegExValidator(getSelectedCsvFile(), ".csv$", "CSVFileRequired"));
      validators.add(new RegExValidator(getDisplay().getCsvOptions().getRowText(), "^[1-9]\\d*$", "RowMustBePositiveInteger"));
      validators.add(new ConditionalValidator(getDisplay().getCsvOptions().isCharsetSpecify(), new RequiredTextValidator(getDisplay().getCsvOptions().getCharsetSpecifyText(), "SpecificCharsetNotIndicated")));
      validators.add(new ConditionalValidator(getDisplay().getCsvOptions().isCharsetSpecify(), new ConditionValidator(isSpecificCharsetAvailable(), "CharsetNotAvailable")));

      return validators;
    }
  }

  private final class FileSelectedHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      // get the units from the map file
      String path = "/functional-units/entities/identifiers/map" + getSelectedCsvFile().getText() + "/units";

      ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>> newBuilder().forResource(path).get()//
      .withCallback(new GetMappedUnitsCompletedCallback())//
      .withCallback(Response.SC_NOT_FOUND, new GetMappedUnitsFailedCallback())//
      .withCallback(Response.SC_BAD_REQUEST, new GetMappedUnitsFailedCallback())//
      .send();
    }
  }

  private final class GetMappedUnitsCompletedCallback implements ResourceCallback<JsArray<FunctionalUnitDto>> {
    @Override
    public void onResource(Response response, JsArray<FunctionalUnitDto> resource) {
      JsArray<FunctionalUnitDto> units = JsArrays.toSafeArray(resource);
      if(units.length() != 2) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "TwoMappedUnitsExpected", null));
        getDisplay().renderMappedUnitsFailed();
      } else {
        getDisplay().renderMappedUnits(units);
      }
    }
  }

  private final class GetMappedUnitsFailedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_NOT_FOUND) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "MappedUnitsCannotBeIdentified", null));
      } else if(response.getStatusCode() == Response.SC_BAD_REQUEST) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "fileReadError", null));
      }
      getDisplay().renderMappedUnitsFailed();
    }
  }

  private void finish() {
    getDisplay().renderPendingConclusion();
    mapIdentifiers();
  }

  private void mapIdentifiers() {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          getDisplay().renderCompletedConclusion(response.getText());
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

    String path = "/functional-unit/" + getDisplay().getSelectedUnitName() + "/entities/identifiers/map" + getSelectedCsvFile().getText();

    ResourceRequestBuilderFactory.<DatasourceFactoryDto> newBuilder().forResource(path).put()//
    .withCallback(200, callbackHandler)//
    .withCallback(400, callbackHandler)//
    .withCallback(500, callbackHandler).send();
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
