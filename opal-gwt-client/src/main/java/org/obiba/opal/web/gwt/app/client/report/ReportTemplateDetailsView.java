/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.report;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.DateTimeColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static org.obiba.opal.web.gwt.app.client.report.ReportTemplateDetailsPresenter.DOWNLOAD_ACTION;

public class ReportTemplateDetailsView extends ViewWithUiHandlers<ReportTemplateDetailsUiHandlers>
    implements ReportTemplateDetailsPresenter.Display {

  interface Binder extends UiBinder<Widget, ReportTemplateDetailsView> {}

  private final Translations translations;

  @UiField
  IconAnchor edit;

  @UiField
  Button remove;

  @UiField
  Button download;

  @UiField
  Button execute;

  @UiField
  Panel detailsPanel;

  @UiField
  Heading reportTemplateName;

  @UiField
  Panel reportTemplatePanel;

  @UiField
  Panel reportsPanel;

  @UiField
  Panel permissionsPanel;

  @UiField
  CellTable<ReportDto> producedReportsTable;

  @UiField
  Panel permissions;

  @UiField
  OpalSimplePager pager;

  @UiField
  InlineLabel noReports;

  @UiField
  Anchor design;

  @UiField
  Label schedule;

  @UiField
  Label format;

  @UiField
  Label parameters;

  @UiField
  Label emails;

  @UiField
  Label failureEmails;

  JsArrayDataProvider<ReportDto> dataProvider = new JsArrayDataProvider<ReportDto>();

  private HasActionHandler<ReportDto> actionsColumn;

  private ReportTemplateDto reportTemplate;

  @Inject
  public ReportTemplateDetailsView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    initProducedReportsTable();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    permissions.clear();
    if(content != null) {
      permissions.add(content.asWidget());
    }
  }

  private void initProducedReportsTable() {
    TextColumn<ReportDto> nameColumn;
    producedReportsTable.addColumn(nameColumn = new TextColumn<ReportDto>() {

      @Override
      public String getValue(ReportDto object) {
        return object.getName();
      }
    }, translations.nameLabel());
    nameColumn.setDefaultSortAscending(false);
    nameColumn.setSortable(true);

    producedReportsTable.addColumn(new DateTimeColumn<ReportDto>() {
      @Override
      public Date getValue(ReportDto file) {
        return new Date((long) file.getLastModifiedTime());
      }
    }, translations.lastModifiedLabel());

    actionsColumn = new ActionsColumn<ReportDto>(DOWNLOAD_ACTION, ActionsColumn.REMOVE_ACTION);
    producedReportsTable.addColumn((ActionsColumn) actionsColumn, translations.actionsLabel());
    producedReportsTable.setEmptyTableWidget(noReports);
    dataProvider.addDataDisplay(producedReportsTable);
    addTablePager();
  }

  private void addTablePager() {
    producedReportsTable.setPageSize(10);
    pager.setDisplay(producedReportsTable);
  }

  @Override
  public void setProducedReports(JsArray<ReportDto> reports) {
    pager.setPagerVisible(reports.length() > 10); // OPAL-901
    renderProducedReports(reports);
  }

  private void renderProducedReports(JsArray<ReportDto> reports) {
    dataProvider.setArray(reports);
    pager.firstPage();
    dataProvider.refresh();

    producedReportsTable.addColumnSortHandler(new ReportColumnSortHandler(dataProvider.getList()));

    Collections.sort(dataProvider.getList(), ReportColumnSortHandler.ASCENDING_COMPARATOR);
  }

  @Override
  public void setReportTemplateDetails(ReportTemplateDto reportTemplate) {
    if(reportTemplate != null) {
      renderReportTemplateDetails(reportTemplate);
    }
    detailsPanel.setVisible(reportTemplate != null);
  }

  private void renderReportTemplateDetails(ReportTemplateDto reportTemplate) {
    this.reportTemplate = reportTemplate;
    design.setText(reportTemplate.getDesign());
    schedule.setText(reportTemplate.getCron());
    format.setText(reportTemplate.getFormat());
    parameters.setText(getReportParamsList(JsArrays.toSafeArray(reportTemplate.getParametersArray())));
    emails.setText(getEmailList(JsArrays.toSafeArray(reportTemplate.getEmailNotificationArray())));
    failureEmails.setText(getEmailList(JsArrays.toSafeArray(reportTemplate.getFailureEmailNotificationArray())));
    reportTemplateName.setText(reportTemplate.getName());
  }

  private String getEmailList(JsArrayString emails) {
    StringBuilder emailList = new StringBuilder();
    for(int i = 0; i < emails.length(); i++) {
      emailList.append(emails.get(i)).append(" ");
    }
    return emailList.toString();
  }

  private String getReportParamsList(JsArray<ParameterDto> params) {
    StringBuilder paramList = new StringBuilder();
    boolean appended = false;
    for(ParameterDto param : JsArrays.toIterable(params)) {
      if(appended) {
        paramList.append(", ");
      }
      paramList.append(param.getKey()).append("=")
          .append(ROptionsHelper.renderROptionValue(param.getKey(), param.getValue()));
      appended = true;
    }
    return paramList.toString();
  }

  @Override
  public HasActionHandler<ReportDto> getActionColumn() {
    return actionsColumn;
  }

  @Override
  public ReportTemplateDto getReportTemplateDetails() {
    return reportTemplate;
  }

  @Override
  public HandlerRegistration addReportDesignClickHandler(ClickHandler handler) {
    return design.addClickHandler(handler);
  }

  @Override
  public HasAuthorization getListReportsAuthorizer() {
    return new WidgetAuthorizer(reportsPanel);
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

  @Override
  public void setVisiblePermissionsPanel(boolean value) {
    permissionsPanel.setVisible(value);
  }

  @Override
  public HasAuthorization getExecuteReportAuthorizer() {
    return new WidgetAuthorizer(execute);
  }

  @Override
  public HasAuthorization getDownloadReportDesignAuthorizer() {
    return new WidgetAuthorizer(download);
  }

  @Override
  public HasAuthorization getRemoveReportTemplateAuthorizer() {
    return new WidgetAuthorizer(remove);
  }

  @Override
  public HasAuthorization getUpdateReportTemplateAuthorizer() {
    return new WidgetAuthorizer(edit);
  }

  @UiHandler("edit")
  public void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  @UiHandler("remove")
  public void onDelete(ClickEvent event) {
    getUiHandlers().onDelete();
  }

  @UiHandler("download")
  public void onDownload(ClickEvent event) {
    getUiHandlers().onDownload();
  }

  @UiHandler("execute")
  public void onExecute(ClickEvent event) {
    getUiHandlers().onExecute();
  }

  private static class ReportColumnSortHandler extends ColumnSortEvent.ListHandler<ReportDto> {

    final static Comparator<ReportDto> ASCENDING_COMPARATOR = new Comparator<ReportDto>() {
      @Override
      public int compare(ReportDto o1, ReportDto o2) {
        return ComparisonChain.start().compare(o1.getName(), o2.getName()).result();
      }
    };

    final static Comparator<ReportDto> DESCENDING_COMPARATOR = new Comparator<ReportDto>() {
      @Override
      public int compare(ReportDto o1, ReportDto o2) {
        return ComparisonChain.start().compare(o1.getName(), o2.getName(), Ordering.natural().reverse()).result();
      }
    };

    private ReportColumnSortHandler(List<ReportDto> list) {
      super(list);
    }

    @Override
    public void onColumnSort(ColumnSortEvent event) {
      // Get the sorted column.
      Column<?, ?> column = event.getColumn();
      if(column == null) {
        return;
      }

      Collections.sort(getList(), event.isSortAscending() ? ASCENDING_COMPARATOR : DESCENDING_COMPARATOR);
    }
  }

}
