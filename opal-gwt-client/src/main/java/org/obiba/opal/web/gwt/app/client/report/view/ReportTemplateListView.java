/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.view;

import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateListPresenter;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.gwtplatform.mvp.client.ViewImpl;

public class ReportTemplateListView extends ViewImpl implements ReportTemplateListPresenter.Display {

  @UiTemplate("ReportTemplateListView.ui.xml")
  interface ReportTemplateListViewUiBinder extends UiBinder<Widget, ReportTemplateListView> {}

  private static final ReportTemplateListViewUiBinder uiBinder = GWT.create(ReportTemplateListViewUiBinder.class);

    private final Widget widget;
 @UiField
  CellTable<ReportTemplateDto> reportTemplateTable;

  SingleSelectionModel<ReportTemplateDto> selectionModel;

  JsArrayDataProvider<ReportTemplateDto> dataProvider = new JsArrayDataProvider<ReportTemplateDto>();

  public ReportTemplateListView() {
    selectionModel = new SingleSelectionModel<ReportTemplateDto>();
    widget = uiBinder.createAndBindUi(this);
    initTable();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setReportTemplates(JsArray<ReportTemplateDto> templates) {
    clearSelection();
    int templateCount = templates.length();
    dataProvider.setArray(templates);
    reportTemplateTable.setPageSize(templateCount);

    // Select the first element in the list.
    if(templates.length() > 0) {
      selectionModel.setSelected(templates.get(0), true);
    }
  }

  @Override
  public void select(ReportTemplateDto reportTemplateDto) {
    // Clear current selection.
    ReportTemplateDto currentSelection = selectionModel.getSelectedObject();
    if(currentSelection != null) {
      selectionModel.setSelected(currentSelection, false);
    }

    // Find and select specified template.
    for(ReportTemplateDto r : reportTemplateTable.getVisibleItems()) {
      if(r.getName().equals(reportTemplateDto.getName())) {
        selectionModel.setSelected(r, true);
        break;
      }
    }
  }

  private void clearSelection() {
    if(getSelectedReportTemplate() != null) {
      selectionModel.setSelected(getSelectedReportTemplate(), false);
      reportTemplateTable.redraw();
    }
  }

  @Override
  public ReportTemplateDto getSelectedReportTemplate() {
    return selectionModel.getSelectedObject();
  }

  private void initTable() {

    reportTemplateTable.setStyleName("selection-list");
    reportTemplateTable.addColumn(new TextColumn<ReportTemplateDto>() {
      @Override
      public String getValue(ReportTemplateDto dto) {
        return dto.getName();
      }
    });
    reportTemplateTable.setSelectionModel(selectionModel);
    dataProvider.addDataDisplay(reportTemplateTable);
  }

  @Override
  public HandlerRegistration addSelectReportTemplateHandler(SelectionChangeEvent.Handler handler) {
    return selectionModel.addSelectionChangeHandler(handler);
  }
}
