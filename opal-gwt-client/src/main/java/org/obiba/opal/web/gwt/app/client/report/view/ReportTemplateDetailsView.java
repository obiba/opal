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

import java.util.Date;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.user.cellview.client.DateTimeColumn;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ReportTemplateDetailsView extends Composite implements ReportTemplateDetailsPresenter.Display {

  @UiTemplate("ReportTemplateDetailsView.ui.xml")
  interface ReportTemplateDetailsViewUiBinder extends UiBinder<Widget, ReportTemplateDetailsView> {
  }

  private static ReportTemplateDetailsViewUiBinder uiBinder = GWT.create(ReportTemplateDetailsViewUiBinder.class);

  @UiField
  CellTable<FileDto> producedReports;

  public ReportTemplateDetailsView() {
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
  public void setProducedReports(JsArray<FileDto> reports) {
    int reportCount = reports.length();
    producedReports.setPageSize(reportCount);
    producedReports.setDataSize(reportCount, true);
    producedReports.setData(0, reportCount, JsArrays.toList(reports, 0, reportCount));
  }

  private void initTable() {
    producedReports.setTitle("Reports");
    producedReports.addColumn(new DateTimeColumn<FileDto>() {

      @Override
      public Date getValue(FileDto reportFile) {
        // return new Date((long) reportFile.getLastModifiedTime());
        return new Date();
      }

    });
  }
}
