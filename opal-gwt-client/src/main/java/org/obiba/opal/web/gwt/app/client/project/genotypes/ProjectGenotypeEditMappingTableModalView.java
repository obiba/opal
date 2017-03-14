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

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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
import org.obiba.opal.web.model.client.opal.VCFSamplesMappingDto;

import javax.annotation.Nullable;

public class ProjectGenotypeEditMappingTableModalView extends ModalPopupViewWithUiHandlers<ProjectGenotypeEditMappingTableModalUiHandlers>
        implements ProjectGenotypeEditMappingTableModalPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectGenotypeEditMappingTableModalView> {}

  @UiField
  Modal dialog;

  @UiField
  ControlGroup participantIdVariableGroup;

  @UiField
  TextBox participantIdVariable;

  @UiField
  ControlGroup sampleIdVariableGroup;

  @UiField
  TextBox sampleIdVariable;

  @UiField
  ControlGroup sampleRoleVariableGroup;

  @UiField
  TextBox sampleRoleVariable;

  @UiField
  Chooser mappingTable;

  @Inject
  public ProjectGenotypeEditMappingTableModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.projectGenotypeEditMappingeModalTitle());
  }

  @Override
  public void onShow() {
  }

  @Override
  public void hideDialog() {
    dialog.hide();
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
        if (Strings.isNullOrEmpty(s)) return;
        int count = mappingTable.getItemCount();
        for (int i = 0; i < count; i++) {
          if (mappingTable.getValue(i).equals(s)) {
            mappingTable.setSelectedIndex(i);
          }
        }
      }
    };
  }

  @Override
  public HasText getParticipantIdVariable() {
    return participantIdVariable;
  }

  @Override
  public HasText getSampleIdVariable() {
    return sampleIdVariable;
  }

  @Override
  public HasText getSampleRoleVariable() {
    return sampleRoleVariable;
  }

  @Override
  public void setAvailableMappingTables(JsArray<TableDto> availableMappingTables) {
    mappingTable.clear();

    for (TableDto tableDto : JsArrays.toIterable(availableMappingTables)) {
      mappingTable.addItem(tableDto.getName(), tableDto.getDatasourceName() + "." + tableDto.getName());
    }
  }

  @Override
  public void setVcfSamplesMappingDto(VCFSamplesMappingDto dto) {
    participantIdVariable.setText(dto.getParticipantIdVariable());
    sampleIdVariable.setText(dto.getSampleIdVariable());
    sampleRoleVariable.setText(dto.getSampleRoleVariable());
    getMappingTable().setText(dto.getTableReference());
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
        case SAMPLE_ID_VARIABLE:
          group = sampleIdVariableGroup;
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
}
