/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateDatasourcePresenter extends WizardPresenterWidget<CreateDatasourcePresenter.Display> implements HasDatasourceForms {

  private final Set<DatasourceFormPresenter> datasourceFormPresenters = new HashSet<DatasourceFormPresenter>();

  public static final WizardType WizardType = new WizardType();

  private static Translations translations = GWT.create(Translations.class);

  public static class Wizard extends WizardProxy<CreateDatasourcePresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<CreateDatasourcePresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  private DatasourceFormPresenter datasourceFormPresenter;

  @Inject
  public CreateDatasourcePresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  public void addDatasourceForm(DatasourceFormPresenter p) {
    datasourceFormPresenters.add(p);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
    getEventBus().fireEvent(new RequestDatasourceFormsEvent(this));
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    datasourceFormPresenters.clear();
  }

  @Override
  protected void onReveal() {

    for(DatasourceFormPresenter formPresenter : datasourceFormPresenters) {
      formPresenter.clearForm();
    }

    updateDatasourceFormDisplay();
  }

  @Override
  protected void onFinish() {
    super.onFinish();
    final String datasourceName = getDatasourceName();
    if(datasourceName.length() == 0) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("DatasourceNameRequired").build());
    } else {
      // check datasource name does not already exist
      ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
        @Override
        public void onResource(Response response, JsArray<DatasourceDto> datasources) {
          if(validateDatasourceNameUnicity(datasourceName, datasources) && validateDatasourceForm()) {
            createDatasource();
          }
        }
      }).send();
    }
  }

  private boolean validateDatasourceNameUnicity(final String datasourceName, JsArray<DatasourceDto> datasources) {
    if(datasources != null) {
      for(int i = 0; i < datasources.length(); i++) {
        DatasourceDto ds = datasources.get(i);
        if(ds.getName().equals(datasourceName)) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error("DatasourceAlreadyExistsWithThisName").build());
          return false;
        }
      }
    }
    return true;
  }

  private boolean validateDatasourceForm() {
    return getDatasourceForm().validateFormData();
  }

  private DatasourceFormPresenter getDatasourceForm() {
    return datasourceFormPresenter;
  }

  private void createDatasource() {
    DatasourceFactoryDto dto = getDatasourceForm().getDatasourceFactory();
    dto.setName(getDatasourceName());
    launchDatasourceCreation(dto);
  }

  @Override
  protected boolean hideOnFinish() {
    return true;
  }

  private void launchDatasourceCreation(final DatasourceFactoryDto dto) {
    ResponseCodeCallback callback = new CreateDatasourceResponseCallback();

    ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasources").post()//
    .withResourceBody(DatasourceFactoryDto.stringify(dto))//
    .withCallback(201, callback).withCallback(400, callback).withCallback(405, callback).withCallback(500, callback).send();
  }

  private void updateDatasourceFormDisplay() {
    datasourceFormPresenter = null;
    for(DatasourceFormPresenter formPresenter : datasourceFormPresenters) {
      if(formPresenter.isForType(getView().getDatasourceType())) {
        datasourceFormPresenter = formPresenter;
        break;
      }
    }
    setInSlot(null, datasourceFormPresenter.getPresenter());
  }

  private String getDatasourceName() {
    return getView().getDatasourceName().getText().trim();
  }

  protected void addEventHandlers() {
    super.registerHandler(getView().addDatasourceTypeChangeHandler(new DatasourceTypeChangeHandler()));
    getView().setDatasourceSelectionTypeValidationHandler(new DatasourceSelectionTypeValidationHandler(getEventBus()));
  }

  final class DatasourceTypeChangeHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent evt) {
      updateDatasourceFormDisplay();
    }
  }

  private final class CreateDatasourceResponseCallback implements ResponseCodeCallback {

    private CreateDatasourceResponseCallback() {
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == 201) {
        DatasourceDto datasourceDto = (DatasourceDto) JsonUtils.unsafeEval(response.getText());
        getEventBus().fireEvent(new DatasourceSelectionChangeEvent(datasourceDto));
      } else if(response.getText() != null && response.getText().length() != 0) {
        ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("DatasourceCreationFailed").args(errorDto.getArgumentsArray()).build());
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(translations.datasourceCreationFailed()).build());
      }
    }
  }

  public interface Display extends WizardView {

    HasText getDatasourceName();

    void setDatasourceSelectionTypeValidationHandler(ValidationHandler handler);

    String getDatasourceType();

    HandlerRegistration addDatasourceTypeChangeHandler(ChangeHandler handler);

  }

  class DatasourceSelectionTypeValidationHandler extends AbstractValidationHandler {

    public DatasourceSelectionTypeValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getView().getDatasourceName(), "DatasourceNameRequired"));
      return validators;
    }
  }

}
