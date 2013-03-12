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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class IdentifiersMapPresenter extends WizardPresenterWidget<IdentifiersMapPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  public static class Wizard extends WizardProxy<IdentifiersMapPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<IdentifiersMapPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  public interface Display extends WizardView {

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

  private final FileSelectionPresenter csvOptionsFileSelectionPresenter;

  private final List<String> availableCharsets = new ArrayList<String>();

  protected TableDto identifiersTable;

  @Inject
  public IdentifiersMapPresenter(final Display display, final EventBus eventBus,
      FileSelectionPresenter csvOptionsFileSelectionPresenter) {
    super(eventBus, display);
    this.csvOptionsFileSelectionPresenter = csvOptionsFileSelectionPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();
    getIdentifiersTable();
    getDefaultCharset();
    getAvailableCharsets();

    csvOptionsFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    csvOptionsFileSelectionPresenter.bind();
    getView().setCsvOptionsFileSelectorWidgetDisplay(csvOptionsFileSelectionPresenter.getDisplay());

    getView().setFileSelectionValidator(new FileValidator());

    super.registerHandler(getView().addFileSelectedClickHandler(new FileSelectedHandler()));
  }

  @Override
  protected void onFinish() {
    UriBuilder ub = UriBuilder.create()
        .segment("functional-unit", getView().getSelectedUnitName(), "entities", "identifiers", "map");
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).put()
        .authorize(new Authorizer(getEventBus()) {

          @Override
          public void authorized() {
            getView().renderPendingConclusion();
            mapIdentifiers();
          }
        }).send();
  }

  private void getIdentifiersTable() {
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource("/functional-units/entities/table").get()
        .withCallback(new ResourceCallback<TableDto>() {

          @Override
          public void onResource(Response response, TableDto resource) {
            if(resource != null) {
              identifiersTable = resource;
            }
          }
        }).send();
  }

  class FileValidator extends AbstractValidationHandler {

    public FileValidator() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new RegExValidator(getSelectedCsvFile(), ".csv$", "CSVFileRequired"));
      validators
          .add(new RegExValidator(getView().getCsvOptions().getRowText(), "^[1-9]\\d*$", "RowMustBePositiveInteger"));
      validators.add(new RequiredTextValidator(getView().getCsvOptions().getCharsetText(), "CharsetNotAvailable"));

      return validators;
    }
  }

  private final class FileSelectedHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      // get the units from the map file
      String path = "/functional-units/entities/identifiers/map/units?path=" + getSelectedCsvFile().getText();

      ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>>newBuilder().forResource(path).get()//
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
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("TwoMappedUnitsExpected").build());
        getView().renderMappedUnitsFailed();
      } else {
        getView().renderMappedUnits(units);
      }
    }
  }

  private final class GetMappedUnitsFailedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_NOT_FOUND) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("MappedUnitsCannotBeIdentified").build());
      } else if(response.getStatusCode() == Response.SC_BAD_REQUEST) {
        try {
          ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorDto.getStatus()).build());
        } catch(Exception e) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error("fileReadError").build());
        }
      }
      getView().renderMappedUnitsFailed();
    }
  }

  private void mapIdentifiers() {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 200) {
          getView().renderCompletedConclusion(response.getText());
        } else {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          getView().renderFailedConclusion();
          if(errorDto != null) {
            // getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorDto.getStatus()).build());
            JsArrayString errors = JsArrays.toSafeArray(errorDto.getArgumentsArray());
            for(int i = 0; i < errors.length(); i++) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(errors.get(i)).build());
            }
          }
        }
      }
    };
    String build = UriBuilder.create()
        .segment("functional-unit", getView().getSelectedUnitName(), "entities", "identifiers", "map").build();
    String path = build + "?path=" + getSelectedCsvFile().getText();

    ResourceRequestBuilderFactory.newBuilder().forResource(path).put()//
        .accept("application/x-protobuf+json").accept("text/plain").withCallback(200, callbackHandler)//
        .withCallback(400, callbackHandler)//
        .withCallback(500, callbackHandler).send();
  }

  public void getDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/default").get()
        .withCallback(new ResourceCallback<JsArrayString>() {

          @Override
          public void onResource(Response response, JsArrayString resource) {
            String charset = resource.get(0);
            getView().setDefaultCharset(charset);
          }
        }).send();

  }

  public void getAvailableCharsets() {
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/available").get()
        .withCallback(new ResourceCallback<JsArrayString>() {
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

}
