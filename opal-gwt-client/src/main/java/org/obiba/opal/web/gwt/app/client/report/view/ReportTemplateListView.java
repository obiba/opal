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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateListPresenter;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionModel.SelectionChangeHandler;

public class ReportTemplateListView extends Composite implements ReportTemplateListPresenter.Display {

  @UiTemplate("ReportTemplateListView.ui.xml")
  interface ReportTemplateListViewUiBinder extends UiBinder<Widget, ReportTemplateListView> {
  }

  private static ReportTemplateListViewUiBinder uiBinder = GWT.create(ReportTemplateListViewUiBinder.class);

  @UiField
  CellTable<ReportTemplateDto> reportTemplateTable;

  SingleSelectionModel<ReportTemplateDto> selectionModel;

  public ReportTemplateListView() {
    selectionModel = new SingleSelectionModel<ReportTemplateDto>();
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setReportTemplates(JsArray<ReportTemplateDto> templates) {
    clearSelection();
    int templateCount = templates.length();
    reportTemplateTable.setPageSize(templateCount);
    reportTemplateTable.setDataSize(templateCount, true);
    reportTemplateTable.setData(0, templateCount, JsArrays.toList(templates, 0, templateCount));

    // Select the first element in the list.
    if(templates.length() > 0) {
      selectionModel.setSelected(templates.get(0), true);
    }
  }

  private void clearSelection() {
    if(getSelectedReportTemplate() != null) {
      selectionModel.setSelected(getSelectedReportTemplate(), false);
      reportTemplateTable.redraw();
    }
  }

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
    reportTemplateTable.setSelectionEnabled(true);
    reportTemplateTable.setSelectionModel(selectionModel);
  }

  @Override
  public HandlerRegistration addSelectReportTemplateHandler(SelectionChangeHandler handler) {
    return selectionModel.addSelectionChangeHandler(handler);
  }
}
