/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.event.IdentifiersTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class IdentifiersTablePresenter extends PresenterWidget<IdentifiersTablePresenter.Display>
    implements IdentifiersTableUiHandlers {

  private final ModalProvider<ImportSystemIdentifiersModalPresenter> importSystemIdentifiersModalProvider;

  private final ModalProvider<CopySystemIdentifiersModalPresenter> copySystemIdentifiersModalProvider;

  private final ModalProvider<ImportIdentifiersMappingModalPresenter> importIdentifiersMappingModalProvider;

  private final ModalProvider<IdentifiersMappingModalPresenter> identifiersMappingModalProvider;

  private final ModalProvider<GenerateIdentifiersModalPresenter> generateIdentifiersModalProvider;

  private TableDto table;

  private Runnable removeConfirmation;

  private final TranslationMessages translationMessages;

  @Inject
  public IdentifiersTablePresenter(EventBus eventBus, Display view,
      ModalProvider<ImportSystemIdentifiersModalPresenter> importSystemIdentifiersModalProvider,
      ModalProvider<CopySystemIdentifiersModalPresenter> copySystemIdentifiersModalProvider,
      ModalProvider<ImportIdentifiersMappingModalPresenter> importIdentifiersMappingModalProvider,
      ModalProvider<IdentifiersMappingModalPresenter> identifiersMappingModalProvider,
      ModalProvider<GenerateIdentifiersModalPresenter> generateIdentifiersModalProvider,
      TranslationMessages translationMessages) {
    super(eventBus, view);
    this.translationMessages = translationMessages;
    this.importSystemIdentifiersModalProvider = importSystemIdentifiersModalProvider.setContainer(this);
    this.copySystemIdentifiersModalProvider = copySystemIdentifiersModalProvider.setContainer(this);
    this.importIdentifiersMappingModalProvider = importIdentifiersMappingModalProvider.setContainer(this);
    this.identifiersMappingModalProvider = identifiersMappingModalProvider.setContainer(this);
    this.generateIdentifiersModalProvider = generateIdentifiersModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void showIdentifiersTable(@SuppressWarnings("ParameterHidesMemberVariable") TableDto table) {
    this.table = table;
    getView().showIdentifiersTable(table);
    String uri = UriBuilders.IDENTIFIERS_TABLE_VARIABLES.create().build(table.getName());
    ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
          @Override
          public void onResource(Response response, JsArray<VariableDto> resource) {
            getView().setVariables(JsArrays.toSafeArray(resource));
          }
        }) //
        .get().send();
  }

  @Override
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler());
  }

  @Override
  public void onIdentifiersRequest(TableDto identifiersTable, String select, final int offset, int limit) {
    String uri = UriBuilders.IDENTIFIERS_TABLE_VALUESETS.create().query("select", select).query("offset", "" + offset)
        .query("limit", "" + limit).build(identifiersTable.getName());
    ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<ValueSetsDto>() {
          @Override
          public void onResource(Response response, ValueSetsDto resource) {
            getView().setValueSets(offset, resource);
          }
        }) //
        .get().send();
  }

  @Override
  public void onDeleteIdentifiersTable() {
    removeConfirmation = new RemoveIdentifiersTableRunnable();

    ConfirmationRequiredEvent event = ConfirmationRequiredEvent
        .createWithMessages(removeConfirmation, translationMessages.removeIdentifiersTable(),
            translationMessages.confirmRemoveIdentifiersTable());

    fireEvent(event);
  }

  @Override
  public void onImportSystemIdentifiers() {
    if(table != null) {
      ImportSystemIdentifiersModalPresenter p = importSystemIdentifiersModalProvider.get();
      p.initialize(table);
    }
  }

  @Override
  public void onCopySystemIdentifiers() {
    if(table != null) {
      CopySystemIdentifiersModalPresenter p = copySystemIdentifiersModalProvider.get();
      p.initialize(table);
    }
  }

  @Override
  public void onImportIdentifiersMapping() {
    if(table != null) {
      ImportIdentifiersMappingModalPresenter p = importIdentifiersMappingModalProvider.get();
      p.initialize(table);
    }
  }

  @Override
  public void onAddIdentifiersMapping() {
    if(table != null) {
      IdentifiersMappingModalPresenter p = identifiersMappingModalProvider.get();
      p.initialize(table);
    }
  }

  @Override
  public void onEditIdentifiersMapping(VariableDto variable) {
    IdentifiersMappingModalPresenter p = identifiersMappingModalProvider.get();
    p.initialize(variable, table);
  }

  @Override
  public void onDeleteIdentifiersMapping(VariableDto variable) {
    removeConfirmation = new RemoveIdentifiersMappingRunnable(variable);

    ConfirmationRequiredEvent event = ConfirmationRequiredEvent
        .createWithMessages(removeConfirmation, translationMessages.removeIdentifiersMapping(),
            translationMessages.confirmRemoveIdentifiersMapping());

    fireEvent(event);
  }

  @Override
  public void onGenerateIdentifiersMapping(VariableDto variable) {
    GenerateIdentifiersModalPresenter p = generateIdentifiersModalProvider.get();
    p.initialize(variable, table);
  }

  @Override
  public void onDownloadIdentifiers(VariableDto variable) {
    fireEvent(new FileDownloadRequestEvent(
        UriBuilder.create().segment("identifiers", "mapping", "{}", "_export").query("type", variable.getEntityType())
            .build(variable.getName())));
  }

  @Override
  public void onDownloadIdentifiers() {
    fireEvent(new FileDownloadRequestEvent(
        UriBuilder.create().segment("identifiers", "mappings", "_export").query("type", table.getEntityType())
            .build()));
  }

  //
  // Private methods
  //

  private class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }

  private class RemoveIdentifiersTableRunnable implements Runnable {
    @Override
    public void run() {
      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            fireEvent(new IdentifiersTableSelectionEvent.Builder().dto(table).build());
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      String uri = UriBuilders.IDENTIFIERS_TABLE.create().build(table.getName());
      ResourceRequestBuilderFactory.newBuilder().forResource(uri).delete().withCallback(SC_OK, callbackHandler)
          .withCallback(SC_FORBIDDEN, callbackHandler).withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler)
          .withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

  }

  private class RemoveIdentifiersMappingRunnable implements Runnable {

    private final VariableDto variable;

    private RemoveIdentifiersMappingRunnable(VariableDto variable) {
      this.variable = variable;
    }

    @Override
    public void run() {
      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            fireEvent(new IdentifiersTableSelectionEvent.Builder().dto(table).build());
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      String uri = UriBuilders.IDENTIFIERS_TABLE_VARIABLE.create().build(table.getName(), variable.getName());
      ResourceRequestBuilderFactory.newBuilder().forResource(uri).delete().withCallback(SC_OK, callbackHandler)
          .withCallback(SC_FORBIDDEN, callbackHandler).withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler)
          .withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

  }

  public interface Display extends View, HasUiHandlers<IdentifiersTableUiHandlers> {

    void showIdentifiersTable(TableDto tableDto);

    void setVariables(JsArray<VariableDto> variables);

    void setValueSets(int offset, ValueSetsDto valueSets);

  }

}
