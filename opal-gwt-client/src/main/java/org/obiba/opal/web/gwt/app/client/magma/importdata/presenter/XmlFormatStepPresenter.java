/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

public class XmlFormatStepPresenter extends PresenterWidget<XmlFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  private final FileSelectionPresenter xmlFileSelectionPresenter;

  private ViewValidator viewValidator;

  @Inject
  public XmlFormatStepPresenter(EventBus eventBus, Display display, FileSelectionPresenter xmlFileSelectionPresenter) {
    super(eventBus, display);
    this.xmlFileSelectionPresenter = xmlFileSelectionPresenter;
  }

  @Override
  protected void onBind() {
    xmlFileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE_OR_FOLDER);
    xmlFileSelectionPresenter.bind();
    getView().setXmlFileSelectorWidgetDisplay(xmlFileSelectionPresenter.getView());
    viewValidator = new ViewValidator();
  }

  @Override
  public ImportConfig getImportConfig() {
    ImportConfig importConfig = new ImportConfig();
    importConfig.setFormat(ImportFormat.XML);
    importConfig.setXmlFile(getView().getSelectedFile());

    return importConfig;
  }

  @Override
  public boolean validate() {
    return viewValidator.validate();
  }

  public Map<HasType<ControlGroupType>, String> getErrors() {
    return viewValidator.getErrors();
  }

  //
  // Interfaces
  //

  public interface Display extends View, WizardStepDisplay {

    enum FormField {
      FILE
    }

    void setXmlFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    String getSelectedFile();

    HasType<ControlGroupType> getGroupType(String id);
  }

  private final class ViewValidator extends ViewValidationHandler {

    private Map<HasType<ControlGroupType>, String> errors;

    @Override
    protected Set<FieldValidator> getValidators() {
      errors = new HashMap<>();

      Set<FieldValidator> validators = new LinkedHashSet<>();

      validators.add(
          new ConditionValidator(fileExtensionCondition(xmlFileSelectionPresenter.getSelectedFile()), "ZipFileRequired",
              Display.FormField.FILE.name()));

      return validators;
    }

    private HasValue<Boolean> fileExtensionCondition(final String selectedFile) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return selectedFile.toLowerCase().endsWith(".zip");
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
