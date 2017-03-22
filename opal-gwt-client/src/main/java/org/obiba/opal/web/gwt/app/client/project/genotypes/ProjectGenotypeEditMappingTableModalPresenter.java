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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfMappingDeleteRequestEvent;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfMappingEditRequestEvent;
import org.obiba.opal.web.gwt.app.client.validator.*;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.VCFSamplesMappingDto;

import javax.annotation.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ProjectGenotypeEditMappingTableModalPresenter extends ModalPresenterWidget<ProjectGenotypeEditMappingTableModalPresenter.Display>
    implements ProjectGenotypeEditMappingTableModalUiHandlers {

  private static Logger logger = Logger.getLogger("ProjectGenotypeEditMappingTableModalPresenter");

  private static final String PARTICIPANT_ID_VARIABE_KEY = "participantId";

  private static final String SAMPLE_ROLE_VARIABE_KEY = "sampleRole";

  private static final String CONTROL_CATEGORY = "control";

  private static final String SAMPLE_CATEGORY = "sample";

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

  public void setVCFSamplesMapping(VCFSamplesMappingDto currentGenotypesMapping, ProjectDto projectDto) {
    projectName = projectDto.getName();
    getView().setVcfSamplesMappingDto(currentGenotypesMapping);
    if (currentGenotypesMapping != null) onGetTableVariables(currentGenotypesMapping.getTableReference());
  }

  @Override
  public void onSaveEdit() {
    getView().clearErrors();
    String mappingTable = getView().getMappingTable().getText();

    if(Strings.isNullOrEmpty(mappingTable)) {
      if (getView().getInitialMappingTable() != null) fireEvent(new VcfMappingDeleteRequestEvent(projectName));
      getView().hideDialog();
    } else if (validationHandler.validate()) {
      VCFSamplesMappingDto dto = VCFSamplesMappingDto.create();
      dto.setParticipantIdVariable(getView().getParticipantIdVariable().getText());
      dto.setSampleRoleVariable(getView().getSampleRoleVariable().getText());
      dto.setTableReference(getView().getMappingTable().getText());
      dto.setProjectName(projectName);
      fireEvent(new VcfMappingEditRequestEvent(dto));
      getView().hideDialog();
    }
  }

  @Override
  public void onGetTableVariables(String tableReference) {
    if (Strings.isNullOrEmpty(tableReference)) return;
    String[] parts = tableReference.split("\\.");

    ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder()
      .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLES.create().build(parts[0], parts[1]))
      .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
        @Override
        public void onResource(Response response, JsArray<VariableDto> variables) {
          Map<String, VariableDto> suggestions = suggestParticipantSampleVariables(variables);
          getView().setVariables(variables, suggestions.get(PARTICIPANT_ID_VARIABE_KEY),
              suggestions.get(SAMPLE_ROLE_VARIABE_KEY));
        }

        private Map<String, VariableDto> suggestParticipantSampleVariables(JsArray<VariableDto> variables) {
          Map<String, VariableDto> suggestions = Maps.newHashMap();

          for (VariableDto variable : JsArrays.toIterable(variables)) {
            JsArray<CategoryDto> categories = variable.getCategoriesArray();

            if (variable.hasReferencedEntityType() && "Participant".equals(variable.getReferencedEntityType())) {
              // found candidate for Participant ID variable
              suggestions.put(PARTICIPANT_ID_VARIABE_KEY, variable);
            } else if (categories != null && categories.length() > 0) {
              // found candidate for Sample Role variable
              boolean foundControl = false;
              boolean foundSample = false;

              for(CategoryDto category : JsArrays.toIterable(categories)) {
                String categoryName = category.getName();
                if (CONTROL_CATEGORY.equalsIgnoreCase(categoryName)) foundControl = true;
                if (SAMPLE_CATEGORY.equalsIgnoreCase(categoryName)) foundSample = true;

                if (foundControl && foundSample) {
                  suggestions.put(SAMPLE_ROLE_VARIABE_KEY, variable);
                  break;
                }
              }
            }

            if (suggestions.size() == 2) {
              return suggestions;
            }
          }

          // The mapping variables do not have the apt categories nor the reference entity type
          return suggestions;
        }

      }).get().send();
  }

  private class ModalValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<>();
        validators.add(new RequiredTextValidator(getView().getParticipantIdVariable(),
            "ParticipantIdVariableIsRequired", Display.FormField.PARTICIPANT_ID_VARIABLE.name()));
        validators.add(new RequiredTextValidator(getView().getSampleRoleVariable(),
            "SampleRoleVariableIsRequired", Display.FormField.SAMPLE_ROLE_VARIABLE.name()));
        validators.add(
          new ConditionValidator(variableNameCondition(getView().getParticipantIdVariable(),
            getView().getSampleRoleVariable()),
          "VCFParticipantSampleVariablesIdentical"));
      }
      return validators;
    }

    private HasValue<Boolean> variableNameCondition(final HasText participantIdVariable, final HasText sampleRoleVariables) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !participantIdVariable.getText().equals(sampleRoleVariables.getText());
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Strings.isNullOrEmpty(id) ? null : Display.FormField.valueOf(id), message);
    }

  }

  public interface Display extends PopupView, HasUiHandlers<ProjectGenotypeEditMappingTableModalUiHandlers> {
    enum FormField {
      PARTICIPANT_ID_VARIABLE,
      SAMPLE_ROLE_VARIABLE
    }

    void setVariables(JsArray<VariableDto> variables, VariableDto suggestedParticipantIdVar,
        VariableDto suggestedSampleRoleVar);

    HasText getMappingTable();

    HasText getParticipantIdVariable();

    HasText getSampleRoleVariable();

    void setAvailableMappingTables(JsArray<TableDto> availableMappingTables);

    void setVcfSamplesMappingDto(VCFSamplesMappingDto dto);

    void clearErrors();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();

    VCFSamplesMappingDto getInitialMappingTable();
  }

}
