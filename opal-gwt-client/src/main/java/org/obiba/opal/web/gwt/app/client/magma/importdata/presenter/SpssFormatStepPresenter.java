/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;
import org.obiba.opal.web.gwt.app.client.presenter.CharacterSetDisplay;
import org.obiba.opal.web.gwt.app.client.support.LanguageLocale;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class SpssFormatStepPresenter extends PresenterWidget<SpssFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  private final FileSelectionPresenter spssFileSelectionPresenter;

  private ViewValidator viewValidator;

  @Inject
  public SpssFormatStepPresenter(EventBus eventBus, Display display,
      FileSelectionPresenter spssFileSelectionPresenter) {
    super(eventBus, display);
    this.spssFileSelectionPresenter = spssFileSelectionPresenter;
  }

  @Override
  protected void onBind() {
    spssFileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE_OR_FOLDER);
    spssFileSelectionPresenter.bind();
    getView().setSpssFileSelectorWidgetDisplay(spssFileSelectionPresenter.getView());
    setDefaultCharset();
    viewValidator = new ViewValidator();
  }

  @Override
  public ImportConfig getImportConfig() {
    ImportConfig importData = new ImportConfig();
    importData.setFormat(ImportFormat.SPSS);
    importData.setFile(getView().getSelectedFile());
    importData.setCharacterSet(getView().getCharsetText().getText());
    importData.setDestinationEntityType(getView().getEntityType().getText());
    importData.setLocale(getView().getLocale());

    return importData;
  }

  @Override
  public boolean validate() {
    return viewValidator.validate();
  }

  public Map<HasType<ControlGroupType>, String> getErrors() {
    return viewValidator.getErrors();
  }

  public interface Display extends View, WizardStepDisplay, CharacterSetDisplay {

    enum FormField {
      FILE,
      LOCALE
    }

    void setSpssFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    String getSelectedFile();

    HasText getEntityType();

    String getLocale();

    HasType<ControlGroupType> getGroupType(String id);
  }

  private void setDefaultCharset() {
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder() //
        .forResource(UriBuilders.SYSTEM_CHARSET.create().build()) //
        .get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setDefaultCharset(response.getText());
          }
        }, Response.SC_OK).send();
  }

  private final class ViewValidator extends ViewValidationHandler {

    private Map<HasType<ControlGroupType>, String> errors;

    @Override
    protected Set<FieldValidator> getValidators() {
      errors = new HashMap<HasType<ControlGroupType>, String>();

      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new ConditionValidator(fileExtensionCondition(getView().getSelectedFile()), "SpssFileRequired",
          Display.FormField.FILE.name()));

      ConditionValidator localeValidator = new ConditionValidator(localeCondition(getView().getLocale()),
          "InvalidLocaleName", Display.FormField.LOCALE.name());
      localeValidator.setArgs(Lists.newArrayList(getView().getLocale()));
      validators.add(localeValidator);

      return validators;
    }

    private HasValue<Boolean> fileExtensionCondition(final String selectedFile) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return selectedFile.toLowerCase().endsWith(".sav");
        }
      };
    }

    private HasValue<Boolean> localeCondition(final String locale) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return LanguageLocale.isValid(locale);
        }
      };
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
