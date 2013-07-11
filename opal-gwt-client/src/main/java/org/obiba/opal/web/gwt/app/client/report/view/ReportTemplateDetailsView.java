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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.DateTimeColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.MenuItemAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.TabAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter.DOWNLOAD_ACTION;

public class ReportTemplateDetailsView extends ViewImpl implements ReportTemplateDetailsPresenter.Display {

  @UiTemplate("ReportTemplateDetailsView.ui.xml")
  interface ReportTemplateDetailsViewUiBinder extends UiBinder<Widget, ReportTemplateDetailsView> {}

  private static final ReportTemplateDetailsViewUiBinder uiBinder = GWT.create(ReportTemplateDetailsViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Label noReportTemplatesLabel;

  @UiField
  Panel reportTemplatePanel;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  CellTable<ReportDto> producedReportsTable;

  @UiField
  Panel permissions;

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

  JsArrayDataProvider<ReportDto> dataProvider = new JsArrayDataProvider<ReportDto>();

  private HasActionHandler<ReportDto> actionsColumn;

  private ReportTemplateDto reportTemplate;

  private Label reportTemplateName;

  private MenuItem toolsItem;

  private MenuItemSeparator removeSeparator;

  public ReportTemplateDetailsView() {
    widget = uiBinder.createAndBindUi(this);
    initProducedReportsTable();
    initActionToolbar();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    permissions.clear();
    if(content != null) {
      permissions.add(content.asWidget());
    }
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
    producedReportsTable.addColumn(new TextColumn<ReportDto>() {

      @Override
      public String getValue(ReportDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    producedReportsTable.addColumn(new DateTimeColumn<ReportDto>() {
      @Override
      public Date getValue(ReportDto file) {
        return new Date((long) file.getLastModifiedTime());
      }
    }, translations.lastModifiedLabel());

    actionsColumn = new ActionsColumn<ReportDto>(DOWNLOAD_ACTION, DELETE_ACTION);
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
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setReportTemplatesAvailable(boolean available) {
    toolbarPanel.setVisible(available);
    reportTemplatePanel.setVisible(available);
    tabs.setVisible(available);
    noReportTemplatesLabel.setVisible(!available);
  }

  @Override
  public void setProducedReports(JsArray<ReportDto> reports) {
    pager.setVisible(reports.length() > 10); // OPAL-901
    renderProducedReports(reports);
  }

  private void renderProducedReports(JsArray<ReportDto> reports) {
    dataProvider.setArray(reports);
    pager.firstPage();
    dataProvider.refresh();
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
      emailList.append(emails.get(i)).append(" ");
    }
    return emailList.toString();
  }

  private String getReportParamsList(JsArray<ParameterDto> params) {
    StringBuilder paramList = new StringBuilder();
    for(ParameterDto param : JsArrays.toIterable(params)) {
      paramList.append(param.getKey()).append("=").append(param.getValue()).append(" ");
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
    return new CompositeAuthorizer(new MenuItemAuthorizer(toolsItem), new MenuItemAuthorizer(remove),
        new UIObjectAuthorizer(removeSeparator)) {
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

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabAuthorizer(tabs, 1);
  }

}
