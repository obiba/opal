/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project.genotypes;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePanel;
import org.obiba.opal.web.model.client.magma.TableDto;

import javax.annotation.Nullable;
import java.util.List;

public class ProjectExportVcfFileModalView extends ModalPopupViewWithUiHandlers<ProjectExportVcfFileModalUiHandlers>
        implements ProjectExportVcfFileModalPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectExportVcfFileModalView> {}

  @UiField
  Modal dialog;

  @UiField
  Button cancelButton;

  @UiField
  OpalSimplePanel vcfFilePanel;

  @UiField
  ControlGroup fileGroup;

  @UiField
  ControlGroup participantsFilterGroup;

  @UiField
  Chooser participantsFilter;

  @UiField
  ControlGroup participantsIdentifiersGroup;

  @UiField
  Chooser participantsIdentifiersMapping;

  @UiField
  CheckBox includeCaseControls;

  @UiField
  Alert exportNVCF;

  @UiField
  FlowPanel mappingDependant;

  private static final String PARTICIPANT_FILTER_NONE = "_none";

  private Translations translations;

  private List<String> participantIdentifiersMappingList;

  @Inject
  public ProjectExportVcfFileModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.exportVcfModalTitle());
    this.translations = translations;
    includeCaseControls.setValue(true); // default value
  }

  @Override
  public void onShow() {
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void showExportNAlert(String message) {
    exportNVCF.setText(message);
  }

  @Override
  public HasText getParticipantsFilterTable() {

    return new HasText() {
      @Override
      public String getText() {
        String selectedParticipantFilter = participantsFilter.getSelectedValue();
        return selectedParticipantFilter == null || PARTICIPANT_FILTER_NONE.equals(selectedParticipantFilter) ? null : selectedParticipantFilter;
      }

      @Override
      public void setText(String s) {
        if(Strings.isNullOrEmpty(s)) return;
        int count = participantsFilter.getItemCount();
        for(int i = 0; i < count; i++) {
          if(participantsFilter.getValue(i).equals(s)) {
            participantsFilter.setSelectedIndex(i);
            break;
          }
        }
      }
    };
  }

  @Override
  public HasText getParticipantIdentifiersMapping() {
    return new HasText() {
      @Override
      public String getText() {
        String participantIdentifierMappingTableName = participantsIdentifiersMapping.getSelectedValue();
        return participantIdentifierMappingTableName == null || PARTICIPANT_FILTER_NONE.equals(participantIdentifierMappingTableName) ? null : participantIdentifierMappingTableName;
      }

      @Override
      public void setText(String text) {
      }
    };
  }

  @Override
  public void showMappingDependant(boolean show) {
    mappingDependant.setVisible(show);
  }

  @Override
  public boolean hasCaseControls() {
    return includeCaseControls.getValue();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("exportButton")
  public void exportButtonClick(ClickEvent event) {
    getUiHandlers().onExport();
  }

  @Override
  public void setParticipants(JsArray<TableDto> tables) {
    participantsFilter.clear();
    participantsFilter.addItem("(" + translations.none() + ")", PARTICIPANT_FILTER_NONE);

    List<String> groups = Lists.newArrayList();

    for (TableDto tableDto : JsArrays.toIterable(tables)) {
      if (groups.indexOf(tableDto.getDatasourceName()) == -1) {
        groups.add(tableDto.getDatasourceName());
        participantsFilter.addGroup(tableDto.getDatasourceName());
      }

      participantsFilter.addItemToGroup(tableDto.getName(), tableDto.getDatasourceName() + "." + tableDto.getName());
    }

    participantsFilter.update();

    participantsFilter.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent event) {
        updateParticipantsIdentifiersGroupVisibility();
      }
    });
  }

  @Override
  public void setParticipantIdentifiersMappingList(List<String> participantIdentifiersMappingList) {
    this.participantIdentifiersMappingList=participantIdentifiersMappingList;
    updateParticipantsIdentifiersGroupVisibility();
  }

  private void updateParticipantsIdentifiersGroupVisibility() {
    participantsIdentifiersMapping.clear();
    participantsIdentifiersMapping.addItem("(" + translations.none() + ")", PARTICIPANT_FILTER_NONE);
    for (String participantIdentifiersMapping : participantIdentifiersMappingList) {
      participantsIdentifiersMapping.addItem(participantIdentifiersMapping);
    }
    participantsIdentifiersGroup.setVisible(participantIdentifiersMappingList.size()>0);
    participantsIdentifiersMapping.setEnabled(!participantsFilter.getSelectedValue().equals(PARTICIPANT_FILTER_NONE) && participantIdentifiersMappingList.size()>0);
  }

  @Override
  public void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display) {
    vcfFilePanel.setWidget(display.asWidget());
    display.setFieldWidth("20em");
  }

  @Override
  public void clearErrors() {
    dialog.clearAlert();
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case DIRECTORY:
          group = fileGroup ;
          break;
      }
    }
    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }
}
