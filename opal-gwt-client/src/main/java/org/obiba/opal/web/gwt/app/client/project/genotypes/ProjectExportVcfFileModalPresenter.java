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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfFileExportRequestEvent;
import org.obiba.opal.web.gwt.app.client.validator.*;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.ExportVCFCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.VCFSummaryDto;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ProjectExportVcfFileModalPresenter extends ModalPresenterWidget<ProjectExportVcfFileModalPresenter.Display>
    implements ProjectExportVcfFileModalUiHandlers {

  private final ValidationHandler validationHandler;

  private final FileSelectionPresenter fileSelectionPresenter;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final RequestCredentials credentials;

  List<VCFSummaryDto> selectedVCFs = Lists.newArrayList();

  private static Logger logger = Logger.getLogger("ProjectExportVcfFileModalPresenter");

  @Inject
  public ProjectExportVcfFileModalPresenter(Display display,
                                            EventBus eventBus, Translations translations, TranslationMessages translationMessages,
                                            FileSelectionPresenter fileSelection, RequestCredentials credentials) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    fileSelectionPresenter = fileSelection;
    validationHandler = new ModalValidationHandler();
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.credentials = credentials;
  }

  @Override
  protected void onBind() {
    fileSelectionPresenter.bind();
    fileSelectionPresenter.setFileSelectionType(FileSelectorPresenter.FileSelectionType.FOLDER);
    fileSelectionPresenter.getView().setFile("/home/" + credentials.getUsername() + "/export");
    getView().setFileSelectorWidgetDisplay(fileSelectionPresenter.getView());
  }

  @Override
  protected void onUnbind() {
    fileSelectionPresenter.unbind();
  }

  @Override
  public void onExport() {
    getView().clearErrors();
    if (validationHandler.validate()) {
      ExportVCFCommandOptionsDto dto = ExportVCFCommandOptionsDto.create();
      JsArrayString names = JsArrayString.createArray().cast();
      if (selectedVCFs != null || selectedVCFs.size() > 0) {
        for (VCFSummaryDto summaryDto: selectedVCFs) {
          names.push(summaryDto.getName());
        }
      }

      dto.setNamesArray(names);
      dto.setTable(getView().getParticipantsFilterTable().getText());
      dto.setParticipantIdentifiersMapping(getView().getParticipantIdentifiersMapping().getText());
      dto.setCaseControl(getView().hasCaseControls());
      dto.setDestination(fileSelectionPresenter.getSelectedFile());

      fireEvent(new VcfFileExportRequestEvent(dto));
      getView().hideDialog();
    }
  }

  public void setExportVCFs(List<VCFSummaryDto> vcfs, boolean allVCFs) {
    selectedVCFs = vcfs;
    if (!allVCFs) {
      getView().showExportNAlert(translationMessages.exportNVCFs(vcfs.size()));
    } else {
      getView().showExportNAlert(translations.exportAllVCFs());
    }
  }

  public void setParticipantTables(JsArray<TableDto> participantTables) {
    getView().setParticipants(participantTables);
  }

  public void setParticipantIdentifiersMappingList(List<String> participantIdentifiersMappingList) {
    getView().setParticipantIdentifiersMappingList(participantIdentifiersMappingList);
  }

  public void showMappingDependantContent(boolean show) {
    getView().showMappingDependant(show);
  }

  private class ModalValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<>();
        validators.add(new ConditionValidator(vcfFileExtensionCondition(fileSelectionPresenter.getSelectedFile()),
                "VCFExportDirectoryRequired",
                Display.FormField.DIRECTORY.name()));
      }
      return validators;
    }

    private HasValue<Boolean> vcfFileExtensionCondition(final String selectedFile) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !Strings.isNullOrEmpty(selectedFile);
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }

  }

  public interface Display extends PopupView, HasUiHandlers<ProjectExportVcfFileModalUiHandlers> {

    enum FormField {
      DIRECTORY,
      NAME
    }

    void setParticipants(JsArray<TableDto> tables);

    void setParticipantIdentifiersMappingList(List<String> tables);

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    void clearErrors();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();

    void showExportNAlert(String message);

    HasText getParticipantsFilterTable();

    HasText getParticipantIdentifiersMapping();

    void showMappingDependant(boolean show);

    boolean hasCaseControls();
  }

}
