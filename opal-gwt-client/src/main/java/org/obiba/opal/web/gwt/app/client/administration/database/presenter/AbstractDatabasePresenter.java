package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validation.ConstraintViolationErrorsEvent;
import org.obiba.opal.web.gwt.app.client.validation.ConstraintViolationUtils;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;
import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public abstract class AbstractDatabasePresenter<TView extends AbstractDatabasePresenter.Display>
    extends ModalPresenterWidget<TView> implements DatabaseUiHandlers {

  public enum Mode {
    CREATE, UPDATE
  }

  public enum Usage {
    IMPORT(translations.importLabel(), SqlSchema.values()),
    STORAGE(translations.storageLabel(), SqlSchema.HIBERNATE),
    EXPORT(translations.exportLabel(), SqlSchema.HIBERNATE, SqlSchema.JDBC);

    private final String label;

    private final SqlSchema[] supportedSqlSchemas;

    Usage(String label, SqlSchema... supportedSqlSchemas) {
      this.label = label;
      this.supportedSqlSchemas = supportedSqlSchemas;
    }

    public String getLabel() {
      return label;
    }

    public SqlSchema[] getSupportedSqlSchemas() {
      return supportedSqlSchemas;
    }
  }

  public enum SqlSchema {
    HIBERNATE(translations.hibernateDatasourceLabel()), //
    JDBC(translations.jdbcDatasourceLabel()), //
    LIMESURVEY("Limesurvey");

    private final String label;

    SqlSchema(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }

  }

  protected static final Translations translations = GWT.create(Translations.class);

  protected static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  protected Mode dialogMode;

  protected ValidationHandler validationHandler;

  @Inject
  public AbstractDatabasePresenter(EventBus eventBus, TView view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  protected abstract DatabaseDto getDto();

  protected abstract void displayDatabase(DatabaseDto dto);

  protected abstract ViewValidationHandler createValidationHandler();

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);
    validationHandler = createValidationHandler();
  }

  @Override
  public void save() {
    switch(dialogMode) {
      case CREATE:
        createDatabase();
        break;
      case UPDATE:
        updateDatabase();
        break;
    }
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  /**
   * Setup the dialog for creating a method
   */
  void createNewDatabase() {
    setDialogMode(Mode.CREATE);
  }

  /**
   * Setup the dialog for updating an existing method
   *
   * @param dto method to update
   */
  void editDatabase(DatabaseDto dto) {
    setDialogMode(Mode.UPDATE);
    displayDatabase(dto);
  }

  void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  DatabaseDto.Usage parseUsage(@Nonnull Usage usage) {
    switch(usage) {
      case IMPORT:
        return DatabaseDto.Usage.IMPORT;
      case STORAGE:
        return DatabaseDto.Usage.STORAGE;
      case EXPORT:
        return DatabaseDto.Usage.EXPORT;
      default:
        throw new IllegalArgumentException("Unknown database usage: " + usage);
    }
  }

  private void updateDatabase() {
    if(validationHandler.validate()) {
      DatabaseDto dto = getDto();
      ResponseCodeCallback callbackHandler = new CreateOrUpdateCallBack(dto);
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(DatabaseResources.database(dto.getName())) //
          .put() //
          .withResourceBody(DatabaseDto.stringify(dto)) //
          .withCallback(Response.SC_OK, callbackHandler) //
          .withCallback(Response.SC_CREATED, callbackHandler) //
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  private void createDatabase() {
    if(validationHandler.validate()) {
      DatabaseDto dto = getDto();
      ResponseCodeCallback callbackHandler = new CreateOrUpdateCallBack(dto);
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(DatabaseResources.databases()) //
          .post() //
          .withResourceBody(DatabaseDto.stringify(dto)) //
          .withCallback(Response.SC_OK, callbackHandler) //
          .withCallback(Response.SC_CREATED, callbackHandler) //
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  private class CreateOrUpdateCallBack implements ResponseCodeCallback {

    private final DatabaseDto dto;

    private CreateOrUpdateCallBack(DatabaseDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      switch(response.getStatusCode()) {
        case Response.SC_OK:
          getView().hideDialog();
          getEventBus().fireEvent(new DatabaseUpdatedEvent(dto));
          break;
        case Response.SC_CREATED:
          getView().hideDialog();
          getEventBus().fireEvent(new DatabaseCreatedEvent(dto));
          break;
        default:
          ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
          Collection<ConstraintViolationErrorDto> violationDtos = ConstraintViolationUtils.parseErrors(error);
          if(violationDtos.isEmpty()) {
            String errorMessage = translations.userMessageMap().get(error.getStatus());
            getView().showError(errorMessage == null
                ? translationMessages
                .unknownResponse(error.getStatus(), String.valueOf(JsArrays.toList(error.getArgumentsArray())))
                : errorMessage);
          } else {
            ConstraintViolationErrorsEvent.fire(getView().asWidget(), violationDtos);
          }
      }
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DatabaseUiHandlers> {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    void showError(String message);

    HasText getName();

    TakesValue<Usage> getUsage();

    HasChangeHandlers getUsageChangeHandlers();

    HasText getUrl();

    HasText getUsername();

    HasText getPassword();

    HasText getProperties();

    HasValue<Boolean> getDefaultStorage();

    void toggleDefaultStorage(boolean show);

  }

}
