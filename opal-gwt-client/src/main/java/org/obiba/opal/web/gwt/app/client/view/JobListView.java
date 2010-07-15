/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import static org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.CANCEL_ACTION;
import static org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.LOG_ACTION;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.ActionHandler;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.Display;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.WorkbenchLayout;
import org.obiba.opal.web.gwt.user.cellview.client.DateTimeColumn;
import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.ListView.Delegate;

/**
 *
 */
public class JobListView extends Composite implements Display {

  @UiTemplate("JobListView.ui.xml")
  interface JobListViewUiBinder extends UiBinder<WorkbenchLayout, JobListView> {
  }

  private static JobListViewUiBinder uiBinder = GWT.create(JobListViewUiBinder.class);

  @UiField
  CellTable<CommandStateDto> table;

  @UiField
  Button clearButton;

  SelectionModel<CommandStateDto> selectionModel = new SingleSelectionModel<CommandStateDto>();

  SimplePager<CommandStateDto> pager;

  private static Translations translations = GWT.create(Translations.class);

  private HasActionHandler actionsColumn;

  //
  // Constructors
  //

  public JobListView() {
    initWidget(uiBinder.createAndBindUi(this));

    initTable();
  }

  //
  // JobListPresenter.Display Methods
  //

  public SelectionModel<CommandStateDto> getTableSelection() {
    return null;
  }

  public void renderRows(final JsArray<CommandStateDto> rows) {
    table.setDelegate(new Delegate<CommandStateDto>() {

      @Override
      public void onRangeChanged(ListView<CommandStateDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(rows, start, length));
      }
    });

    pager.firstPage();
    table.setData(0, table.getPageSize(), JsArrays.toList(rows, 0, table.getPageSize()));
    table.setDataSize(rows.length(), true);
    table.redraw();
  }

  public void showClearJobsButton(boolean show) {
    clearButton.setEnabled(show);
  }

  public HasActionHandler getActionsColumn() {
    return actionsColumn;
  }

  public HandlerRegistration addClearButtonHandler(ClickHandler handler) {
    return clearButton.addClickHandler(handler);
  }

  //
  // Composite Methods
  //

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

  //
  // Methods
  //

  private void initTable() {
    table.setSelectionEnabled(false);
    table.setSelectionModel(selectionModel);

    addTableColumns();
    addTablePager();
  }

  private void addTableColumns() {
    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return String.valueOf(object.getId());
      }
    }, translations.idLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return object.getCommand();
      }
    }, translations.typeLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return object.getOwner();
      }
    }, translations.userLabel());

    table.addColumn(new DateTimeColumn<CommandStateDto>() {
      @Override
      public Date getValue(CommandStateDto object) {
        return object.getStartTime() > 0 ? new Date((long) object.getStartTime()) : null;
      }
    }, translations.startLabel());

    table.addColumn(new DateTimeColumn<CommandStateDto>() {
      @Override
      public Date getValue(CommandStateDto object) {
        return object.getEndTime() > 0 ? new Date((long) object.getEndTime()) : null;
      }
    }, translations.endLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return translations.statusMap().get(object.getStatus());
      }
    }, translations.statusLabel());

    actionsColumn = new ActionsColumn();
    table.addColumn((ActionsColumn) actionsColumn, translations.actionsLabel());
  }

  private void addTablePager() {
    table.setPageSize(50);
    pager = new SimplePager<CommandStateDto>(table);
    table.setPager(pager);
    ((FlowPanel) table.getParent()).insert(pager, 0);
    DOM.removeElementAttribute(pager.getElement(), "style");
    DOM.setStyleAttribute(pager.getElement(), "cssFloat", "right");
  }

  //
  // Inner Classes
  //

  static class ActionsColumn extends Column<CommandStateDto, CommandStateDto> implements HasActionHandler {
    //
    // Constructors
    //

    public ActionsColumn() {
      super(new ActionsCell());
    }

    //
    // Column Methods
    //

    public CommandStateDto getValue(CommandStateDto object) {
      return object;
    }

    //
    // HasActionHandler Methods
    //

    public void setActionHandler(ActionHandler actionHandler) {
      ((ActionsCell) getCell()).setActionHandler(actionHandler);
    }
  }

  static class ActionsCell extends AbstractCell<CommandStateDto> {
    //
    // Instance Variables
    //

    private CompositeCell<CommandStateDto> delegateCell;

    private FieldUpdater<CommandStateDto, String> hasCellFieldUpdater;

    private ActionHandler actionHandler;

    //
    // Constructors
    //

    public ActionsCell() {
      hasCellFieldUpdater = new FieldUpdater<CommandStateDto, String>() {
        public void update(int rowIndex, CommandStateDto object, String value) {
          if(actionHandler != null) {
            actionHandler.doAction(object, value);
          }
        }
      };
    }

    //
    // AbstractCell Methods
    //

    @Override
    public Object onBrowserEvent(Element parent, CommandStateDto value, Object viewData, NativeEvent event, ValueUpdater<CommandStateDto> valueUpdater) {
      refreshActions(value);

      return delegateCell.onBrowserEvent(parent, value, viewData, event, valueUpdater);
    }

    @Override
    public void render(CommandStateDto value, Object viewData, StringBuilder sb) {
      refreshActions(value);

      delegateCell.render(value, viewData, sb);
    }

    //
    // Methods
    //

    public void setActionHandler(ActionHandler actionHandler) {
      this.actionHandler = actionHandler;
    }

    private void refreshActions(CommandStateDto value) {
      if(value.getStatus().toString().equals("NOT_STARTED") || value.getStatus().toString().equals("IN_PROGRESS")) {
        delegateCell = createCompositeCell(LOG_ACTION, CANCEL_ACTION);
      } else {
        delegateCell = createCompositeCell(LOG_ACTION);
      }
    }

    private CompositeCell<CommandStateDto> createCompositeCell(String... actionNames) {
      List<HasCell<CommandStateDto, ?>> hasCells = new ArrayList<HasCell<CommandStateDto, ?>>();

      final Cell<String> cell = new ClickableTextCell() {

        @Override
        public void render(String value, Object viewData, StringBuilder sb) {
          super.render(translations.actionMap().get(value), viewData, sb);
        }
      };

      for(final String actionName : actionNames) {
        hasCells.add(new HasCell<CommandStateDto, String>() {

          @Override
          public Cell<String> getCell() {
            return cell;
          }

          @Override
          public FieldUpdater<CommandStateDto, String> getFieldUpdater() {
            return hasCellFieldUpdater;
          }

          @Override
          public String getValue(CommandStateDto object) {
            return actionName;
          }
        });
      }

      return new CompositeCell<CommandStateDto>(hasCells);
    }
  }
}
