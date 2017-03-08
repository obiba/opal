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

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.model.client.opal.VCFSummaryDto;

public class ProjectGenotypesView extends ViewWithUiHandlers<ProjectGenotypesUiHandlers>
    implements ProjectGenotypesPresenter.Display {

  @UiField
  Label participants;

  @UiField
  Label participantsWithGenotypes;

  @UiField
  Label samples;

  @UiField
  Label controlSamples;

  @UiField
  Label project;

  @UiField
  Label table;

  @UiField
  Label participantId;

  @UiField
  Label sampleId;

  @UiField
  Label sampleRole;

  @UiField
  Table<VCFSummaryDto> vcfFilesTable;

  @UiField
  OpalSimplePager tablePager;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final ListDataProvider<VCFSummaryDto> dataProvider = new ListDataProvider<VCFSummaryDto>();

  private CheckboxColumn<VCFSummaryDto> checkColumn;

  private ColumnSortEvent.ListHandler<VCFSummaryDto> typeSortHandler;

  @Override
  public void renderRows(JsArray<VCFSummaryDto> rows) {
    dataProvider.setList(JsArrays.toList(rows));
    typeSortHandler.setList(dataProvider.getList());
    ColumnSortEvent.fire(vcfFilesTable, vcfFilesTable.getColumnSortList());
    tablePager.firstPage();
  }

  interface Binder extends UiBinder<Widget, ProjectGenotypesView> {
  }

  @Inject
  public ProjectGenotypesView(Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    this.translationMessages = translationMessages;
    addTableColumns();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (content != null) {
    }
  }

  @UiHandler("downloadVCF")
  public void downloadVCFClick(ClickEvent event) {
    getUiHandlers().onDownloadVcfFiles();
  }

  @UiHandler("importVCF")
  public void importVCFClick(ClickEvent event) {
    getUiHandlers().onImportVcfFiles();
  }

  private void addTableColumns() {
    initializeColumns();
    dataProvider.addDataDisplay(vcfFilesTable);
    initializeSortableColumns();
    vcfFilesTable.setSelectionModel(new SingleSelectionModel<VCFSummaryDto>());
    vcfFilesTable.setEmptyTableWidget(new InlineLabel(translationMessages.tableCount(0)));
    vcfFilesTable.setPageSize(Table.DEFAULT_PAGESIZE);
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

  }

  private class ProjectGenotypesCheckStatusDisplay implements CheckboxColumn.Display<VCFSummaryDto> {

    @Override
    public Table<VCFSummaryDto> getTable() {
      return null;
    }

    @Override
    public Object getItemKey(VCFSummaryDto item) {
      return vcfFilesTable;
    }

    @Override
    public IconAnchor getClearSelection() {
      return null;
    }

    @Override
    public IconAnchor getSelectAll() {
      return null;
    }

    @Override
    public HasText getSelectAllStatus() {
      return null;
    }

    @Override
    public ListDataProvider<VCFSummaryDto> getDataProvider() {
      return dataProvider;
    }

    @Override
    public String getNItemLabel(int nb) {
      return null;
    }

    @Override
    public Alert getSelectActionsAlert() {
      return null;
    }

    @Override
    public Alert getSelectTipsAlert() {
      return null;
    }
  }

  private static class ProjectGenotypesColumns {
    static final Column<VCFSummaryDto, String> VCF_FILE = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return vcfSummaryDto.getName();
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
        return Integer.toString(vcfSummaryDto.getSamplesCount());
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
        return "300";
      }
    };

    static final Column<VCFSummaryDto, String> ORPHAN_SAMPLES_COUNT = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return "100";
      }
    };

    static final Column<VCFSummaryDto, String> CONTROLS_COUNT = new TextColumn<VCFSummaryDto>() {

      @Override
      public String getValue(VCFSummaryDto vcfSummaryDto) {
        return "2";
      }
    };
  }

  private class GenotypesActionsColumn extends ActionsColumn<VCFSummaryDto> {

    private static final String DOWNLOAD_ACTION = "Download";

    private static final String STATISTICS_ACTION = "Statistics";

    private GenotypesActionsColumn() {
      super(new ActionsProvider<VCFSummaryDto>() {

        @Override
        public String[] allActions() {
          return new String[] { REMOVE_ACTION, DOWNLOAD_ACTION, STATISTICS_ACTION };
        }

        @Override
        public String[] getActions(VCFSummaryDto value) {
          return allActions();
        }
      });
      setActionHandler(new ActionHandler<VCFSummaryDto>() {
        @Override
        public void doAction(VCFSummaryDto object, String actionName) {

          switch(actionName){
            case REMOVE_ACTION:
              getUiHandlers().onRemoveVcfFile(object);
              break;
            case  EDIT_ACTION:
              getUiHandlers().onDownloadVcfFile(object);
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