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

import static org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter.DOWNLOAD_ACTION;

import java.util.Date;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.MenuItemAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Widget;

public class ReportTemplateDetailsView extends Composite implements ReportTemplateDetailsPresenter.Display {

  @UiTemplate("ReportTemplateDetailsView.ui.xml")
  interface ReportTemplateDetailsViewUiBinder extends UiBinder<Widget, ReportTemplateDetailsView> {
  }

  private static ReportTemplateDetailsViewUiBinder uiBinder = GWT.create(ReportTemplateDetailsViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  Label noReportTemplatesLabel;

  @UiField
  HTMLPanel reportTemplatePanel;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  CellTable<FileDto> producedReportsTable;

  @UiField
  SimplePager pager;

  @UiField
  InlineLabel noReports;

  @UiField
  FlowPanel reportTemplateDetails;

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
  FlowPanel toolbarPanel;

  private MenuBar toolbar;

  private MenuBar actionsMenu;

  private MenuItem remove;

  private MenuItem run;

  private MenuItem downloadReportDesign;

  private MenuItem update;

  JsArrayDataProvider<FileDto> dataProvider = new JsArrayDataProvider<FileDto>();

  private HasActionHandler<FileDto> actionsColumn;

  private ReportTemplateDto reportTemplate;

  private Label reportTemplateName;

  private MenuItem toolsItem;

  private MenuItemSeparator removeSeparator;

  public ReportTemplateDetailsView() {
    initWidget(uiBinder.createAndBindUi(this));
    initProducedReportsTable();
    initActionToolbar();
  }

  private void initActionToolbar() {
    toolbarPanel.add(reportTemplateName = new Label());
    reportTemplateName.addStyleName("title");
    toolbarPanel.add(toolbar = new MenuBar());
    toolbar.setAutoOpen(true);
    toolsItem = toolbar.addItem("", actionsMenu = new MenuBar(true));
    toolsItem.addStyleName("tools");
    actionsMenu.addStyleName("tools");
  }

  private void initProducedReportsTable() {
    producedReportsTable.addColumn(new TextColumn<FileDto>() {
      @Override
      public String getValue(FileDto file) {
        return new Date((long) file.getLastModifiedTime()).toString();
      }
    }, translations.producedDate());

    actionsColumn = new ActionsColumn<FileDto>(DOWNLOAD_ACTION, DELETE_ACTION);
    producedReportsTable.addColumn((ActionsColumn) actionsColumn, translations.actionsLabel());
    dataProvider.addDataDisplay(producedReportsTable);
    addTablePager();
  }

  private void addTablePager() {
    producedReportsTable.setPageSize(10);
    pager.setDisplay(producedReportsTable);
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
  public void setReportTemplatesAvailable(boolean available) {
    toolbarPanel.setVisible(available);
    reportTemplatePanel.setVisible(available);
    tabs.setVisible(available);
    noReportTemplatesLabel.setVisible(available == false);
  }

  @Override
  public void setProducedReports(final JsArray<FileDto> files) {
    pager.setVisible(files.length() != 0); // OPAL-901
    renderProducedReports(files);
  }

  private void renderProducedReports(JsArray<FileDto> files) {
    dataProvider.setArray(files);
    pager.firstPage();
    dataProvider.refresh();

    producedReportsTable.setVisible(files.length() > 0);
    pager.setVisible(files.length() > 0);
    noReports.setVisible(files.length() == 0);
  }

  @Override
  public void setReportTemplateDetails(ReportTemplateDto reportTemplate) {
    if(reportTemplate != null) {
      renderReportTemplateDetails(reportTemplate);
    }
  }

  private void renderReportTemplateDetails(ReportTemplateDto reportTemplate) {
    reportTemplateDetails.setVisible(true);
    this.reportTemplate = reportTemplate;
    design.setText(reportTemplate.getDesign());
    schedule.setText(reportTemplate.getCron());
    format.setText(reportTemplate.getFormat());
    parameters.setText(getReportParamsList(JsArrays.toSafeArray(reportTemplate.getParametersArray())));
    emails.setText(getEmailList(JsArrays.toSafeArray(reportTemplate.getEmailNotificationArray())));
    reportTemplateName.setText(reportTemplate.getName());
  }

  private String getEmailList(JsArrayString emails) {
    StringBuilder emailList = new StringBuilder();
    for(int i = 0; i < emails.length(); i++) {
      emailList.append(emails.get(i) + " ");
    }
    return emailList.toString();
  }

  private String getReportParamsList(JsArray<ParameterDto> params) {
    StringBuilder paramList = new StringBuilder();
    for(ParameterDto param : JsArrays.toIterable(params)) {
      paramList.append(param.getKey() + "=" + param.getValue() + " ");
    }
    return paramList.toString();
  }

  @Override
  public HasActionHandler<FileDto> getActionColumn() {
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
  public void setRemoveReportTemplateCommand(Command command) {
    if(remove == null) {
      removeSeparator = actionsMenu.addSeparator(new MenuItemSeparator());
      actionsMenu.addItem(remove = new MenuItem(translations.removeLabel(), command));
    } else {
      remove.setCommand(command);
    }
  }

  @Override
  public void setRunReportCommand(Command command) {
    if(run == null) {
      actionsMenu.addItem(run = new MenuItem(translations.runLabel(), command));
    } else {
      run.setCommand(command);
    }
  }

  @Override
  public void setDownloadReportDesignCommand(Command command) {
    if(downloadReportDesign == null) {
      actionsMenu.addItem(downloadReportDesign = new MenuItem(translations.downloadReportDesignLabel(), command));
    } else {
      downloadReportDesign.setCommand(command);
    }
  }

  @Override
  public void setUpdateReportTemplateCommand(Command command) {
    if(update == null) {
      toolbar.addItem(update = new MenuItem("", command)).addStyleName("edit");
    } else {
      update.setCommand(command);
    }
  }

  @Override
  public HasAuthorization getRemoveReportTemplateAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(toolsItem), new MenuItemAuthorizer(remove), new UIObjectAuthorizer(removeSeparator)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getRunReportAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(toolsItem), new MenuItemAuthorizer(run)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getDownloadReportDesignAuthorizer() {
    return new CompositeAuthorizer(new MenuItemAuthorizer(toolsItem), new MenuItemAuthorizer(downloadReportDesign)) {
      @Override
      public void unauthorized() {
      }
    };
  }

  @Override
  public HasAuthorization getUpdateReportTemplateAuthorizer() {
    return new MenuItemAuthorizer(update);
  }

  @Override
  public HasAuthorization getListReportsAuthorizer() {
    return new WidgetAuthorizer(tabs);
  }

}
