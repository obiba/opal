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
import static org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.DELETE_ACTION;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.ActionHandler;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.Display;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.model.client.CommandStateDto;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
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
  interface JobListViewUiBinder extends UiBinder<DockLayoutPanel, JobListView> {
  }

  private static JobListViewUiBinder uiBinder = GWT.create(JobListViewUiBinder.class);

  @UiField
  Label title;

  @UiField
  CellTable<CommandStateDto> table;

  SelectionModel<CommandStateDto> selectionModel = new SingleSelectionModel<CommandStateDto>();

  SimplePager<CommandStateDto> pager;

  private static Translations translations = GWT.create(Translations.class);

  private HasFieldUpdater<CommandStateDto, String> idColumn;

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

  @Override
  public SelectionModel<CommandStateDto> getTableSelection() {
    return null;
  }

  @Override
  public void renderRows(final JsArray<CommandStateDto> rows) {
    title.setText(translations.jobsLabel() + " (" + rows.length() + ")");

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

  @SuppressWarnings("unchecked")
  @Override
  public void clear() {
    renderRows((JsArray<CommandStateDto>) JavaScriptObject.createArray());
  }

  public HasFieldUpdater<CommandStateDto, String> getIdColumn() {
    return idColumn;
  }

  public HasActionHandler getActionsColumn() {
    return actionsColumn;
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
    idColumn = new IdColumn();
    table.addColumn((IdColumn) idColumn, translations.idLabel());

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

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return object.getStartTime() != null ? object.getStartTime() : "";
      }
    }, translations.startLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return object.getEndTime() != null ? object.getEndTime() : "";
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
    ((VerticalPanel) table.getParent()).insert(pager, 0);
  }

  //
  // Inner Classes
  //

  static class IdColumn extends Column<CommandStateDto, String> implements HasFieldUpdater<CommandStateDto, String> {
    //
    // Constructors
    //

    public IdColumn() {
      super(new ClickableTextCell());
    }

    //
    // Column Methods
    //

    public String getValue(CommandStateDto object) {
      return String.valueOf(object.getId());
    }
  }

  static class ActionsColumn extends Column<CommandStateDto, CommandStateDto> implements HasActionHandler {
    //
    // Instance Variables
    //

    private ActionHandler actionHandler;

    private FieldUpdater<CommandStateDto, String> cellContainerFieldUpdater;

    //
    // Constructors
    //

    public ActionsColumn() {
      super(new ActionsCell());

      addActionCell(CANCEL_ACTION);
      addActionCell(DELETE_ACTION);

      cellContainerFieldUpdater = new FieldUpdater<CommandStateDto, String>() {
        public void update(int rowIndex, CommandStateDto object, String value) {
          if(actionHandler != null) {
            actionHandler.doAction(object, value);
          }
        }
      };
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
      this.actionHandler = actionHandler;
    }

    //
    // Methods
    //

    private void addActionCell(final String actionName) {
      final Cell<String> cell = new ClickableTextCell() {

        @Override
        public void render(String value, Object viewData, StringBuilder sb) {
          super.render(translations.actionMap().get(value), viewData, sb);
        }
      };

      ((ActionsCell) getCell()).addHasCell(new HasCell<CommandStateDto, String>() {

        @Override
        public Cell<String> getCell() {
          return cell;
        }

        @Override
        public FieldUpdater<CommandStateDto, String> getFieldUpdater() {
          return cellContainerFieldUpdater;
        }

        @Override
        public String getValue(CommandStateDto object) {
          return actionName;
        }
      });
    }
  }

  static class ActionsCell extends CompositeCell<CommandStateDto> {

    private List<HasCell<CommandStateDto, ?>> hasCells;

    public ActionsCell() {
      hasCells = new ArrayList<HasCell<CommandStateDto, ?>>();
    }

    @Override
    public void addHasCell(HasCell<CommandStateDto, ?> hasCell) {
      hasCells.add(hasCell);

      super.addHasCell(hasCell);
    }

    @Override
    public void removeHasCell(HasCell<CommandStateDto, ?> hasCell) {
      hasCells.remove(hasCell);

      super.removeHasCell(hasCell);
    }

    @Override
    public void render(CommandStateDto value, Object viewData, StringBuilder sb) {
      // Remove all actions.
      super.removeHasCell(getHasCell(0));
      super.removeHasCell(getHasCell(1));

      // Add back only valid actions (i.e., given the current job status).
      if(value.getStatus().toString().equals("NOT_STARTED") || value.getStatus().toString().equals("IN_PROGRESS")) {
        super.addHasCell(getHasCell(0));
      }
      if(value.getStatus().toString().equals("SUCCEEDED") || value.getStatus().toString().equals("FAILED") || value.getStatus().toString().equals("CANCELED")) {
        super.addHasCell(getHasCell(1));
      }

      super.render(value, viewData, sb);
    }

    public HasCell<CommandStateDto, ?> getHasCell(int index) {
      return hasCells.get(index);
    }
  }
}
