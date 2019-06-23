/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.analysis;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.analysis.component.AnalysisStatusColumn;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginsRepository;
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
import org.obiba.opal.web.model.client.opal.*;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysesView extends ViewWithUiHandlers<AnalysesUiHandlers> implements AnalysesPresenter.Display {

  private static final int NAME_COLUMN_INDEX = 0;

  private static final int TYPE_COLUMN_INDEX = 1;

  private static final int UPDATED_COLUMN_INDEX = 4;

  @UiTemplate("AnalysesView.ui.xml")
  interface AnalysesViewUiBinder extends UiBinder<Widget, AnalysesView> { }

  private ColumnSortEvent.ListHandler<OpalAnalysisDto> tableColumnSortHandler;

  @UiField
  Table<OpalAnalysisDto> table;

  @UiField
  TextBoxClearable filter;

  @UiField
  Button refreshButton;

  @UiField
  Button newButton;

  private ListDataProvider<OpalAnalysisDto> dataProvider = new ListDataProvider<OpalAnalysisDto>();

  private ActionsColumn<OpalAnalysisDto> analysesActionColumn;

  private AnalysisPluginsRepository pluginsRepository;

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

    UpdatedColumn updatedColumn = new UpdatedColumn();
    NameColumn nameColumn = new NameColumn();
    TypeColumn typeColumn = new TypeColumn();

    table.addColumn(nameColumn, translations.analysisNameLabel());
    table.addColumn(typeColumn, translations.analysisTypeLabel());
    table.addColumn(new LastResultCount(), translations.analysisTotalLabel());
    table.addColumn(new AnalysisStatusColumn.ForOpalAnalysisDto(), translations.analysisStatusLabel());
    table.addColumn(updatedColumn, translations.dateLabel());
    table.addColumn(analysesActionColumn, translations.actionsLabel());

    table.setColumnWidth(table.getColumn(5), 175, com.google.gwt.dom.client.Style.Unit.PX);
    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(new InlineLabel(translationMessages.analysisCount(0)));
    dataProvider.addDataDisplay(table);

    initializeSortableColumns(updatedColumn, nameColumn, typeColumn);
  }

  private void initializeSortableColumns(UpdatedColumn updatedColumn, NameColumn nameColumn, TypeColumn typeColumn) {
    tableColumnSortHandler = new ColumnSortEvent.ListHandler<OpalAnalysisDto>(dataProvider.getList());
    table.getHeader(UPDATED_COLUMN_INDEX).setHeaderStyleNames("sortable-header-column");
    table.getHeader(NAME_COLUMN_INDEX).setHeaderStyleNames("sortable-header-column");
    table.getHeader(TYPE_COLUMN_INDEX).setHeaderStyleNames("sortable-header-column");
    tableColumnSortHandler.setComparator(table.getColumn(UPDATED_COLUMN_INDEX), updatedColumn);
    tableColumnSortHandler.setComparator(table.getColumn(NAME_COLUMN_INDEX), nameColumn);
    tableColumnSortHandler.setComparator(table.getColumn(TYPE_COLUMN_INDEX), typeColumn);
    table.getColumnSortList().push(table.getColumn(NAME_COLUMN_INDEX));
    table.getColumnSortList().push(table.getColumn(TYPE_COLUMN_INDEX));
    table.getColumnSortList().push(table.getColumn(UPDATED_COLUMN_INDEX));
    table.addColumnSortHandler(tableColumnSortHandler);
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
  public void setPlugins(List<PluginPackageDto> plugins) {
    pluginsRepository = new AnalysisPluginsRepository(plugins);
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
    tableColumnSortHandler.setList(dataProvider.getList());
    ColumnSortEvent.fire(table, table.getColumnSortList());
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

  private abstract class SortableColumn extends TextColumn<OpalAnalysisDto> implements Comparator<OpalAnalysisDto>  {

    private SortableColumn() {
      setDefaultSortAscending(false);
      setSortable(true);
    }
  }

  private class NameColumn extends SortableColumn {

    @Override
    public String getValue(OpalAnalysisDto dto) {
      return dto.getName();
    }

    @Override
    public int compare(OpalAnalysisDto o1, OpalAnalysisDto o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  private class TypeColumn extends SortableColumn {
    private Map<String, String> titleCache = new HashMap<String, String>();

    @Override
    public String getValue(OpalAnalysisDto dto) {
      return getTitle(dto);
    }

    private String getTitle(OpalAnalysisDto dto) {
      String templateName = dto.getTemplateName();

      if (!titleCache.containsKey(templateName)) {
        AnalysisPluginData analysisPluginData = pluginsRepository.findAnalysisPluginData(dto);
        if (analysisPluginData == null) {
          titleCache.put(templateName, templateName);
        } else {
          titleCache.put(templateName, analysisPluginData.getTemplateDto().getTitle());
        }
      }

      return titleCache.get(templateName);
    }

    @Override
    public int compare(OpalAnalysisDto o1, OpalAnalysisDto o2) {
      return titleCache.get(o1.getTemplateName()).compareTo(titleCache.get(o2.getTemplateName()));
    }
  }

  private class LastResultCount extends TextColumn<OpalAnalysisDto> {
    @Override
    public String getValue(OpalAnalysisDto dto) {
      return dto.hasLastResult() ? getSuccessCount(dto.getLastResult()) : "0 / 0";
    }

    private String getSuccessCount(OpalAnalysisResultDto dto) {
      int successCount = 0;
      for (AnalysisResultItemDto item : JsArrays.toList(dto.getResultItemsArray())) {
        successCount += item.getStatus().getName().equals(AnalysisStatusDto.PASSED.getName()) ? 1 : 0;
      }

      return successCount + " / " + dto.getResultItemsCount();
    }
  }

  private class UpdatedColumn extends SortableColumn implements Comparator<OpalAnalysisDto> {
    @Override
    public String getValue(OpalAnalysisDto object) {
      String date = object.hasLastResult() ? object.getLastResult().getStartDate() : object.getUpdated();
      return Moment.create(date).format(FormatType.MONTH_NAME_TIME_SHORT);
    }

    @Override
    public int compare(OpalAnalysisDto o1, OpalAnalysisDto o2) {
      return (int)(Moment.create(o1.getUpdated()).valueOf() - Moment.create(o2.getUpdated()).valueOf());
    }
  }

}