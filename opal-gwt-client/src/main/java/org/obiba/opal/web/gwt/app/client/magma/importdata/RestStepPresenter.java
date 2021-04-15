/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

public class RestStepPresenter extends PresenterWidget<RestStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  private ViewValidator viewValidator;

  @Inject
  public RestStepPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  protected void onBind() {
    super.onBind();
    viewValidator = new ViewValidator();
  }

  @Override
  public ImportConfig getImportConfig() {
    ImportConfig importConfig = new ImportConfig();
    importConfig.setImportFormat(ImportFormat.REST);
    importConfig.put("url", getView().getUrl().getText())
        .put("remoteDatasource", getView().getRemoteDatasource().getText());

    if (getView().getUseCredentials().getValue())
        importConfig.put("username", getView().getUsername().getText())
        .put("password", getView().getPassword().getText());
    else
        importConfig.put("token", getView().getToken().getText());
    return importConfig;
  }

  @Override
  public boolean validate() {
    return viewValidator.validate();
  }

  public Map<HasType<ControlGroupType>, String> getErrors() {
    return viewValidator.getErrors();
  }

  public interface Display extends View, WizardStepDisplay {

    enum FormField {
      URL,
      USERNAME,
      PASSWORD,
      TOKEN,
      REMOTE_DATESOURCE
    }

    HasText getRemoteDatasource();

    HasText getPassword();

    HasText getUsername();

    HasValue<Boolean> getUseCredentials();

    HasText getToken();

    HasValue<Boolean> getUseToken();

    HasText getUrl();

    HasType<ControlGroupType> getGroupType(String id);
  }

  private final class ViewValidator extends ViewValidationHandler {

    private Map<HasType<ControlGroupType>, String> errors;

    @Override
    protected Set<FieldValidator> getValidators() {
      errors = new HashMap<HasType<ControlGroupType>, String>();

      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new RequiredTextValidator(getView().getUrl(), "OpalURLIsRequired", Display.FormField.URL.name()));
      validators.add(new ConditionalValidator(getView().getUseCredentials(),
          new RequiredTextValidator(getView().getUsername(), "OpalUsernameRequired", Display.FormField.USERNAME.name())));
      validators.add(new ConditionalValidator(getView().getUseCredentials(),
          new RequiredTextValidator(getView().getPassword(), "OpalPasswordRequired", Display.FormField.PASSWORD.name())));
      validators.add(new ConditionalValidator(getView().getUseToken(),
          new RequiredTextValidator(getView().getToken(), "OpalTokenRequired", Display.FormField.TOKEN.name())));
      validators.add(new RequiredTextValidator(getView().getRemoteDatasource(), "RemoteDatasourceIsRequired",
          Display.FormField.REMOTE_DATESOURCE.name()));

      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      errors.put(getView().getGroupType(id), message);
    }

    public Map<HasType<ControlGroupType>, String> getErrors() {
      return errors;
    }
  }
}
