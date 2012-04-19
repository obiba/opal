/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter;

import org.obiba.opal.web.gwt.app.client.validator.RequiredOptionValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidatablePresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.JdbcDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.JdbcDatasourceSettingsDto;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;

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
    public Subscriber(EventBus eventBus, JdbcDatasourceFormPresenter presenter) {
      super(eventBus, presenter);
    }

  }

  @SuppressWarnings("unchecked")
  @Inject
  public JdbcDatasourceFormPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);

    addValidator(new RequiredOptionValidator(RequiredOptionValidator.asSet(getView().getUseMetadataTablesOption(), getView().getDoNotUseMetadataTablesOption()), "MustIndicateWhetherJdbcDatasourceShouldUseMetadataTables"));
  }

  @Override
  public PresenterWidget<? extends DatasourceFormPresenter.Display> getPresenter() {
    return this;
  }

  @Override
  protected void onReveal() {
    ResourceRequestBuilderFactory.<JsArray<JdbcDataSourceDto>> newBuilder().forResource("/jdbc/databases").withCallback(new ResourceCallback<JsArray<JdbcDataSourceDto>>() {

      @Override
      public void onResource(Response response, JsArray<JdbcDataSourceDto> resource) {
        getView().setDatabases(resource);
      }
    }).get().send();
  }

  @Override
  public boolean validateFormData() {
    return validate();
  }

  @Override
  public DatasourceFactoryDto getDatasourceFactory() {
    JdbcDatasourceFactoryDto extensionDto = JdbcDatasourceFactoryDto.create();
    extensionDto.setDatabase(getView().getSelectedDatabase());
    extensionDto.setSettings(getSettings());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(JdbcDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  @Override
  public boolean isForType(String type) {
    return type.equalsIgnoreCase("jdbc");
  }

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

    void setDatabases(JsArray<JdbcDataSourceDto> resource);

    String getSelectedDatabase();

    HasValue<Boolean> getUseMetadataTablesOption();

    HasValue<Boolean> getDoNotUseMetadataTablesOption();

    HasText getDefaultCreatedTimestampColumnName();

    HasText getDefaultUpdatedTimestampColumnName();

  }

  @Override
  public void clearForm() {
    getView().getDefaultCreatedTimestampColumnName().setText("");
    getView().getDefaultUpdatedTimestampColumnName().setText("");
    getView().getDoNotUseMetadataTablesOption().setValue(true);
    getView().getUseMetadataTablesOption().setValue(false);
  }
}
