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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateDatasourcePresenter extends WidgetPresenter<CreateDatasourcePresenter.Display> {
  //
  // Instance Variables
  //

  @Inject
  private Provider<NavigatorPresenter> navigatorPresenter;

  @Inject
  private Provider<CreateDatasourceConclusionStepPresenter> createDatasourceConclusionStepPresenter;

  @Inject
  private HibernateDatasourceFormPresenter hibernateDatasourceFormPresenter;

  @Inject
  private ExcelDatasourceFormPresenter excelDatasourceFormPresenter;

  @Inject
  private FsDatasourceFormPresenter fsDatasourceFormPresenter;

  @Inject
  private JdbcDatasourceFormPresenter jdbcDatasourceFormPresenter;

  @Inject
  private CsvDatasourceFormPresenter csvDatasourceFormPresenter;

  private Set<DatasourceFormPresenter> datasourceFormPresenters = new HashSet<DatasourceFormPresenter>();

  //
  // Constructors
  //

  @Inject
  public CreateDatasourcePresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
    registerDatasourceFormPresenters();
  }

  @Override
  protected void onUnbind() {
    datasourceFormPresenters.clear();
  }

  private void registerDatasourceFormPresenters() {
    // FIXME: Is there a way of registering these automatically ? Injecting the set fails.
    datasourceFormPresenters.add(hibernateDatasourceFormPresenter);
    datasourceFormPresenters.add(excelDatasourceFormPresenter);
    datasourceFormPresenters.add(fsDatasourceFormPresenter);
    datasourceFormPresenters.add(jdbcDatasourceFormPresenter);
    datasourceFormPresenters.add(csvDatasourceFormPresenter);
    updateDatasourceFormDisplay();
  }

  private void updateDatasourceFormDisplay() {
    // GWT.log(getDisplay().getDatasourceType());
    for(DatasourceFormPresenter formPresenter : datasourceFormPresenters) {
      if(formPresenter.isForType(getDisplay().getDatasourceType())) {
        getDisplay().setDatasourceForm(formPresenter);
        return;
      }
    }
    getDisplay().setDatasourceForm(null);
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getDisplay().addFinishClickHandler(new FinishClickHandler()));
    super.registerHandler(getDisplay().addCreateClickHandler(new CreateClickHandler()));
    super.registerHandler(getDisplay().addDatasourceTypeChangeHandler(new DatasourceTypeChangeHandler()));
    getDisplay().setDatasourceSelectionTypeValidationHandler(new DatasourceSelectionTypeValidationHandler(eventBus));
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  //
  // Inner Classes / Interfaces
  //

  final class DatasourceTypeChangeHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent evt) {
      updateDatasourceFormDisplay();
    }
  }

  public interface Display extends WidgetDisplay {

    HasText getDatasourceName();

    void setDatasourceSelectionTypeValidationHandler(ValidationHandler handler);

    String getDatasourceType();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addFinishClickHandler(ClickHandler handler);

    HandlerRegistration addCreateClickHandler(ClickHandler handler);

    HandlerRegistration addDatasourceTypeChangeHandler(ChangeHandler handler);

    void setDatasourceForm(DatasourceFormPresenter formPresenter);

    DatasourceFormPresenter getDatasourceForm();

    void showDialog();

    void hideDialog();

    void setConclusion(CreateDatasourceConclusionStepPresenter presenter);
  }

  class DatasourceSelectionTypeValidationHandler extends AbstractValidationHandler {

    public DatasourceSelectionTypeValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getDisplay().getDatasourceName(), "DatasourceNameRequired"));
      return validators;
    }
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getDisplay().hideDialog();
    }
  }

  class FinishClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new WorkbenchChangeEvent(navigatorPresenter.get()));
      getDisplay().hideDialog();
    }
  }

  class CreateClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      final String datasourceName = getDatasourceName();
      if(datasourceName.length() == 0) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "DatasourceNameRequired", null));
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

    private String getDatasourceName() {
      return getDisplay().getDatasourceName().getText().trim();
    }

    private boolean validateDatasourceNameUnicity(final String datasourceName, JsArray<DatasourceDto> datasources) {
      if(datasources != null) {
        for(int i = 0; i < datasources.length(); i++) {
          DatasourceDto ds = datasources.get(i);
          if(ds.getName().equals(datasourceName)) {
            eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "DatasourceAlreadyExistsWithThisName", null));
            return false;
          }
        }
      }
      return true;
    }

    private boolean validateDatasourceForm() {
      return getDisplay().getDatasourceForm().validateFormData();
    }

    private void createDatasource() {
      DatasourceFactoryDto dto = getDisplay().getDatasourceForm().getDatasourceFactory();
      dto.setName(getDatasourceName());

      CreateDatasourceConclusionStepPresenter presenter = createDatasourceConclusionStepPresenter.get();
      presenter.setDatasourceFactory(dto);
      getDisplay().setConclusion(presenter);
    }
  }
}
