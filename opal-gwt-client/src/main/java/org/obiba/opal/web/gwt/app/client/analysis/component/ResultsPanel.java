package org.obiba.opal.web.gwt.app.client.analysis.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisResultDto;

import java.util.List;
import org.obiba.opal.web.model.client.opal.OpalAnalysisResultsDto;

public class ResultsPanel extends Composite {
  interface Binder extends UiBinder<Widget, ResultsPanel>  {}

  private final EventBus eventBus;

  private final RequestUrlBuilder urlBuilder;

  private static final ResultsPanel.Binder uiBinder = GWT.create(ResultsPanel.Binder.class);

  private static final String DELETE_RESULT = "Delete";

  private static final String DOWNLOAD_RESULT = "Download";

  private static final String DOWNLOAD_LAST_RESULT = "Report";

  private OpalAnalysisResultDto lastResult;

  private List<OpalAnalysisResultDto> history;

  private ListDataProvider<OpalAnalysisResultDto> dataProvider = new ListDataProvider<>();

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
  TextBoxClearable filter;

  @UiField
  CollapsiblePanel historyPanel;

  @UiField
  InlineHTML status;

  public ResultsPanel(EventBus eventBus, RequestUrlBuilder urlBuilder) {
    initWidget(uiBinder.createAndBindUi(this));
    this.eventBus = eventBus;
    this.urlBuilder = urlBuilder;
  }

  public void initialize(TableDto tableDto, JsArray<OpalAnalysisResultDto> results) {
    int lastIndex = setHistoryResults(results);
    lastResult = results.get(lastIndex);
    initializeLastResult();

    if (history.size() > 0) initializeHistory();
    this.tableDto = tableDto;

    report.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        downloadResult(lastResult.getAnalysisId(), lastResult.getId());
      }
    });
  }

  private int setHistoryResults(JsArray<OpalAnalysisResultDto> results) {
    int lastIndex = results.length() - 1;
    history = JsArrays.toList(results, 1, lastIndex + 1);

    return lastIndex;
  }

  private void initializeLastResult() {
    String value = AnalysisStatusColumn.formatForRender(lastResult);
    String cellValue = AnalysisStatusColumn.StatusImageCell.renderAsString(value);

    status.setHTML(cellValue);
    start.setText(lastResult.getStartDate());
    end.setText(lastResult.getEndDate());
    message.setText(lastResult.getMessage());
  }

  private void initializeHistory() {
    historyPanel.setVisible(true);
    addTableColumns();
    initFilter();
    beforeRenderRows();
    renderRows();
  }

  private void addTableColumns() {
    actionColumn = actionColumn();

    actionColumn.setActionHandler(new ActionHandler<OpalAnalysisResultDto>() {
      @Override
      public void doAction(OpalAnalysisResultDto analysisResult, String actionName) {
        if (DOWNLOAD_RESULT.equals(actionName)) {
          downloadResult(analysisResult.getAnalysisId(), analysisResult.getId());

        } else if (DELETE_RESULT.equals(actionName)) {
          ResourceRequestBuilderFactory.<OpalAnalysisResultsDto>newBuilder().forResource(
              UriBuilders.PROJECT_TABLE_ANALYSIS_RESULT.create().build(tableDto.getDatasourceName(), tableDto.getName(), analysisResult.getAnalysisId(), analysisResult.getId()))
              .withCallback(new ResourceCallback<OpalAnalysisResultsDto>() {
                @Override
                public void onResource(Response response, OpalAnalysisResultsDto resource) {
                  historyPanel.setVisible(false);
                  setHistoryResults(resource.getAnalysisResultsArray());

                  if (history.size() > 0) {
                    historyPanel.setVisible(true);
                    filter.setText("");
                    renderRows();
                  }
                }
              }).delete().send();
        }
      }
    });

    // Status Column
    historyTable.addColumn(new AnalysisStatusColumn(), translations.analysisResultStatusLabel());

    // Date Column
    historyTable.addColumn(new TextColumn<OpalAnalysisResultDto>() {
      @Override
      public String getValue(OpalAnalysisResultDto object) {
        return object.getEndDate();
      }
    }, translations.analysisResultDateLabel());

    // Action Column
    historyTable.addColumn(actionColumn, translations.actionsLabel());

    historyTable.setColumnWidth(historyTable.getColumn(0), 25, Style.Unit.PCT);

    historyTable.setPageSize(historyTable.DEFAULT_PAGESIZE);
    historyTable.setEmptyTableWidget(new InlineLabel(translationMessages.analysisResultCount(0)));

    dataProvider.addDataDisplay(historyTable);
  }

  private void downloadResult(String analysisId, String resultId) {
    FileDownloadRequestEvent fileDownloadRequestEvent = new FileDownloadRequestEvent(
        UriBuilders.PROJECT_TABLE_ANALYSIS_RESULT_EXPORT.create()
            .build(tableDto.getDatasourceName(), tableDto.getName(),
                analysisId, resultId));

    eventBus.fireEvent(fileDownloadRequestEvent);
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

  private void initFilter() {
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterAnalysePlaceholder());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
  }

  private void beforeRenderRows() {
    filter.setText("");
    historyTable.showLoadingIndicator(dataProvider);
  }

  private void renderRows() {
    dataProvider.setList(history);
    dataProvider.refresh();
  }

  @UiHandler("filter")
  public void filterKeyUp(KeyUpEvent event) {
    String filterText = filter.getText();

    List<OpalAnalysisResultDto> filtered = new ArrayList<>();

    if (filterText != null && filterText.trim().length() > 0) {
      for(OpalAnalysisResultDto result : history) {
        if (result.getEndDate().toLowerCase().contains(filterText.trim().toLowerCase())) {
          filtered.add(result);
        }
      }
    } else {
      filtered = history;
    }

    dataProvider.setList(filtered);
    dataProvider.refresh();
  }
}