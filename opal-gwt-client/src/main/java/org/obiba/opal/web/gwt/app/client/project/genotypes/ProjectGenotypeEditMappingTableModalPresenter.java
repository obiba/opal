/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project.genotypes;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.GenotypesMappingEditRequestEvent;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.VCFSamplesMappingDto;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProjectGenotypeEditMappingTableModalPresenter extends ModalPresenterWidget<ProjectGenotypeEditMappingTableModalPresenter.Display>
    implements ProjectGenotypeEditMappingTableModalUiHandlers {

  private final ValidationHandler validationHandler;

  private String projectName;

  @Inject
  public ProjectGenotypeEditMappingTableModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    validationHandler = new ModalValidationHandler();
  }

  @Override
  protected void onBind() {
  }

  public void setMappingTables(JsArray<TableDto> availableMappingTables) {
    getView().setAvailableMappingTables(availableMappingTables);
  }

  public void setGenotypesMapping(VCFSamplesMappingDto currentGenotypesMapping) {
    projectName = currentGenotypesMapping.getProjectName();
    getView().setVCFSamplesMappingDto(currentGenotypesMapping);
  }

  @Override
  public void onSaveEdit() {
    getView().clearErrors();
    if(validationHandler.validate()) {
      VCFSamplesMappingDto genotypeDto = VCFSamplesMappingDto.create();
      genotypeDto.setParticipantIdVariable(getView().getParticipantIdVariable().getText());
      genotypeDto.setSampleIdVariable(getView().getSampleIdVariable().getText());
      genotypeDto.setSampleRoleVariable(getView().getSampleRoleVariable().getText());
      genotypeDto.setTableName(getView().getMappingTable().getText());
      genotypeDto.setProjectName(projectName);
      fireEvent(new GenotypesMappingEditRequestEvent(genotypeDto));

      getView().hideDialog();
    }
  }

  private class ModalValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<>();
        validators.add(new RequiredTextValidator(getView().getMappingTable(),
            "MappingTableIsRequired", Display.FormField.MAPPING_TABLE.name()));
        validators.add(new RequiredTextValidator(getView().getParticipantIdVariable(),
            "ParticipantIdVariableIsRequired", Display.FormField.PARTICIPANT_ID_VARIABLE.name()));
        validators.add(new RequiredTextValidator(getView().getSampleIdVariable(),
            "SampleIdVariableIsRequired", Display.FormField.SAMPLE_ID_VARIABLE.name()));
        validators.add(new RequiredTextValidator(getView().getSampleRoleVariable(),
            "SampleRoleVariableIsRequired", Display.FormField.SAMPLE_ROLE_VARIABLE.name()));
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }

  }

  public interface Display extends PopupView, HasUiHandlers<ProjectGenotypeEditMappingTableModalUiHandlers> {

    enum FormField {
      MAPPING_TABLE,
      PARTICIPANT_ID_VARIABLE,
      SAMPLE_ID_VARIABLE,
      SAMPLE_ROLE_VARIABLE
    }

    HasText getMappingTable();

    HasText getParticipantIdVariable();

    HasText getSampleIdVariable();

    HasText getSampleRoleVariable();

    void setAvailableMappingTables(JsArray<TableDto> availableMappingTables);

    void setVCFSamplesMappingDto(VCFSamplesMappingDto dto);

    void clearErrors();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();
  }

}
