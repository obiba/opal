/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.analysis.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.AnalysisResultItemDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisResultDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisResultsDto;

import java.util.Comparator;
import java.util.List;

public class ResultsPanel extends Composite {

  private static final int ENDED_COLUMN_INDEX = 1;

  interface Binder extends UiBinder<Widget, ResultsPanel>  {}

  private final RequestUrlBuilder urlBuilder;

  private static final ResultsPanel.Binder uiBinder = GWT.create(ResultsPanel.Binder.class);

  private static final String DELETE_RESULT = "Delete";

  private static final String DOWNLOAD_RESULT = "Report";

  private OpalAnalysisResultDto lastResult;

  private JsArray<OpalAnalysisResultDto> history;

  private List<AnalysisResultItemDto> details;

  private ListDataProvider<AnalysisResultItemDto> detailsDataProvider = new ListDataProvider<AnalysisResultItemDto>();

  private ListDataProvider<OpalAnalysisResultDto> historyDataProvider = new ListDataProvider<OpalAnalysisResultDto>();

  private ColumnSortEvent.ListHandler<OpalAnalysisResultDto> tableColumnSortHandler;

  private static final Translations translations = GWT.create(Translations.class);

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  private ActionsColumn<OpalAnalysisResultDto> actionColumn;

  private TableDto tableDto;

  @UiField
  Label start;

  @UiField
  Label end;

  @UiField
  Label message;

  @UiField
  Anchor report;

  @UiField
  Table<OpalAnalysisResultDto> historyTable;

  @UiField
  CollapsiblePanel historyPanel;

  @UiField
  InlineHTML status;

  @UiField
  CollapsiblePanel detailsPanel;

  @UiField
  Table detailsTable;

  public ResultsPanel(RequestUrlBuilder urlBuilder) {
    initWidget(uiBinder.createAndBindUi(this));
    this.urlBuilder = urlBuilder;
  }

  public void initialize(TableDto tableDto, JsArray<OpalAnalysisResultDto> results) {
    // Results are in descending order
    lastResult = results.shift();
    history = results;
    details = JsArrays.toList(lastResult.getResultItemsArray());
    initializeLastResult();

    if (details.size() > 0) initializeDetails();
    if (history.length() > 0) initializeHistory();

    this.tableDto = tableDto;

    report.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        downloadResult(lastResult.getAnalysisName(), lastResult.getId());
      }
    });
  }

  private void updateHistory(JsArray<OpalAnalysisResultDto> results) {
    // Results are in descending order
    lastResult = results.shift();
    history = results;
    if (history.length() > 0) {
      historyPanel.setVisible(true);
      renderHistoryRows();
    }
  }

  private void initializeLastResult() {
    String value = AnalysisStatusColumn.formatForRender(lastResult);
    String cellValue = AnalysisStatusColumn.StatusImageCell.renderAsString(value);

    status.setHTML(cellValue);
    start.setText(Moment.create(lastResult.getStartDate()).format(FormatType.MONTH_NAME_DAY_TIME_SHORT));
    end.setText(Moment.create(lastResult.getEndDate()).format(FormatType.MONTH_NAME_DAY_TIME_SHORT));
    message.setText(lastResult.getMessage());
  }

  private void initializeDetails() {
    detailsPanel.setVisible(true);
    detailsPanel.setOpen(true);
    addDetailsTableColumns();
    beforeDetailsRenderRows();
    renderDetailsRows();
  }

  private void initializeHistory() {
    historyPanel.setVisible(true);
    historyPanel.setText(translations.analysisResultHistoryLabel());
    addHistoryTableColumns();
    beforeHistoryRenderRows();
    renderHistoryRows();
  }

  private void addDetailsTableColumns() {
    // Status Column
    detailsTable.addColumn(new AnalysisStatusColumn.ForOpalAnalysisResultDto(), translations.analysisStatusLabel());
    detailsTable.addColumn(new TextColumn<AnalysisResultItemDto>() {
      @Override
      public String getValue(AnalysisResultItemDto itemDto) {
        return itemDto.getMessage();
      }
    }, translations.analysisMessageLabel());

    detailsTable.setColumnWidth(detailsTable.getColumn(0), 25, Style.Unit.PCT);
    detailsTable.setPageSize(historyTable.DEFAULT_PAGESIZE);
    detailsDataProvider.addDataDisplay(detailsTable);
  }

  private void addHistoryTableColumns() {
    actionColumn = actionColumn();

    actionColumn.setActionHandler(new ActionHandler<OpalAnalysisResultDto>() {
      @Override
      public void doAction(OpalAnalysisResultDto analysisResult, String actionName) {
        if (DOWNLOAD_RESULT.equals(actionName)) {
          downloadResult(analysisResult.getAnalysisName(), analysisResult.getId());

        } else if (DELETE_RESULT.equals(actionName)) {
          ResourceRequestBuilderFactory.<OpalAnalysisResultsDto>newBuilder().forResource(
              UriBuilders.PROJECT_TABLE_ANALYSIS_RESULT.create().build(tableDto.getDatasourceName(), tableDto.getName(), analysisResult.getAnalysisName(), analysisResult.getId()))
              .withCallback(new ResourceCallback<OpalAnalysisResultsDto>() {
                @Override
                public void onResource(Response response, OpalAnalysisResultsDto resource) {
                  historyPanel.setVisible(false);
                  updateHistory(resource.getAnalysisResultsArray());


                }
              }).delete().send();
        }
      }
    });

    // Status Column
    historyTable.addColumn(new AnalysisStatusColumn.ForOpalAnalysisResultDto(), translations.analysisStatusLabel());

    // Date Column
    EndedColumn endedColumn = new EndedColumn();
    historyTable.addColumn(endedColumn, translations.analysisResultDateLabel());

    // Action Column
    historyTable.addColumn(actionColumn, translations.actionsLabel());
    historyTable.setColumnWidth(historyTable.getColumn(0), 25, Style.Unit.PCT);
    historyTable.setPageSize(historyTable.DEFAULT_PAGESIZE);
    historyTable.setEmptyTableWidget(new InlineLabel(translationMessages.analysisResultCount(0)));

    historyDataProvider.addDataDisplay(historyTable);
    initializeSortableColumns(endedColumn);
  }

  private void initializeSortableColumns(EndedColumn endedColumn) {
    tableColumnSortHandler = new ColumnSortEvent.ListHandler<OpalAnalysisResultDto>(historyDataProvider.getList());
    historyTable.getHeader(ENDED_COLUMN_INDEX).setHeaderStyleNames("sortable-header-column");
    tableColumnSortHandler.setComparator(endedColumn, endedColumn);
    historyTable.getColumnSortList().push(endedColumn);
    historyTable.addColumnSortHandler(tableColumnSortHandler);
  }

  private void downloadResult(String analysisName, String resultId) {
    String href = urlBuilder.buildAbsoluteUrl(UriBuilders.PROJECT_TABLE_ANALYSIS_RESULT_REPORT.create()
      .build(tableDto.getDatasourceName(), tableDto.getName(), analysisName, resultId));

    Window.open(href, "_blank", "");
  }

  private ActionsColumn<OpalAnalysisResultDto> actionColumn() {
    return new ActionsColumn<OpalAnalysisResultDto>(new ActionsProvider<OpalAnalysisResultDto>() {
      @Override
      public String[] allActions() {
        return new String[] {DOWNLOAD_RESULT, DELETE_RESULT};
      }

      @Override
      public String[] getActions(OpalAnalysisResultDto value) {
        return allActions();
      }
    });
  }

  private void beforeDetailsRenderRows() {
    detailsTable.showLoadingIndicator(detailsDataProvider);
  }

  private void beforeHistoryRenderRows() {
    historyTable.showLoadingIndicator(historyDataProvider);
  }

  private void renderDetailsRows() {
    detailsDataProvider.setList(details);
    detailsDataProvider.refresh();
  }

  private void renderHistoryRows() {
    historyDataProvider.setList(JsArrays.toList(history));
    historyDataProvider.refresh();
    tableColumnSortHandler.setList(historyDataProvider.getList());
    ColumnSortEvent.fire(historyTable, historyTable.getColumnSortList());
  }


  private class EndedColumn extends TextColumn<OpalAnalysisResultDto> implements Comparator<OpalAnalysisResultDto>  {

    EndedColumn() {
      setDefaultSortAscending(false);
      setSortable(true);
    }

    @Override
    public String getValue(OpalAnalysisResultDto object) {
      return Moment.create(object.getEndDate()).format(FormatType.MONTH_NAME_TIME_SHORT);
    }

    @Override
    public int compare(OpalAnalysisResultDto o1, OpalAnalysisResultDto o2) {
      return (int)(Moment.create(o1.getEndDate()).valueOf() - Moment.create(o2.getEndDate()).valueOf());
    }
  }
}