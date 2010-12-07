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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter.ActionHandler;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.ListView.Delegate;

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

  // FlowPanel producedReports;

  @UiField
  CellTable<FileDto> producedReportsTable;

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

  private MenuItem update;

  SimplePager<FileDto> pager;

  private HasActionHandler actionsColumn;

  private ReportTemplateDto reportTemplate;

  private Label reportTemplateName;

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
    toolbar.addItem("", actionsMenu = new MenuBar(true)).addStyleName("tools");
    actionsMenu.addStyleName("tools");
  }

  private void initProducedReportsTable() {
    producedReportsTable.addColumn(new TextColumn<FileDto>() {
      @Override
      public String getValue(FileDto file) {
        return new Date((long) file.getLastModifiedTime()).toString();
      }
    }, translations.producedDate());

    actionsColumn = new ActionsColumn();
    producedReportsTable.addColumn((ActionsColumn) actionsColumn, translations.actionsLabel());
    addTablePager();
  }

  private void addTablePager() {
    producedReportsTable.setPageSize(10);
    pager = new SimplePager<FileDto>(producedReportsTable);
    producedReportsTable.setPager(pager);
    ((VerticalPanel) producedReportsTable.getParent()).insert(pager, 0);
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

  private void renderProducedReports(final JsArray<FileDto> files) {
    tabs.setVisible(true);
    producedReportsTable.setDelegate(new Delegate<FileDto>() {

      @Override
      public void onRangeChanged(ListView<FileDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(files, start, length));
      }
    });

    pager.firstPage();
    int pageSize = producedReportsTable.getPageSize();
    producedReportsTable.setData(0, pageSize, JsArrays.toList(files, 0, pageSize));
    producedReportsTable.setDataSize(files.length(), true);
    producedReportsTable.redraw();

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

  // TODO Extract the following ActionsColumn and ActionsCell cells class. These should be part of some generic
  // component. JobListView should also be refactored because it includes similar classes.
  static class ActionsColumn extends Column<FileDto, FileDto> implements HasActionHandler {

    public ActionsColumn() {
      super(new ActionsCell());
    }

    public FileDto getValue(FileDto object) {
      return object;
    }

    public void setActionHandler(ActionHandler actionHandler) {
      ((ActionsCell) getCell()).setActionHandler(actionHandler);
    }
  }

  static class ActionsCell extends AbstractCell<FileDto> {

    private CompositeCell<FileDto> delegateCell;

    private FieldUpdater<FileDto, String> hasCellFieldUpdater;

    private ActionHandler actionHandler;

    public ActionsCell() {
      hasCellFieldUpdater = new FieldUpdater<FileDto, String>() {
        public void update(int rowIndex, FileDto object, String value) {
          if(actionHandler != null) {
            actionHandler.doAction(object, value);
          }
        }
      };
    }

    @Override
    public Object onBrowserEvent(Element parent, FileDto value, Object viewData, NativeEvent event, ValueUpdater<FileDto> valueUpdater) {
      refreshActions(value);

      return delegateCell.onBrowserEvent(parent, value, viewData, event, valueUpdater);
    }

    @Override
    public void render(FileDto value, Object viewData, StringBuilder sb) {
      refreshActions(value);

      delegateCell.render(value, viewData, sb);
    }

    public void setActionHandler(ActionHandler actionHandler) {
      this.actionHandler = actionHandler;
    }

    private void refreshActions(FileDto value) {
      delegateCell = createCompositeCell(DOWNLOAD_ACTION, DELETE_ACTION);
    }

    private CompositeCell<FileDto> createCompositeCell(String... actionNames) {
      List<HasCell<FileDto, ?>> hasCells = new ArrayList<HasCell<FileDto, ?>>();

      final Cell<String> cell = new ClickableTextCell() {

        @Override
        public void render(String value, Object viewData, StringBuilder sb) {
          super.render(translations.actionMap().get(value), viewData, sb);
        }
      };

      for(final String actionName : actionNames) {
        hasCells.add(new HasCell<FileDto, String>() {

          @Override
          public Cell<String> getCell() {
            return cell;
          }

          @Override
          public FieldUpdater<FileDto, String> getFieldUpdater() {
            return hasCellFieldUpdater;
          }

          @Override
          public String getValue(FileDto object) {
            return actionName;
          }
        });
      }

      return new CompositeCell<FileDto>(hasCells);
    }
  }

  @Override
  public HasActionHandler getActionColumn() {
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
      actionsMenu.addItem(remove = new MenuItem(translations.removeLabel(), command));
      actionsMenu.addSeparator(new MenuItemSeparator());
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
  public void setUpdateReportTemplateCommand(Command command) {
    if(update == null) {
      toolbar.addItem(update = new MenuItem("", command)).addStyleName("edit");
    } else {
      update.setCommand(command);
    }
  }

}
