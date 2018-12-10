package org.obiba.opal.web.gwt.app.client.analysis;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import javax.inject.Inject;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

public class AnalysesView extends ViewWithUiHandlers<AnalysesUiHandlers> implements AnalysesPresenter.Display {

  @UiTemplate("AnalysesView.ui.xml")
  interface AnalysesViewUiBinder extends UiBinder<Widget, AnalysesView> { }

  @UiField
  Table<OpalAnalysisDto> table;

  @UiField
  TextBoxClearable filter;

  @UiField
  Button refreshButton;

  @UiField
  Button newButton;

  private ListDataProvider<OpalAnalysisDto> dataProvider = new ListDataProvider<>();

  private ActionsColumn<OpalAnalysisDto> analysesActionColumn;

  private final Translations translations;
  private final TranslationMessages translationMessages;

  private static AnalysesViewUiBinder uiBinder = GWT.create(AnalysesViewUiBinder.class);

  @Inject
  public AnalysesView(Translations translations,
      TranslationMessages translationMessages) {
    initWidget(uiBinder.createAndBindUi(this));

    this.translations = translations;
    this.translationMessages = translationMessages;

    addTableColumns();
    initFilter();
  }

  private void addTableColumns() {
    analysesActionColumn = actionColumn();

    table.addColumn(new TextColumn<OpalAnalysisDto>() {
      @Override
      public String getValue(OpalAnalysisDto object) {
        return object.getName();
      }
    }, translations.analysisNameLabel());

    table.addColumn(new TextColumn<OpalAnalysisDto>() {
      @Override
      public String getValue(OpalAnalysisDto object) {
          return object.getTemplateName();
      }
    }, translations.analysisTypeLabel());

    table.addColumn(new TextColumn<OpalAnalysisDto>() {
      @Override
      public String getValue(OpalAnalysisDto object) {
        return Moment.create(object.getUpdated()).format(FormatType.MONTH_NAME_TIME_SHORT);
      }
    }, translations.dateLabel());

    table.addColumn(analysesActionColumn, translations.actionsLabel());
    table.setColumnWidth(table.getColumn(3), 175, com.google.gwt.dom.client.Style.Unit.PX);

    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(new InlineLabel(translationMessages.analysisCount(0)));

    dataProvider.addDataDisplay(table);
  }

  private void initFilter() {
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterAnalysePlaceholder());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
  }

  private ActionsColumn<OpalAnalysisDto> actionColumn() {
    return new ActionsColumn<OpalAnalysisDto>(new ActionsProvider<OpalAnalysisDto>() {
      @Override
      public String[] allActions() {
        return new String[] {RUN_ANALYSIS, VIEW_ANALYSIS, DUPLICATE_ANALYSIS, DELETE_ANALYSIS};
      }

      @Override
      public String[] getActions(OpalAnalysisDto value) {
        return allActions();
      }
    });
  }

  @Override
  public void beforeRenderRows() {
    filter.setText("");
    table.showLoadingIndicator(dataProvider);
  }

  @Override
  public void renderRows(JsArray<OpalAnalysisDto> analyses) {
    dataProvider.setList(JsArrays.toList(analyses));
    dataProvider.refresh();
  }

  @Override
  public void afterRenderRows() {
    table.hideLoadingIndicator();
  }

  @Override
  public void clearTable() {
    renderRows((JsArray<OpalAnalysisDto>) JavaScriptObject.createArray());
  }

  @Override
  public HandlerRegistration addRefreshButtonHandler(ClickHandler handler) {
    return refreshButton.addClickHandler(handler);
  }

  @Override
  public HasActionHandler<OpalAnalysisDto> getActionColumn() {
    return analysesActionColumn;
  }

  @UiHandler("newButton")
  public void onNewButton(ClickEvent event) {
    getUiHandlers().createAnalysis();
  }

  @UiHandler("filter")
  public void filterKeyUp(KeyUpEvent event) {
    getUiHandlers().onUpdateAnalysesFilter(filter.getText());
  }
}