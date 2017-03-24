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

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.TabPanelAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.VCFSamplesMappingDto;
import org.obiba.opal.web.model.client.opal.VCFStoreDto;
import org.obiba.opal.web.model.client.opal.VCFSummaryDto;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class ProjectGenotypesView extends ViewWithUiHandlers<ProjectGenotypesUiHandlers>
    implements ProjectGenotypesPresenter.Display {

  private static Logger logger = Logger.getLogger("ProjectGenotypesView");

  private static final int SORTABLE_COLUMN_VCF_FILE = 1;

  private static final int PERMISSIONS_TAB_INDEX = 1;

  @UiField
  Label participants;

  @UiField
  Label participantsWithGenotype;

  @UiField
  Label samples;

  @UiField
  Label controlSamples;

  @UiField
  Anchor tableLink;

  @UiField
  Anchor participantIdLink;

  @UiField
  Anchor sampleRoleLink;

  @UiField
  Table<VCFSummaryDto> vcfFilesTable;

  @UiField
  OpalSimplePager tablePager;

  @UiField
  FlowPanel filterPanel;

  @UiField
  TextBoxClearable filter;

  @UiField
  Button addMapping;

  @UiField
  IconAnchor editMapping;

  @UiField
  IconAnchor deleteMapping;

  @UiField
  PropertiesTable mappingProperties;

  @UiField
  Alert selectItemTipsAlert;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  Button exportVCF;

  @UiField
  IconAnchor exportLink;

  @UiField
  Button importVCF;

  @UiField
  FlowPanel noVcfServiceAlertPanel;

  @UiField
  SimplePanel permissionsPanel;

  @UiField
  TabPanel tabPanel;

  @UiField
  FlowPanel tabPanelContainer;

  @UiField
  IconAnchor deleteLink;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final ListDataProvider<VCFSummaryDto> dataProvider = new ListDataProvider<VCFSummaryDto>();

  private CheckboxColumn<VCFSummaryDto> checkColumn;

  private ColumnSortEvent.ListHandler<VCFSummaryDto> typeSortHandler;

  private boolean editableMapping;

  private VCFSamplesMappingDto vcfSamplesMapping;

  interface Binder extends UiBinder<Widget, ProjectGenotypesView> {
  }

  @Inject
  public ProjectGenotypesView(Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    this.translations = translations;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    addTableColumns();
    initializeFilter();
    showEditMapping(false);
  }

  @Override
  public List<VCFSummaryDto> getSelectedVCFs() {
    return checkColumn.getSelectedItems();
  }

  @Override
  public List<VCFSummaryDto> getAllVCFs() {
    return dataProvider.getList();
  }

  @Override
  public void setVCFSamplesSummary(VCFStoreDto dto) {
    participants.setText(dto != null && dto.hasParticipantsCount() ? dto.getParticipantsCount()+"" : "");
    participantsWithGenotype.setText(dto != null && dto.hasParticipantsWithGenotypeCount() ? dto.getParticipantsWithGenotypeCount()+"" : "");
    samples.setText(dto != null && dto.hasSamplesCount() ? dto.getSamplesCount()+"" : "");
    controlSamples.setText(dto != null && dto.hasControlSamplesCount() ? dto.getControlSamplesCount()+"" : "");
  }

  @Override
  public void setVCFSamplesMapping(VCFSamplesMappingDto vcfSamplesMapping) {
    this.vcfSamplesMapping = vcfSamplesMapping;
    boolean hasMapping = vcfSamplesMapping != null;
    showEditMapping(hasMapping);
    tableLink.setText(hasMapping ? vcfSamplesMapping.getTableReference() : "");
    participantIdLink.setText(hasMapping ? vcfSamplesMapping.getParticipantIdVariable() : "");
    sampleRoleLink.setText(hasMapping ? vcfSamplesMapping.getSampleRoleVariable() : "");
  }

  private void showEditMapping(boolean hasMapping) {
    editMapping.setVisible(editableMapping && hasMapping);
    deleteMapping.setVisible(editableMapping && hasMapping);
    mappingProperties.setVisible(hasMapping);
    addMapping.setVisible(editableMapping && !hasMapping);
  }

  private void refreshEditMapping() {
    boolean hasMapping = vcfSamplesMapping != null;
    showEditMapping(hasMapping);
  }

  @Override
  public void beforeRenderRows() {
    checkColumn.clearSelection();
    tablePager.setPagerVisible(false);
    vcfFilesTable.showLoadingIndicator(dataProvider);
    initializeFilter();
  }

  @Override
  public void afterRenderRows() {
    boolean pagerVisible = vcfFilesTable.getRowCount() > Table.DEFAULT_PAGESIZE;
    dataProvider.refresh();
    tablePager.setPagerVisible(pagerVisible);
    vcfFilesTable.hideLoadingIndicator();
    filterPanel.setStyleName(pagerVisible ? "span3" : "pull-right");
  }

  @Override
  public void clear(boolean hasVcfService) {
    clearMappingTable();
    // clear summary
    participants.setText("");
    participantsWithGenotype.setText("");
    samples.setText("");
    controlSamples.setText("");

    noVcfServiceAlertPanel.setVisible(!hasVcfService);
    tabPanelContainer.setVisible(hasVcfService);
  }

  @Override
  public void clearMappingTable() {
    setVCFSamplesMapping(null);
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, PERMISSIONS_TAB_INDEX);
  }

  @Override
  public HasAuthorization getImportAuthorizer() {
    return new WidgetAuthorizer(importVCF);
  }

  @Override
  public HasAuthorization getExportAuthorizer() {
    return new WidgetAuthorizer(exportVCF, exportLink);
  }

  @Override
  public HasAuthorization getEditMappingAuthorizer() {
    return new HasAuthorization() {
      @Override
      public void beforeAuthorization() {

      }

      @Override
      public void authorized() {
        editableMapping = true;
        refreshEditMapping();
      }

      @Override
      public void unauthorized() {
        editableMapping = false;
        refreshEditMapping();
      }
    };
  }

  @Override
  public HasAuthorization getRemoveVCF() {
    return new WidgetAuthorizer(deleteLink);
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    permissionsPanel.clear();
    if(content != null) {
      permissionsPanel.add(content.asWidget());
    }
  }

  @Override
  public void renderRows(JsArray<VCFSummaryDto> rows) {
    dataProvider.setList(JsArrays.toList(rows));
    typeSortHandler.setList(dataProvider.getList());
    ColumnSortEvent.fire(vcfFilesTable, vcfFilesTable.getColumnSortList());
    tablePager.firstPage();
  }

  private void initializeFilter() {
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterVCFs());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
  }

  @UiHandler({"exportVCF","exportLink"})
  public void downloadVCFClick(ClickEvent event) {
    getUiHandlers().onExportVcfFiles();
  }

  @UiHandler("importVCF")
  public void importVCFClick(ClickEvent event) {
    getUiHandlers().onImportVcfFiles();
  }

  @UiHandler("addMapping")
  public void addMappingClick(ClickEvent event) {
    getUiHandlers().onAddMappingTable();
  }

  @UiHandler("editMapping")
  public void editMappingClick(ClickEvent event) {
    getUiHandlers().onEditMappingTable();
  }

  @UiHandler("deleteMapping")
  public void deleteMappingClick(ClickEvent event) {
    getUiHandlers().onDeleteMappingTable();
  }

  @UiHandler("deleteLink")
  public void deleteLinkClick(ClickEvent event) {
    getUiHandlers().onRemoveVcfFile(checkColumn.getSelectedItems());
  }

  @UiHandler("filter")
  public void filterKeyUp(KeyUpEvent event) {
    getUiHandlers().onFilterUpdate(filter.getText());
  }

  @UiHandler("tableLink")
  public void tableLinkClick(ClickEvent event) {
    getUiHandlers().onMappingTableNavigateTo();
  }

  @UiHandler("participantIdLink")
  public void participantIdLinkClick(ClickEvent event) {
    getUiHandlers().onMappingTableNavigateToVariable(participantIdLink.getText());
  }

  @UiHandler("sampleRoleLink")
  public void sampleRoleLinkClick(ClickEvent event) {
    getUiHandlers().onMappingTableNavigateToVariable(sampleRoleLink.getText());
  }

  @UiHandler("refreshButton")
  public void refreshButtonClick(ClickEvent event) {
    getUiHandlers().onRefresh();
  }

  private void addTableColumns() {
    initializeColumns();
    dataProvider.addDataDisplay(vcfFilesTable);
    initializeSortableColumns();
    vcfFilesTable.setSelectionModel(new SingleSelectionModel<VCFSummaryDto>());
    vcfFilesTable.setPageSize(Table.DEFAULT_PAGESIZE);
    vcfFilesTable.setEmptyTableWidget(new InlineLabel(translationMessages.vcfFilesCount(0)));
    tablePager.setDisplay(vcfFilesTable);
  }

  private void initializeColumns() {
    checkColumn = new CheckboxColumn<VCFSummaryDto>(new ProjectGenotypesCheckStatusDisplay());
    vcfFilesTable.addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    vcfFilesTable.setColumnWidth(checkColumn, 1, Style.Unit.PX);
    vcfFilesTable.addColumn(ProjectGenotypesColumns.VCF_FILE, translations.vcfFileColumnHeader());
    vcfFilesTable.addColumn(ProjectGenotypesColumns.PARTICIPANTS_COUNT, translations.vcfParticipantsCountColumnHeader());
    vcfFilesTable.addColumn(ProjectGenotypesColumns.SAMPLES_COUNT, translations.vcfSamplesCountColumnHeader());
    vcfFilesTable.addColumn(ProjectGenotypesColumns.ORPHAN_SAMPLES_COUNT, translations.vcfOrphanSamplesCountColumnHeader());
    vcfFilesTable.addColumn(ProjectGenotypesColumns.CONTROLS_COUNT, translations.vcfControlsCountColumnHeader());
    vcfFilesTable.addColumn(ProjectGenotypesColumns.VARIANTS_COUNT, translations.vcfVariantsCountColumnHeader());
    vcfFilesTable.addColumn(ProjectGenotypesColumns.GENOTYPES_COUNT, translations.vcfGenotypesCountColumnHeader());
    vcfFilesTable.addColumn(new GenotypesActionsColumn(), translations.actionsLabel());
  }

  private void initializeSortableColumns() {
    typeSortHandler = new ColumnSortEvent.ListHandler<VCFSummaryDto>(dataProvider.getList());
    typeSortHandler.setComparator(vcfFilesTable.getColumn(SORTABLE_COLUMN_VCF_FILE), new VCFFileNameComparator());
    vcfFilesTable.getHeader(SORTABLE_COLUMN_VCF_FILE).setHeaderStyleNames("sortable-header-column");
    vcfFilesTable.getColumnSortList().push(vcfFilesTable.getColumn(SORTABLE_COLUMN_VCF_FILE));
    vcfFilesTable.addColumnSortHandler(typeSortHandler);
  }

  private class ProjectGenotypesCheckStatusDisplay implements CheckboxColumn.Display<VCFSummaryDto> {

    @Override
    public Table<VCFSummaryDto> getTable() {
      return vcfFilesTable;
    }

    @Override
    public Object getItemKey(VCFSummaryDto item) {
      return item.getName();
    }

    @Override
    public IconAnchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public IconAnchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public HasText getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public ListDataProvider<VCFSummaryDto> getDataProvider() {
      return dataProvider;
    }

    @Override
    public String getNItemLabel(int nb) {
      return translationMessages.nVCFsLabel(nb);
    }

    @Override
    public Alert getSelectActionsAlert() {
      return selectAllItemsAlert;
    }

    @Override
    public Alert getSelectTipsAlert() {
      return selectItemTipsAlert;
    }
  }

  private static class ProjectGenotypesColumns {
    static final Column<VCFSummaryDto, String> VCF_FILE = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return vcfSummaryDto.getName() + "." + vcfSummaryDto.getFormat().toLowerCase() + ".gz";
      }
    };

    static final Column<VCFSummaryDto, String> GENOTYPES_COUNT = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return Integer.toString(vcfSummaryDto.getGenotypesCount());
      }
    };

    static final Column<VCFSummaryDto, String> SAMPLES_COUNT = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return vcfSummaryDto.hasSamplesCount() //
          ? Integer.toString(vcfSummaryDto.getSamplesCount()) //
          : ""; //
      }
    };

    static final Column<VCFSummaryDto, String> VARIANTS_COUNT = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return Integer.toString(vcfSummaryDto.getVariantsCount());
      }
    };

    static final Column<VCFSummaryDto, String> PARTICIPANTS_COUNT = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return vcfSummaryDto.hasParticipantsCount()
          ? Integer.toString(vcfSummaryDto.getParticipantsCount())
          : "";
      }
    };

    static final Column<VCFSummaryDto, String> ORPHAN_SAMPLES_COUNT = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return Integer.toString(vcfSummaryDto.hasOrphanSamplesCount()
          ? vcfSummaryDto.getOrphanSamplesCount()
          : vcfSummaryDto.getTotalSamplesCount());
      }
    };

    static final Column<VCFSummaryDto, String> CONTROLS_COUNT = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return vcfSummaryDto.hasControlSamplesCount()
          ? Integer.toString(vcfSummaryDto.getControlSamplesCount())
          : "";
      }
    };
  }

  private static final class VCFFileNameComparator implements Comparator<VCFSummaryDto> {

    @Override
    public int compare(VCFSummaryDto o1, VCFSummaryDto o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  private class GenotypesActionsColumn extends ActionsColumn<VCFSummaryDto> {

    private static final String STATISTICS_ACTION = "Statistics";

    private GenotypesActionsColumn() {
      super(new ActionsProvider<VCFSummaryDto>() {

        @Override
        public String[] allActions() {
          return new String[] { REMOVE_ACTION, STATISTICS_ACTION };
        }

        private String[] someActions() {
          return new String[] { STATISTICS_ACTION };
        }

        @Override
        public String[] getActions(VCFSummaryDto value) {
          return deleteLink.isVisible() ? allActions() : someActions();
        }
      });
      setActionHandler(new ActionHandler<VCFSummaryDto>() {
        @Override
        public void doAction(VCFSummaryDto object, String actionName) {

          switch(actionName){
            case REMOVE_ACTION:
              getUiHandlers().onRemoveVcfFile(Lists.newArrayList(object));
              break;
            case STATISTICS_ACTION:
              getUiHandlers().onDownloadStatistics(object);
              break;
          }
        }
      });
    }
  }
}