/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredValueValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.MongoDbSettingsDto;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.administration.database.presenter.MongoDatabasePresenter.Display.FormField;

public class MongoDatabasePresenter extends AbstractDatabasePresenter<MongoDatabasePresenter.Display> {

  @Inject
  public MongoDatabasePresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().getUsageChangeHandlers().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        getView().toggleDefaultStorage(getView().getUsage().getValue() == Usage.STORAGE);
      }
    });
  }

  @Override
  protected void displayDatabase(DatabaseDto dto) {
    getView().getName().setText(dto.getName());
    getView().getUsage().setValue(Usage.valueOf(dto.getUsage().getName()));
    getView().getDefaultStorage().setValue(dto.getDefaultStorage());

    MongoDbSettingsDto mongoDto = MongoDbSettingsDto.create();
    getView().getUrl().setText(mongoDto.getUrl());
    getView().getUsername().setText(mongoDto.getUsername());
    getView().getPassword().setText(mongoDto.getPassword());
    getView().getProperties().setText(mongoDto.getProperties());
  }

  @Override
  protected void hideNonEditableIdentifiersDatabaseFields() {
    getView().getNameGroupVisibility().setVisible(false);
    getView().getUsageGroupVisibility().setVisible(false);
    getView().getDefaultStorageGroupVisibility().setVisible(false);
  }

  @Override
  protected DatabaseDto getDto() {
    DatabaseDto dto = DatabaseDto.create();
    MongoDbSettingsDto mongoDto = MongoDbSettingsDto.create();

    dto.setUsedForIdentifiers(usedForIdentifiers);
    dto.setName(getView().getName().getText());
    dto.setUsage(parseUsage(getView().getUsage().getValue()));
    dto.setDefaultStorage(getView().getDefaultStorage().getValue());

    mongoDto.setUrl(getView().getUrl().getText());
    mongoDto.setUsername(getView().getUsername().getText());
    mongoDto.setPassword(getView().getPassword().getText());
    mongoDto.setProperties(getView().getProperties().getText());

    dto.setMongoDbSettings(mongoDto);
    return dto;
  }

  @Override
  protected ViewValidationHandler createValidationHandler() {
    return new MongoDatabaseValidationHandler();
  }

  private class MongoDatabaseValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired", FormField.NAME.name()));
        validators.add(new RequiredTextValidator(getView().getUrl(), "UrlIsRequired", FormField.URL.name()));
        validators.add(new RequiredValueValidator(getView().getUsage(), "UsageIsRequired", FormField.USAGE.name()));
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(FormField.valueOf(id), message);
    }

  }

  public interface Display extends AbstractDatabasePresenter.Display {

    enum FormField {
      NAME,
      URL,
      USAGE,
    }

    void showError(@Nullable FormField formField, String message);

    HasVisibility getNameGroupVisibility();

    HasVisibility getDefaultStorageGroupVisibility();

  }

}
