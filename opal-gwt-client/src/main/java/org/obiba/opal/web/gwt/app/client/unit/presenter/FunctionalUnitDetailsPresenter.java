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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitDeletedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitSelectedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.KeyPairCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateDialogPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter.IdentifiersImportPresenter;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.KeyDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FunctionalUnitDetailsPresenter extends PresenterWidget<FunctionalUnitDetailsPresenter.Display> {

  public static final String DELETE_ACTION = "Delete";

  public static final String DOWNLOAD_ACTION = "DownloadCertificate";

  private Runnable removeConfirmation;

  private GenerateConfirmationRunnable generateConfirmation;

  private FunctionalUnitUpdateDialogPresenter functionalUnitUpdateDialogPresenter;

  private AddKeyPairDialogPresenter addKeyPairDialogPresenter;

  private FunctionalUnitDto functionalUnit;

  private Request countIdentifiersRequest;

  public interface Display extends View {
    void setKeyPairs(JsArray<KeyDto> keyPairs);

    HasActionHandler<KeyDto> getActionColumn();

    void setFunctionalUnitDetails(FunctionalUnitDto FunctionalUnit);

    FunctionalUnitDto getFunctionalUnitDetails();

    void setRemoveFunctionalUnitCommand(Command command);

    void setDownloadIdentifiersCommand(Command command);

    void setExportIdentifiersCommand(Command command);

    void setUpdateFunctionalUnitCommand(Command command);

    void setCurrentCountOfIdentifiers(String count);

    void setAddKeyPairCommand(Command command);

    void setGenerateIdentifiersCommand(Command command);

    void setImportIdentifiersFromDataCommand(Command command);

    void setAvailable(boolean available);

    HasAuthorization getRemoveFunctionalUnitAuthorizer();

    HasAuthorization getDownloadIdentifiersAuthorizer();

    HasAuthorization getExportIdentifiersAuthorizer();

    HasAuthorization getGenerateIdentifiersAuthorizer();

    HasAuthorization getImportIdentifiersFromDataAuthorizer();

    HasAuthorization getAddKeyPairAuthorizer();

    HasAuthorization getListKeyPairsAuthorizer();

    HasAuthorization getUpdateFunctionalUnitAuthorizer();

  }

  @Inject
  public FunctionalUnitDetailsPresenter(final Display display, final EventBus eventBus, FunctionalUnitUpdateDialogPresenter functionalUnitUpdateDialogPresenter, AddKeyPairDialogPresenter addKeyPairDialogPresenter) {
    super(eventBus, display);
    this.functionalUnitUpdateDialogPresenter = functionalUnitUpdateDialogPresenter;
    this.addKeyPairDialogPresenter = addKeyPairDialogPresenter;
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    updateCurrentCountOfIdentifiers();
  }

  @Override
  protected void onBind() {
    super.onBind();
    initUiComponents();
    addHandlers();
    setCommands();
  }

  private void initUiComponents() {
    getView().setKeyPairs((JsArray<KeyDto>) JsArray.createArray());
    getView().setAvailable(false);
  }

  private void addHandlers() {
    getView().getActionColumn().setActionHandler(new ActionHandler<KeyDto>() {
      public void doAction(KeyDto dto, String actionName) {
        if(actionName != null) {
          doActionImpl(dto, actionName);
        }
      }
    });

    super.registerHandler(getEventBus().addHandler(FunctionalUnitSelectedEvent.getType(), new FunctionalUnitSelectedEvent.Handler() {

      @Override
      public void onFunctionalUnitSelected(FunctionalUnitSelectedEvent event) {
        refreshFunctionalUnitDetails(event.getFunctionalUnit());
      }
    }));

    super.registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    super.registerHandler(getEventBus().addHandler(FunctionalUnitUpdatedEvent.getType(), new FunctionalUnitUpdatedHandler()));
    super.registerHandler(getEventBus().addHandler(KeyPairCreatedEvent.getType(), new KeyPairCreatedHandler()));
  }

  private void setCommands() {
    getView().setDownloadIdentifiersCommand(new DownloadIdentifiersCommand());
    getView().setExportIdentifiersCommand(new ExportIdentifiersCommand());
    getView().setRemoveFunctionalUnitCommand(new RemoveFunctionalUnitCommand());

    getView().setGenerateIdentifiersCommand(new GenerateIdentifiersCommand());
    getView().setImportIdentifiersFromDataCommand(new ImportIdentifiersCommand());

    getView().setAddKeyPairCommand(new AddKeyPairCommand());

    getView().setUpdateFunctionalUnitCommand(new EditFunctionalUnitCommand());
  }

  private void updateCurrentCountOfIdentifiers() {
    if(countIdentifiersRequest != null && countIdentifiersRequest.isPending()) {
      countIdentifiersRequest.cancel();
      countIdentifiersRequest = null;
    }
    getView().setCurrentCountOfIdentifiers("");
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK) {
          getView().setCurrentCountOfIdentifiers(response.getText());
        } else {
          getView().setCurrentCountOfIdentifiers("");
        }
      }

    };

    countIdentifiersRequest = ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/entities/count").get()//
    .withCallback(Response.SC_OK, callbackHandler) //
    .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
    .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void authorize() {
    // export identifiers
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/entities/identifiers").get().authorize(getView().getDownloadIdentifiersAuthorizer()).send();
    // export identifiers mapping
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/entities/csv").get().authorize(getView().getExportIdentifiersAuthorizer()).send();
    // remove
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName()).delete().authorize(getView().getRemoveFunctionalUnitAuthorizer()).send();

    // generate identifiers
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/entities/identifiers").post().authorize(getView().getGenerateIdentifiersAuthorizer()).send();
    // add identifiers
    ResourceAuthorizationRequestBuilderFactory.newBuilder()//
    .forResource("/functional-unit/" + functionalUnit.getName() + "/entities").post()//
    .authorize(CascadingAuthorizer.newBuilder().and("/functional-units/entities/table", HttpMethod.GET)//
    .authorize(getView().getImportIdentifiersFromDataAuthorizer()).build())//
    .send();
    // add key pair
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/keys").post().authorize(getView().getAddKeyPairAuthorizer()).send();

    // edit
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName()).put().authorize(getView().getUpdateFunctionalUnitAuthorizer()).send();

    // display key pairs
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/keys").get().authorize(getView().getListKeyPairsAuthorizer()).send();
  }

  private void authorizeDownloadCertificate(KeyDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/key/" + dto.getAlias() + "/certificate").get().authorize(authorizer).send();
  }

  private void authorizeDeleteKeyPair(KeyDto dto, HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/key/" + dto.getAlias()).delete().authorize(authorizer).send();
  }

  protected void doActionImpl(final KeyDto dto, String actionName) {
    if(actionName.equals(DOWNLOAD_ACTION)) {
      authorizeDownloadCertificate(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          downloadCertificate(dto);
        }
      });
    } else if(actionName.equals(DELETE_ACTION)) {
      authorizeDeleteKeyPair(dto, new Authorizer(getEventBus()) {

        @Override
        public void authorized() {
          removeConfirmation = new Runnable() {
            public void run() {
              deleteKeyPair(dto);
            }
          };
          getEventBus().fireEvent(new ConfirmationRequiredEvent(removeConfirmation, "deleteKeyPair", "confirmDeleteKeyPair"));
        }
      });
    }
  }

  private void deleteKeyPair(final KeyDto dto) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NOT_FOUND) {
          refreshKeyPairs(functionalUnit);
        } else {
          ClientErrorDto error = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).build());
        }
      }

    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/key/" + dto.getAlias()).delete() //
    .withCallback(Response.SC_OK, callbackHandler) //
    .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
    .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void downloadCertificate(final KeyDto dto) {
    String url = new StringBuilder("/functional-unit/").append(functionalUnit.getName()) //
    .append("/key/").append(dto.getAlias()).append("/certificate").toString();
    getEventBus().fireEvent(new FileDownloadEvent(url));
  }

  private void refreshFunctionalUnitDetails(FunctionalUnitDto functionalUnit) {
    if(functionalUnit == null) {
      getView().setAvailable(false);
    } else {
      String name = functionalUnit.getName();
      ResourceRequestBuilderFactory.<FunctionalUnitDto> newBuilder().forResource("/functional-unit/" + name).get().withCallback(new FunctionalUnitFoundCallBack()).withCallback(Response.SC_NOT_FOUND, new FunctionalUnitNotFoundCallBack(name)).send();
      refreshKeyPairs(functionalUnit);
    }
  }

  private void refreshKeyPairs(FunctionalUnitDto functionalUnit) {
    String name = functionalUnit.getName();
    ResourceRequestBuilderFactory.<JsArray<KeyDto>> newBuilder().forResource("/functional-unit/" + name + "/keys").get().withCallback(new KeyPairsCallback()).withCallback(Response.SC_NOT_FOUND, new FunctionalUnitNotFoundCallBack(name)).send();
  }

  private class AddKeyPairCommand implements Command {

    @Override
    public void execute() {
      addKeyPairDialogPresenter.setFunctionalUnit(functionalUnit);
      addToPopupSlot(addKeyPairDialogPresenter);
    }

  }

  private final class GenerateIdentifiersCommand implements Command {

    @Override
    public void execute() {
      if(generateConfirmation != null) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("IdentifiersGenerationPending").build());
      } else {
        generateConfirmation = new GenerateConfirmationRunnable();
        getEventBus().fireEvent(new ConfirmationRequiredEvent(generateConfirmation, "generateFunctionalUnitIdentifiers", "confirmGenerateFunctionalUnitIdentifiers"));
      }
    }
  }

  private final class DownloadIdentifiersCommand implements Command {
    @Override
    public void execute() {
      String url = new StringBuilder("/functional-unit/").append(functionalUnit.getName()) //
      .append("/entities/identifiers").toString();
      getEventBus().fireEvent(new FileDownloadEvent(url));
    }
  }

  private final class GenerateConfirmationRunnable implements Runnable {
    public void run() {
      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_OK) {
            int count = 0;
            try {
              count = Integer.parseInt(response.getText());
            } catch(NumberFormatException e) {
            }
            if(count > 0) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().info("IdentifiersGenerationCompleted").args(functionalUnit.getName(), response.getText()).nonSticky().build());
            } else {
              getEventBus().fireEvent(NotificationEvent.newBuilder().info("NoIdentifiersGenerated").args(functionalUnit.getName()).nonSticky().build());
            }
          } else {
            getEventBus().fireEvent(NotificationEvent.newBuilder().error("IdentifiersGenerationFailed").args(functionalUnit.getName()).build());
          }
          generateConfirmation = null;
          onReveal();
        }

      };

      ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/entities/identifiers").post()//
      .withCallback(Response.SC_OK, callbackHandler) //
      .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
      .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
    }
  }

  private final class ExportIdentifiersCommand implements Command {
    @Override
    public void execute() {
      String url = new StringBuilder("/functional-unit/").append(functionalUnit.getName()) //
      .append("/entities/csv").toString();
      getEventBus().fireEvent(new FileDownloadEvent(url));
    }
  }

  private class EditFunctionalUnitCommand implements Command {

    @Override
    public void execute() {
      functionalUnitUpdateDialogPresenter.setDialogMode(Mode.UPDATE);
      FunctionalUnitUpdateDialogPresenter.Display display = functionalUnitUpdateDialogPresenter.getView();
      FunctionalUnitDto functionalUnit = getView().getFunctionalUnitDetails();
      display.setName(functionalUnit.getName());
      display.setSelect(functionalUnit.getSelect());
      addToPopupSlot(functionalUnitUpdateDialogPresenter);
    }

  }

  private class RemoveFunctionalUnitCommand implements Command {

    @Override
    public void execute() {
      removeConfirmation = new Runnable() {
        public void run() {
          ResponseCodeCallback callbackHandler = new FunctionalUnitDeleteCallback();
          ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
        }
      };
      getEventBus().fireEvent(new ConfirmationRequiredEvent(removeConfirmation, "removeFunctionalUnit", "confirmDeleteFunctionalUnit"));
    }

  }

  private class ImportIdentifiersCommand implements Command {

    @Override
    public void execute() {
      getEventBus().fireEvent(new WizardRequiredEvent(IdentifiersImportPresenter.WizardType, functionalUnit));
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
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      } else if(generateConfirmation != null && event.getSource().equals(generateConfirmation)) {
        if(event.isConfirmed()) {
          generateConfirmation.run();
        } else {
          generateConfirmation = null;
        }
      }
    }
  }

  private class FunctionalUnitDeleteCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_OK) { // unit was deleted
        getEventBus().fireEvent(new FunctionalUnitDeletedEvent(getView().getFunctionalUnitDetails()));
      }
    }
  }

  private class FunctionalUnitFoundCallBack implements ResourceCallback<FunctionalUnitDto> {

    @Override
    public void onResource(Response response, FunctionalUnitDto resource) {
      functionalUnit = resource;
      getView().setFunctionalUnitDetails(functionalUnit);
      updateCurrentCountOfIdentifiers();
      authorize();
    }
  }

  private class KeyPairsCallback implements ResourceCallback<JsArray<KeyDto>> {

    @Override
    public void onResource(Response response, JsArray<KeyDto> resource) {
      JsArray<KeyDto> keyPairs = (resource != null) ? resource : (JsArray<KeyDto>) JsArray.createArray();
      getView().setKeyPairs(keyPairs);
    }

  }

  private class FunctionalUnitNotFoundCallBack implements ResponseCodeCallback {

    private String functionalUnitName;

    public FunctionalUnitNotFoundCallBack(String functionalUnitName) {
      this.functionalUnitName = functionalUnitName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("FunctionalUnitCannotBeFound").args(functionalUnitName).build());
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
