/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project.genotypes;

import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfFileUploadRequestEvent;
import org.obiba.opal.web.gwt.app.client.validator.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ProjectImportVcfFileModalPresenter extends ModalPresenterWidget<ProjectImportVcfFileModalPresenter.Display>
    implements ProjectImportVcfFileModalUiHandlers {


  private static Logger logger = Logger.getLogger("ProjectImportVcfFileModalPresenter");

  private final ValidationHandler validationHandler;

  private final FileSelectionPresenter fileSelectionPresenter;

  @Inject
  public ProjectImportVcfFileModalPresenter(Display display,
                                            EventBus eventBus,
                                            FileSelectionPresenter fileSelection) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    fileSelectionPresenter = fileSelection;
    validationHandler = new ModalValidationHandler();

  }

  @Override
  protected void onBind() {
    fileSelectionPresenter.bind();
    fileSelectionPresenter.setFileSelectionType(FileSelectorPresenter.FileSelectionType.FILE);
    getView().setFileSelectorWidgetDisplay(fileSelectionPresenter.getView());
  }

  @Override
  protected void onUnbind() {
    fileSelectionPresenter.unbind();
  }

  @Override
  public void onImport() {
    getView().clearErrors();
    if(validationHandler.validate()) {
      fireEvent(new VcfFileUploadRequestEvent.Builder(fileSelectionPresenter.getSelectedFile()).build());
      getView().hideDialog();
    }
  }

  private class ModalValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
        validators = new LinkedHashSet<>();
        validators.add(new ConditionValidator(vcfFileExtensionCondition(fileSelectionPresenter.getSelectedFile()),
                "VCFFileRequired"));
      return validators;
    }

    private HasValue<Boolean> vcfFileExtensionCondition(final String selectedFile) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return selectedFile.toLowerCase().endsWith(".vcf") || selectedFile.toLowerCase().endsWith(".vcf.gz")
              || selectedFile.toLowerCase().endsWith(".bcf") || selectedFile.toLowerCase().endsWith(".bcf.gz");
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(message);
    }
  }

  public interface Display extends PopupView, HasUiHandlers<ProjectImportVcfFileModalUiHandlers> {

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    void clearErrors();

    void showError(String message);

    void hideDialog();
  }

}
