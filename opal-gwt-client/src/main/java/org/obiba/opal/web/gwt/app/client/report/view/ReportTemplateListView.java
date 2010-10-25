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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ReportTemplateListView extends Composite implements ReportTemplateListPresenter.Display {

  @UiTemplate("ReportTemplateListView.ui.xml")
  interface ReportTemplateListViewUiBinder extends UiBinder<Widget, ReportTemplateListView> {
  }

  private static ReportTemplateListViewUiBinder uiBinder = GWT.create(ReportTemplateListViewUiBinder.class);

  @UiField
  CellTable<ReportTemplateDto> reportTemplateTable;

  public ReportTemplateListView() {
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
    int templateCount = templates.length();
    reportTemplateTable.setPageSize(templateCount);
    reportTemplateTable.setDataSize(templateCount, true);
    reportTemplateTable.setData(0, templateCount, JsArrays.toList(templates, 0, templateCount));
  }

  private void initTable() {
    reportTemplateTable.addColumn(new TextColumn<ReportTemplateDto>() {
      @Override
      public String getValue(ReportTemplateDto dto) {
        return dto.getName();
      }
    });
  }

}
