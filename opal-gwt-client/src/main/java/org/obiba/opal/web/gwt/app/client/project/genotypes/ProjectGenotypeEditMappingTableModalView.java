/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project.genotypes;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.VCFSamplesMappingDto;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Logger;

public class ProjectGenotypeEditMappingTableModalView extends ModalPopupViewWithUiHandlers<ProjectGenotypeEditMappingTableModalUiHandlers>
        implements ProjectGenotypeEditMappingTableModalPresenter.Display {

  private final Translations translations;

  private VCFSamplesMappingDto initialMappingTable;

  interface Binder extends UiBinder<Widget, ProjectGenotypeEditMappingTableModalView> {}

  private static Logger logger = Logger.getLogger("ProjectGenotypeEditMappingTableModalView");

  @UiField
  Modal dialog;

  @UiField
  Chooser mappingTable;

  @UiField
  Chooser participantIds;

  @UiField
  Chooser sampleRoleIds;

  @UiField
  ControlGroup participantIdVariableGroup;

  @UiField
  ControlGroup sampleRoleVariableGroup;

  @UiField
  FlowPanel tableAvailableUiContent;

  @UiField
  FlowPanel noTableAvailableAlertContainer;

  @UiField
  Button saveButton;

  @Inject
  public ProjectGenotypeEditMappingTableModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    this.translations = translations;
    dialog.setTitle(translations.projectGenotypeEditMappingeModalTitle());
    toggleContent(false);
  }

  @Override
  public void onShow() {
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public VCFSamplesMappingDto getInitialMappingTable() {
    return initialMappingTable;
  }

  @Override
  public void setVariables(JsArray<VariableDto> variables, VariableDto suggestedParticipantIdVar,
      VariableDto suggestedSampleRoleVar) {

    participantIds.clear();
    sampleRoleIds.clear();

    participantIds.addItem("");
    sampleRoleIds.addItem("");

    for (VariableDto variableDto : JsArrays.toIterable(variables)) {
      // place full path in case same table name exists in another datasource
      participantIds.addItem(variableDto.getName());
      sampleRoleIds.addItem(variableDto.getName());
    }

    String selectedTable = mappingTable.getSelectedValue();
    if (initialMappingTable != null && initialMappingTable.getTableReference().equals(selectedTable)) {
      participantIds.setSelectedValue(initialMappingTable.getParticipantIdVariable());
      sampleRoleIds.setSelectedValue(initialMappingTable.getSampleRoleVariable());
    } else {
      if (suggestedParticipantIdVar != null) {
        participantIds.setSelectedValue(suggestedParticipantIdVar.getName());
      } else {
        participantIds.setItemSelected(0, true);
      }
      if (suggestedSampleRoleVar != null) {
        sampleRoleIds.setSelectedValue(suggestedSampleRoleVar.getName());
      } else {
        sampleRoleIds.setItemSelected(0, true);
      }
    }
  }

  @Override
  public HasText getMappingTable() {
    return new HasText() {
      @Override
      public String getText() {
        return mappingTable.getSelectedValue();
      }

      @Override
      public void setText(String s) {
        mappingTable.setSelectedValue(s);
      }
    };
  }

  @Override
  public HasText getSampleRoleVariable() {
    return new HasText() {

      @Override
      public String getText() {
        return sampleRoleIds.getSelectedValue();
      }

      @Override
      public void setText(String s) {
        sampleRoleIds.setSelectedValue(s);
      }
    };
  }

  @Override
  public HasText getParticipantIdVariable() {
    return new HasText() {
      @Override
      public String getText() {
        return participantIds.getSelectedValue();
      }

      @Override
      public void setText(String s) {
        participantIds.setSelectedValue(s);
      }
    };
  }

  @Override
  public void setAvailableMappingTables(JsArray<TableDto> availableMappingTables, String projectName) {
    mappingTable.clear();
    List<String> groups = Lists.newArrayList();

    toggleContent(availableMappingTables.length() > 0);

    int tentativeIndex = -1;
    for (int i = 0; i < availableMappingTables.length(); i++) {
      TableDto tableDto = availableMappingTables.get(i);
      if (groups.indexOf(tableDto.getDatasourceName()) == -1) {
        groups.add(tableDto.getDatasourceName());
        mappingTable.addGroup(tableDto.getDatasourceName());
      }
      if (projectName.equals(tableDto.getDatasourceName()) && tentativeIndex == -1) tentativeIndex = i;
      mappingTable.addItemToGroup(tableDto.getName(), tableDto.getDatasourceName() + "." + tableDto.getName());
    }
    if (availableMappingTables.length() > 0) {
      if (initialMappingTable == null) {
        mappingTable.setItemSelected(tentativeIndex == -1 ? 0 : tentativeIndex, true);
      } else {
        mappingTable.setSelectedValue(initialMappingTable.getTableReference());
      }
      mappingTableChange(null);
    }
    mappingTable.update();
  }

  @Override
  public void setVcfSamplesMappingDto(VCFSamplesMappingDto dto) {
    initialMappingTable = dto;
    if (dto != null) {
      mappingTable.setSelectedValue(dto.getTableReference());
      //getMappingTable().setText(dto.getTableReference());
      participantIds.setSelectedValue(dto.getParticipantIdVariable());
      sampleRoleIds.setSelectedValue(dto.getSampleRoleVariable());
    }
  }

  @Override
  public void clearErrors() {
    dialog.clearAlert();
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch (formField) {
        case PARTICIPANT_ID_VARIABLE:
          group = participantIdVariableGroup;
          break;
        case SAMPLE_ROLE_VARIABLE:
          group = sampleRoleVariableGroup;
          break;
      }
    }

    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }

  @UiHandler("saveButton")
  public void saveButtonClick(ClickEvent event) {
    getUiHandlers().onSaveEdit();
  }

  @UiHandler("cancelButton")
  public void cancelButtonClick(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("mappingTable")
  public void mappingTableChange(ChangeEvent event) {
    logger.info("Selected table " + mappingTable.getSelectedValue());
    if ("".equals(mappingTable.getSelectedValue())) {
      participantIds.clear();
      sampleRoleIds.clear();
    } else {
      getUiHandlers().onGetTableVariables(mappingTable.getSelectedValue());
    }
  }

  public void toggleContent(boolean showContent) {
    noTableAvailableAlertContainer.setVisible(!showContent);
    saveButton.setVisible(showContent);
    tableAvailableUiContent.setVisible(showContent);
  }
}
