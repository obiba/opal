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

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitDeletedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitSelectedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.GenerateIdentifiersConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.KeyPairCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.importidentifiers.presenter.IdentifiersImportPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateModalPresenter.Mode;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.KeyDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FunctionalUnitDetailsPresenter
    extends Presenter<FunctionalUnitDetailsPresenter.Display, FunctionalUnitDetailsPresenter.Proxy>
    implements FunctionalUnitUiHandlers {

  public static final String DELETE_ACTION = "Delete";

  public static final String DOWNLOAD_ACTION = "DownloadCertificate";

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private Runnable removeConfirmation;

  private GenerateConfirmationRunnable generateConfirmation;

  private final ModalProvider<FunctionalUnitUpdateModalPresenter> functionalUnitModalProvider;

  private final Provider<AddKeyPairModalPresenter> addKeyPairModalPresenter;

  private final GenerateIdentifiersModalPresenter generateIdentifiersModalPresenter;

  private FunctionalUnitDto functionalUnit;

  private Request countIdentifiersRequest;

  @ProxyStandard
  @NameToken(Places.UNIT)
  public interface Proxy extends ProxyPlace<FunctionalUnitDetailsPresenter> {}

  public interface Display extends View, HasUiHandlers<FunctionalUnitUiHandlers>, HasBreadcrumbs {
    void setKeyPairs(JsArray<KeyDto> keyPairs);

    HasActionHandler<KeyDto> getActionColumn();

    void setFunctionalUnitDetails(FunctionalUnitDto FunctionalUnit);

    FunctionalUnitDto getFunctionalUnitDetails();

    String getCurrentCountOfIdentifiers();

    void setCurrentCountOfIdentifiers(String count);

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
  public FunctionalUnitDetailsPresenter(EventBus eventBus, Display display, Proxy proxy,
      ModalProvider<FunctionalUnitUpdateModalPresenter> functionalUnitModalProvider,
      GenerateIdentifiersModalPresenter generateIdentifiersModalPresenter,
      Provider<AddKeyPairModalPresenter> addKeyPairModalPresenter, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.functionalUnitModalProvider = functionalUnitModalProvider.setContainer(this);
    this.addKeyPairModalPresenter = addKeyPairModalPresenter;
    this.generateIdentifiersModalPresenter = generateIdentifiersModalPresenter;
    this.breadcrumbsHelper = breadcrumbsHelper;
  }

  @TitleFunction
  public String getPageTitle(PlaceRequest request) {
    return functionalUnit.getName();
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    String unitName = request.getParameter("name", "");
    if(!unitName.isEmpty()) retrieveFunctioanUnit(unitName);
  }

  @Override
  public void removeUnit() {
    removeConfirmation = new Runnable() {
      @Override
      public void run() {
        ResponseCodeCallback callbackHandler = new FunctionalUnitDeleteCallback();
        UriBuilder ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName());
        ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).delete()
            .withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
      }
    };
    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithKeys(removeConfirmation, "removeFunctionalUnit", "confirmDeleteFunctionalUnit"));
  }

  @Override
  public void updateUnit() {
    FunctionalUnitUpdateModalPresenter presenter = functionalUnitModalProvider.get();
    presenter.setDialogMode(Mode.UPDATE);
    FunctionalUnitUpdateModalPresenter.Display display = presenter.getView();
    FunctionalUnitDto functionalUnit = getView().getFunctionalUnitDetails();
    display.setName(functionalUnit.getName());
    display.setDescription(functionalUnit.getDescription());
    display.setSelect(functionalUnit.getSelect());
  }

  @Override
  public void exportIdentifiers() {
    String url = "/functional-unit/" + functionalUnit.getName() + "/entities/identifiers";
    getEventBus().fireEvent(new FileDownloadRequestEvent(url));
  }

  @Override
  public void exportIdentifiersMapping() {
    String url = "/functional-unit/" + functionalUnit.getName() + "/entities/csv";
    getEventBus().fireEvent(new FileDownloadRequestEvent(url));
  }

  @Override
  public void importIdentifiersFromFile() {
    getEventBus().fireEvent(new WizardRequiredEvent(IdentifiersImportPresenter.WizardType, functionalUnit));
  }

  @Override
  public void generateIdentifiers() {
    if(generateConfirmation != null) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("IdentifiersGenerationPending").build());
    } else {
      UriBuilder uriBuilder = UriBuilder.create().segment("functional-units", "entities", "table");
      uriBuilder.query("counts","true");
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(uriBuilder.build()).get()
          .withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, TableDto tableDto) {
              showGenerateIdentifiersDialog(tableDto);
            }
          }).send();
    }
  }

  @Override
  public void addCryptographicKey() {
    AddKeyPairModalPresenter popup = addKeyPairModalPresenter.get();
    popup.setFunctionalUnit(functionalUnit);
    addToPopupSlot(popup);
  }

  @Override
  protected void onReset() {
    super.onReset();
    updateCurrentCountOfIdentifiers();
  }

  @Override
  protected void onBind() {
    super.onBind();
    initUiComponents();
    addHandlers();
  }

  private void initUiComponents() {
    getView().setKeyPairs((JsArray<KeyDto>) JsArray.createArray());
    getView().setAvailable(false);
  }

  private void addHandlers() {
    getView().getActionColumn().setActionHandler(new ActionHandler<KeyDto>() {
      @Override
      public void doAction(KeyDto dto, String actionName) {
        if(actionName != null) {
          doActionImpl(dto, actionName);
        }
      }
    });

    EventBus eventBus = getEventBus();

    registerHandler(
        eventBus.addHandler(FunctionalUnitSelectedEvent.getType(), new FunctionalUnitSelectedEvent.Handler() {

          @Override
          public void onFunctionalUnitSelected(FunctionalUnitSelectedEvent event) {
            refreshFunctionalUnitDetails(event.getFunctionalUnit());
          }
        }));

    registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    registerHandler(eventBus.addHandler(FunctionalUnitUpdatedEvent.getType(), new FunctionalUnitUpdatedHandler()));
    registerHandler(eventBus.addHandler(KeyPairCreatedEvent.getType(), new KeyPairCreatedHandler()));
    registerHandler(
        eventBus.addHandler(GenerateIdentifiersConfirmationEvent.getType(), new GenerateIdentifiersHandler()));
  }

  private void updateCurrentCountOfIdentifiers() {
    if(functionalUnit == null) return;

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

    UriBuilder ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "entities", "count");
    countIdentifiersRequest = ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).get()//
        .withCallback(Response.SC_OK, callbackHandler) //
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
        .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void authorize() {
    UriBuilder ub;

    // export identifiers
    ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "entities", "identifiers");
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get()
        .authorize(getView().getDownloadIdentifiersAuthorizer()).send();

    // export identifiers mapping
    ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "entities", "csv");
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get()
        .authorize(getView().getExportIdentifiersAuthorizer()).send();

    // remove
    ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName());
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).delete()
        .authorize(getView().getRemoveFunctionalUnitAuthorizer()).send();

    // generate identifiers
    ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "entities", "identifiers");
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).post()
        .authorize(getView().getGenerateIdentifiersAuthorizer()).send();

    // add identifiers
    ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "entities");
    ResourceAuthorizationRequestBuilderFactory.newBuilder()//
        .forResource(ub.build()).post()//
        .authorize(CascadingAuthorizer.newBuilder().and("/functional-units/entities/table", HttpMethod.GET)//
            .authorize(getView().getImportIdentifiersFromDataAuthorizer()).build())//
        .send();

    // add key pair
    ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "keys");
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).post()
        .authorize(getView().getAddKeyPairAuthorizer()).send();

    // edit
    ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName());
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).put()
        .authorize(getView().getUpdateFunctionalUnitAuthorizer()).send();

    // display key pairs
    ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "keys");
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get()
        .authorize(getView().getListKeyPairsAuthorizer()).send();
  }

  private void authorizeDownloadCertificate(KeyDto dto, HasAuthorization authorizer) {
    UriBuilder ub = UriBuilder.create()
        .segment("functional-unit", functionalUnit.getName(), "key", dto.getAlias(), "certificate");
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).get().authorize(authorizer).send();
  }

  private void authorizeDeleteKeyPair(KeyDto dto, HasAuthorization authorizer) {
    UriBuilder ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "key", dto.getAlias());
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).delete().authorize(authorizer)
        .send();
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
            @Override
            public void run() {
              deleteKeyPair(dto);
            }
          };
          getEventBus().fireEvent(
              ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "deleteKeyPair", "confirmDeleteKeyPair"));
        }
      });
    }
  }

  private void deleteKeyPair(KeyDto dto) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NOT_FOUND) {
          refreshKeyPairs(functionalUnit.getName());
        } else {
          ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).build());
        }
      }

    };
    UriBuilder ub = UriBuilder.create().segment("functional-unit", functionalUnit.getName(), "key", dto.getAlias());
    ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).delete() //
        .withCallback(Response.SC_OK, callbackHandler) //
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
        .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private void downloadCertificate(KeyDto dto) {
    UriBuilder ub = UriBuilder.create()
        .segment("functional-unit", functionalUnit.getName(), "key", dto.getAlias(), "certificate");
    getEventBus().fireEvent(new FileDownloadRequestEvent(ub.build()));
  }

  private void retrieveFunctioanUnit(String unitName) {
    UriBuilder ub = UriBuilder.create().segment("functional-units", "unit", unitName);
    ResourceRequestBuilderFactory.<FunctionalUnitDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new FunctionalUnitFoundCallBack())
        .withCallback(Response.SC_NOT_FOUND, new FunctionalUnitNotFoundCallBack(unitName)).send();
    refreshKeyPairs(unitName);
  }

  private void refreshFunctionalUnitDetails(FunctionalUnitDto functionalUnit) {
    if(functionalUnit == null) {
      getView().setAvailable(false);
    } else {
      retrieveFunctioanUnit(functionalUnit.getName());
    }
  }

  private void refreshKeyPairs(String unitName) {
    UriBuilder ub = UriBuilder.create().segment("functional-unit", unitName, "keys");
    ResourceRequestBuilderFactory.<JsArray<KeyDto>>newBuilder().forResource(ub.build()).get()
        .withCallback(new KeyPairsCallback())
        .withCallback(Response.SC_NOT_FOUND, new FunctionalUnitNotFoundCallBack(unitName)).send();
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void showGenerateIdentifiersDialog(TableDto tableDto) {
    int currentCount = Integer.valueOf(getView().getCurrentCountOfIdentifiers());
    int affectedCount = tableDto.getValueSetCount() - currentCount;

    if(affectedCount == 0) {
      getEventBus().fireEvent(
          NotificationEvent.newBuilder().error("ParticipantIdentifiersAlreadyGenerated").args(functionalUnit.getName())
              .build());
      return;
    }
    generateIdentifiersModalPresenter.setAffectedEntitiesCount(affectedCount);
    addToPopupSlot(generateIdentifiersModalPresenter);
  }

  //
  // Inner classes
  //

  private final class GenerateConfirmationRunnable implements Runnable {

    private final Number size;

    private final boolean allowZeros;

    private final String prefix;

    private GenerateConfirmationRunnable(Number size, boolean allowZeros, String prefix) {
      this.size = size;
      this.allowZeros = allowZeros;
      this.prefix = prefix;
    }

    @Override
    public void run() {
      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_OK) {
            int count = 0;
            try {
              count = Integer.parseInt(response.getText());
            } catch(NumberFormatException ignored) {
            }
            if(count > 0) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().info("IdentifiersGenerationCompleted")
                  .args(functionalUnit.getName(), response.getText()).nonSticky().build());
            } else {
              getEventBus().fireEvent(
                  NotificationEvent.newBuilder().info("NoIdentifiersGenerated").args(functionalUnit.getName())
                      .nonSticky().build());
            }
          } else {
            getEventBus().fireEvent(
                NotificationEvent.newBuilder().error("IdentifiersGenerationFailed").args(functionalUnit.getName())
                    .build());
          }
          generateConfirmation = null;
          onReveal();
        }

      };

      UriBuilder ub = UriBuilder.create()
          .segment("functional-unit", functionalUnit.getName(), "entities", "identifiers")
          .query("size", String.valueOf(size), "zeros", String.valueOf(allowZeros), "prefix", prefix);
      ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).post()//
          .withCallback(Response.SC_OK, callbackHandler) //
          .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler) //
          .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
    }
  }

  private class FunctionalUnitUpdatedHandler implements FunctionalUnitUpdatedEvent.Handler {

    @Override
    public void onFunctionalUnitUpdated(FunctionalUnitUpdatedEvent event) {
      refreshFunctionalUnitDetails(event.getFunctionalUnit());
    }

  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
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

      breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    }
  }

  private class KeyPairsCallback implements ResourceCallback<JsArray<KeyDto>> {

    @Override
    public void onResource(Response response, JsArray<KeyDto> resource) {
      JsArray<KeyDto> keyPairs = resource == null ? (JsArray<KeyDto>) JsArray.createArray() : resource;
      getView().setKeyPairs(keyPairs);
    }

  }

  private class FunctionalUnitNotFoundCallBack implements ResponseCodeCallback {

    private final String functionalUnitName;

    private FunctionalUnitNotFoundCallBack(String functionalUnitName) {
      this.functionalUnitName = functionalUnitName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(
          NotificationEvent.newBuilder().error("FunctionalUnitCannotBeFound").args(functionalUnitName).build());
    }

  }

  private final class KeyPairCreatedHandler implements KeyPairCreatedEvent.Handler {
    @Override
    public void onKeyPairCreated(KeyPairCreatedEvent event) {
      if(event.getFunctionalUnit().getName().equals(functionalUnit.getName())) {
        refreshKeyPairs(functionalUnit.getName());
      }
    }
  }

  private final class GenerateIdentifiersHandler implements GenerateIdentifiersConfirmationEvent.Handler {
    @Override
    public void onGenerateIdentifiersConfirmation(GenerateIdentifiersConfirmationEvent event) {
      generateConfirmation = new GenerateConfirmationRunnable(event.getSize(), event.isAllowZeros(), event.getPrefix());
      generateConfirmation.run();
    }
  }

}
