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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.validator.RequiredOptionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidatablePresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.JdbcDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.JdbcDatasourceSettingsDto;
import org.obiba.opal.web.model.client.opal.JdbcDriverDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;

public class JdbcDatasourceFormPresenter extends ValidatablePresenterWidget<JdbcDatasourceFormPresenter.Display> implements DatasourceFormPresenter {

  public static class Subscriber extends DatasourceFormPresenterSubscriber {

    @Inject
    public Subscriber(com.google.gwt.event.shared.EventBus eventBus, JdbcDatasourceFormPresenter presenter) {
      super(eventBus, presenter);
    }

  }

  @SuppressWarnings("unchecked")
  @Inject
  public JdbcDatasourceFormPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);

    addValidator(new RequiredTextValidator(getView().getUrl(), "UrlRequired"));
    addValidator(new RequiredTextValidator(getView().getUsername(), "UsernameRequired"));
    addValidator(new RequiredTextValidator(getView().getPassword(), "PasswordRequired"));
    addValidator(new RequiredOptionValidator(RequiredOptionValidator.asSet(getView().getUseMetadataTablesOption(), getView().getDoNotUseMetadataTablesOption()), "MustIndicateWhetherJdbcDatasourceShouldUseMetadataTables"));
  }

  @Override
  public PresenterWidget<? extends org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceFormPresenter.Display> getPresenter() {
    return this;
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    ResourceRequestBuilderFactory.<JsArray<JdbcDriverDto>> newBuilder().forResource("/system/jdbcDrivers").get().withCallback(new ResourceCallback<JsArray<JdbcDriverDto>>() {

      @Override
      public void onResource(Response response, JsArray<JdbcDriverDto> drivers) {
        List<JdbcDriverDto> driverList = new ArrayList<JdbcDriverDto>();
        for(int i = 0; i < drivers.length(); i++) {
          driverList.add(drivers.get(i));
        }
        getView().setJdbcDrivers(driverList);
      }
    }).send();
  }

  public DatasourceFactoryDto getDatasourceFactory() {
    JdbcDatasourceFactoryDto extensionDto = JdbcDatasourceFactoryDto.create();
    extensionDto.setDriver(getView().getDriver().getText());
    extensionDto.setUrl(getView().getUrl().getText());
    extensionDto.setUsername(getView().getUsername().getText());
    extensionDto.setPassword(getView().getPassword().getText());
    extensionDto.setSettings(getSettings());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(JdbcDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  public boolean isForType(String type) {
    return type.equalsIgnoreCase("jdbc");
  }

  //
  // Methods
  //

  private JdbcDatasourceSettingsDto getSettings() {
    JdbcDatasourceSettingsDto settingsDto = JdbcDatasourceSettingsDto.create();
    settingsDto.setDefaultEntityType("Participant");
    settingsDto.setUseMetadataTables(getView().getUseMetadataTablesOption().getValue());

    if(getView().getDefaultCreatedTimestampColumnName().getText().trim().length() != 0) {
      settingsDto.setDefaultCreatedTimestampColumnName(getView().getDefaultCreatedTimestampColumnName().getText());
    }

    if(getView().getDefaultUpdatedTimestampColumnName().getText().trim().length() != 0) {
      settingsDto.setDefaultUpdatedTimestampColumnName(getView().getDefaultUpdatedTimestampColumnName().getText());
    }

    return settingsDto;
  }

  //
  // Interfaces and Inner Classes
  //

  public interface Display extends DatasourceFormPresenter.Display {

    HasText getDriver();

    HasText getUrl();

    HasText getUsername();

    HasText getPassword();

    HasValue<Boolean> getUseMetadataTablesOption();

    HasValue<Boolean> getDoNotUseMetadataTablesOption();

    HasText getDefaultCreatedTimestampColumnName();

    HasText getDefaultUpdatedTimestampColumnName();

    void setJdbcDrivers(List<JdbcDriverDto> drivers);
  }

  @Override
  public boolean validateFormData() {
    return validate();
  }

  @Override
  public void clearForm() {
    getView().getUrl().setText("");
    getView().getUsername().setText("");
    getView().getPassword().setText("");
    getView().getDefaultCreatedTimestampColumnName().setText("");
    getView().getDefaultUpdatedTimestampColumnName().setText("");
    getView().getDoNotUseMetadataTablesOption().setValue(true);
    getView().getUseMetadataTablesOption().setValue(false);
    getView().getDriver().setText("");
  }
}
