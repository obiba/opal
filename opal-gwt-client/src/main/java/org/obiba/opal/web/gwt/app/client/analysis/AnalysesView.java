package org.obiba.opal.web.gwt.app.client.analysis;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.OpalAnalysesDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

public class AnalysesView extends ViewWithUiHandlers<AnalysesUiHandlers> implements AnalysesPresenter.Display {

  @UiTemplate("AnalysesView.ui.xml")
  interface AnalysesViewUiBinder extends UiBinder<Widget, AnalysesView> { }

  @UiField
  Table<OpalAnalysisDto> table;

  @UiField
  TextBoxClearable filter;

  private ListDataProvider<OpalAnalysisDto> dataProvider = new ListDataProvider<>();

  private ActionsColumn<OpalAnalysisDto> analysesActionColumn;

  private final Translations translations;

  private static AnalysesViewUiBinder uiBinder = GWT.create(AnalysesViewUiBinder.class);

  public AnalysesView(Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));

    this.translations = translations;

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
        return object.getUpdated();
      }
    }, translations.dateLabel());

    table.addColumn(analysesActionColumn, translations.actionsLabel());
    table.setColumnWidth(table.getColumn(3), 175, com.google.gwt.dom.client.Style.Unit.PX);

    dataProvider.addDataDisplay(table);
  }

  private void initFilter() {
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
    table.showLoadingIndicator(dataProvider);
  }

  @Override
  public void renderRows(OpalAnalysesDto analyses) {
    dataProvider.setList(JsArrays.toList(analyses.getAnalysesArray()));
    dataProvider.refresh();
  }

  @Override
  public void afterRenderRows() {
    table.hideLoadingIndicator();
  }

  @Override
  public HasActionHandler<OpalAnalysisDto> getActionColumn() {
    return analysesActionColumn;
  }
}