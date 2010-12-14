/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.presenter;

import java.util.Arrays;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitDeletedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitSelectedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.KeyPairCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateDialogPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.KeyPairDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;

public class FunctionalUnitDetailsPresenter extends WidgetPresenter<FunctionalUnitDetailsPresenter.Display> {

  public static final String DELETE_ACTION = "Delete";

  public static final String DOWNLOAD_ACTION = "DownloadCertificate";

  private Runnable actionRequiringConfirmation;

  private FunctionalUnitUpdateDialogPresenter functionalUnitUpdateDialogPresenter;

  private AddKeyPairDialogPresenter addKeyPairDialogPresenter;

  private FunctionalUnitDto functionalUnit;

  public interface Display extends WidgetDisplay {
    void setKeyPairs(JsArray<KeyPairDto> keyPairs);

    HasActionHandler getActionColumn();

    void setFunctionalUnitDetails(FunctionalUnitDto FunctionalUnit);

    FunctionalUnitDto getFunctionalUnitDetails();

    void setRemoveFunctionalUnitCommand(Command command);

    void setDownloadIdentifiersCommand(Command command);

    void setExportIdentifiersCommand(Command command);

    void setUpdateFunctionalUnitCommand(Command command);

    void setCurrentCountOfIdentifiers(String count);

    void setAddKeyPairCommand(Command command);

    void setGenerateIdentifiersCommand(Command command);

    void setImportIdentifiersMappingCommand(Command command);

    void setImportIdentifiersFromDataCommand(Command command);

    void setAvailable(boolean available);

  }

  public interface ActionHandler {
    void doAction(KeyPairDto dto, String actionName);
  }

  public interface HasActionHandler {
    void setActionHandler(ActionHandler handler);
  }

  @Inject
  public FunctionalUnitDetailsPresenter(final Display display, final EventBus eventBus, FunctionalUnitUpdateDialogPresenter functionalUnitUpdateDialogPresenter, AddKeyPairDialogPresenter addKeyPairDialogPresenter) {
    super(display, eventBus);
    this.functionalUnitUpdateDialogPresenter = functionalUnitUpdateDialogPresenter;
    this.addKeyPairDialogPresenter = addKeyPairDialogPresenter;
  }

  @Override
  public void refreshDisplay() {
    updateCurrentCountOfIdentifiers();
  }

  @Override
  public void revealDisplay() {
    updateCurrentCountOfIdentifiers();
  }

  @Override
  protected void onBind() {
    initUiComponents();
    addHandlers();
    setCommands();
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

  private void initUiComponents() {
    getDisplay().setKeyPairs((JsArray<KeyPairDto>) JsArray.createArray());
    getDisplay().setAvailable(false);
  }

  private void addHandlers() {
    getDisplay().getActionColumn().setActionHandler(new ActionHandler() {
      public void doAction(KeyPairDto dto, String actionName) {
        if(actionName != null) {
          doActionImpl(dto, actionName);
        }
      }
    });

    super.registerHandler(eventBus.addHandler(FunctionalUnitSelectedEvent.getType(), new FunctionalUnitSelectedEvent.Handler() {

      @Override
      public void onFunctionalUnitSelected(FunctionalUnitSelectedEvent event) {
        refreshFunctionalUnitDetails(event.getFunctionalUnit());
      }
    }));

    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    super.registerHandler(eventBus.addHandler(FunctionalUnitUpdatedEvent.getType(), new FunctionalUnitUpdatedHandler()));
    super.registerHandler(eventBus.addHandler(KeyPairCreatedEvent.getType(), new KeyPairCreatedHandler()));

  }

  private void setCommands() {
    getDisplay().setDownloadIdentifiersCommand(new DownloadIdentifiersCommand());
    getDisplay().setExportIdentifiersCommand(new ExportIdentifiersCommand());
    getDisplay().setRemoveFunctionalUnitCommand(new RemoveFunctionalUnitCommand());

    getDisplay().setAddKeyPairCommand(new AddKeyPairCommand());
    // TODO add identifiers
    // getDisplay().setGenerateIdentifiersCommand(null);
    getDisplay().setImportIdentifiersMappingCommand(new ImportIdentifiersCommand());

    getDisplay().setUpdateFunctionalUnitCommand(new EditFunctionalUnitCommand());
  }

  private void updateCurrentCountOfIdentifiers() {
    getDisplay().setCurrentCountOfIdentifiers("");
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK) {
          getDisplay().setCurrentCountOfIdentifiers(response.getText());
        } else {
          getDisplay().setCurrentCountOfIdentifiers("");
        }
      }

    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/entities/count").get()//
    .withCallback(Response.SC_OK, callbackHandler) //
    .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
    .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  protected void doActionImpl(final KeyPairDto dto, String actionName) {
    if(actionName.equals(DOWNLOAD_ACTION)) {
      downloadCertificate(dto);
    } else if(actionName.equals(DELETE_ACTION)) {
      actionRequiringConfirmation = new Runnable() {
        public void run() {
          deleteKeyPair(dto);
        }
      };
      eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "deleteKeyPair", "confirmDeleteKeyPair"));
    }
  }

  private void deleteKeyPair(final KeyPairDto dto) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NOT_FOUND) {
          refreshKeyPairs(functionalUnit);
        } else {
          ClientErrorDto error = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, error.getStatus(), null));
        }
      }

    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/key/" + dto.getAlias()).delete() //
    .withCallback(Response.SC_OK, callbackHandler) //
    .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
    .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void downloadCertificate(final KeyPairDto dto) {
    String url = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "")) //
    .append("ws/functional-unit/").append(functionalUnit.getName()) //
    .append("/key/").append(dto.getAlias()).append("/certificate").toString();
    eventBus.fireEvent(new FileDownloadEvent(url));
  }

  private void refreshFunctionalUnitDetails(FunctionalUnitDto functionalUnit) {
    if(functionalUnit == null) {
      getDisplay().setAvailable(false);
    } else {
      String name = functionalUnit.getName();
      ResourceRequestBuilderFactory.<FunctionalUnitDto> newBuilder().forResource("/functional-unit/" + name).get().withCallback(new FunctionalUnitFoundCallBack()).withCallback(Response.SC_NOT_FOUND, new FunctionalUnitNotFoundCallBack(name)).send();
      refreshKeyPairs(functionalUnit);
    }
  }

  private void refreshKeyPairs(FunctionalUnitDto functionalUnit) {
    String name = functionalUnit.getName();
    ResourceRequestBuilderFactory.<JsArray<KeyPairDto>> newBuilder().forResource("/functional-unit/" + name + "/keys").get().withCallback(new KeyPairsCallback()).withCallback(Response.SC_NOT_FOUND, new FunctionalUnitNotFoundCallBack(name)).send();
  }

  private class AddKeyPairCommand implements Command {

    @Override
    public void execute() {
      addKeyPairDialogPresenter.bind();
      addKeyPairDialogPresenter.setFunctionalUnit(functionalUnit);
      addKeyPairDialogPresenter.revealDisplay();
    }

  }

  private final class DownloadIdentifiersCommand implements Command {
    @Override
    public void execute() {
      String url = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "")) //
      .append("ws/functional-unit/").append(functionalUnit.getName()) //
      .append("/entities/identifiers").toString();
      eventBus.fireEvent(new FileDownloadEvent(url));
    }
  }

  private final class ExportIdentifiersCommand implements Command {
    @Override
    public void execute() {
      String url = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "")) //
      .append("ws/functional-unit/").append(functionalUnit.getName()) //
      .append("/entities/csv").toString();
      eventBus.fireEvent(new FileDownloadEvent(url));
    }
  }

  private class EditFunctionalUnitCommand implements Command {

    @Override
    public void execute() {
      functionalUnitUpdateDialogPresenter.bind();
      functionalUnitUpdateDialogPresenter.setDialogMode(Mode.UPDATE);
      FunctionalUnitUpdateDialogPresenter.Display display = functionalUnitUpdateDialogPresenter.getDisplay();
      FunctionalUnitDto functionalUnit = getDisplay().getFunctionalUnitDetails();
      display.setName(functionalUnit.getName());
      display.setSelect(functionalUnit.getSelect());
      functionalUnitUpdateDialogPresenter.revealDisplay();
    }

  }

  private class RemoveFunctionalUnitCommand implements Command {

    @Override
    public void execute() {
      actionRequiringConfirmation = new Runnable() {
        public void run() {
          ResponseCodeCallback callbackHandler = new CommandResponseCallBack();
          ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
          eventBus.fireEvent(new FunctionalUnitDeletedEvent(getDisplay().getFunctionalUnitDetails()));
        }
      };
      eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "removeFunctionalUnit", "confirmDeleteFunctionalUnit"));
    }

  }

  private class ImportIdentifiersCommand implements Command {

    @Override
    public void execute() {
      eventBus.fireEvent(new WizardRequiredEvent(WizardType.IMPORT_IDENTIFIERS, functionalUnit.getName()));
    }

  }

  private class FunctionalUnitUpdatedHandler implements FunctionalUnitUpdatedEvent.Handler {

    @Override
    public void onFunctionalUnitUpdated(FunctionalUnitUpdatedEvent event) {
      refreshFunctionalUnitDetails(event.getFunctionalUnit());
    }

  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) && event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private class CommandResponseCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.INFO, "ReportJobStarted", null));
      }
    }
  }

  private class FunctionalUnitFoundCallBack implements ResourceCallback<FunctionalUnitDto> {

    @Override
    public void onResource(Response response, FunctionalUnitDto resource) {
      functionalUnit = resource;
      getDisplay().setFunctionalUnitDetails(functionalUnit);
      updateCurrentCountOfIdentifiers();
    }
  }

  private class KeyPairsCallback implements ResourceCallback<JsArray<KeyPairDto>> {

    @Override
    public void onResource(Response response, JsArray<KeyPairDto> resource) {
      JsArray<KeyPairDto> keyPairs = (resource != null) ? resource : (JsArray<KeyPairDto>) JsArray.createArray();
      getDisplay().setKeyPairs(keyPairs);
    }

  }

  private class FunctionalUnitNotFoundCallBack implements ResponseCodeCallback {

    private String templateName;

    public FunctionalUnitNotFoundCallBack(String FunctionalUnitName) {
      this.templateName = FunctionalUnitName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "FunctionalUnitCannotBeFound", Arrays.asList(new String[] { templateName })));
    }

  }

  private final class KeyPairCreatedHandler implements KeyPairCreatedEvent.Handler {
    @Override
    public void onKeyPairCreated(KeyPairCreatedEvent event) {
      if(event.getFunctionalUnit().getName().equals(functionalUnit.getName())) {
        refreshKeyPairs(functionalUnit);
      }
    }
  }

}
