package org.obiba.opal.web.gwt.app.client.analysis.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisResultDto;

import java.util.List;

public class ResultsPanel extends Composite {
  interface Binder extends UiBinder<Widget, ResultsPanel>  {}

  private final EventBus eventBus;

  private static final ResultsPanel.Binder uiBinder = GWT.create(ResultsPanel.Binder.class);

  private OpalAnalysisResultDto lastResult;

  private List<OpalAnalysisResultDto> history;

  private ListDataProvider<OpalAnalysisResultDto> dataProvider = new ListDataProvider<>();

  private static final Translations translations = GWT.create(Translations.class);

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  private ActionsColumn<OpalAnalysisResultDto> actionColumn;

  @UiField
  Label status;

  @UiField
  Label start;

  @UiField
  Label end;

  @UiField
  Label message;

  @UiField
  Label report;

  @UiField
  Table<OpalAnalysisResultDto> historyTable;

  @UiField
  TextBoxClearable filter;

  public ResultsPanel(EventBus eventBus) {
    initWidget(uiBinder.createAndBindUi(this));
    this.eventBus = eventBus;
  }

  public void initialize(TableDto tableDto, JsArray<OpalAnalysisResultDto> results) {
    int lastIndex = results.length() - 1;
    lastResult = results.get(lastIndex);
    history = JsArrays.toList(results, 1, lastIndex++);

    initializeLastResult();
    initializeHistory();
  }

  private void initializeLastResult() {
    status.setText(lastResult.getStatus().getName());
    start.setText(lastResult.getStartDate());
    end.setText(lastResult.getEndDate());
    message.setText(lastResult.getMessage());
  }

  private void initializeHistory() {
    addTableColumns();
    initFilter();
    beforeRenderRows();
    renderRows();
  }

  private void addTableColumns() {
    actionColumn = actionColumn();

    // Status Column
    historyTable.addColumn(new TextColumn<OpalAnalysisResultDto>() {
      @Override
      public String getValue(OpalAnalysisResultDto object) {
        return object.getStatus().getName();
      }
    }, translations.analysisResultStatusLabel());

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


  private ActionsColumn<OpalAnalysisResultDto> actionColumn() {
    return new ActionsColumn<OpalAnalysisResultDto>(new ActionsProvider<OpalAnalysisResultDto>() {
      @Override
      public String[] allActions() {
        return new String[] {"Download", "Delete"};
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
}