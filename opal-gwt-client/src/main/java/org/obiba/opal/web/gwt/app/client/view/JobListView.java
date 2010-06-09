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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.Display;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter.HasFieldUpdater;
import org.obiba.opal.web.model.client.CommandStateDto;

import com.google.gwt.cell.client.ClickableTextCell;
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

  private Translations translations = GWT.create(Translations.class);

  private HasFieldUpdater<CommandStateDto, String> statusColumn;

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
  }

  @SuppressWarnings("unchecked")
  @Override
  public void clear() {
    renderRows((JsArray<CommandStateDto>) JavaScriptObject.createArray());
  }

  public HasFieldUpdater<CommandStateDto, String> getStatusColumn() {
    return statusColumn;
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
    statusColumn = new StatusColumn();
    table.addColumn((StatusColumn) statusColumn, translations.idLabel());

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
        // TODO: Return String representation of object.getStatus().
        return "Not Implemented";
      }
    }, translations.statusLabel());

    table.addColumn(new TextColumn<CommandStateDto>() {
      @Override
      public String getValue(CommandStateDto object) {
        return "Not Implemented";
      }
    }, translations.endLabel());
  }

  private void addTablePager() {
    table.setPageSize(20);
    pager = new SimplePager<CommandStateDto>(table);
    table.setPager(pager);
    ((VerticalPanel) table.getParent()).insert(pager, 0);
  }

  //
  // Inner Classes
  //

  static class StatusColumn extends Column<CommandStateDto, String> implements HasFieldUpdater<CommandStateDto, String> {
    //
    // Constructors
    //

    public StatusColumn() {
      super(new ClickableTextCell());
    }

    //
    // Column Methods
    //

    public String getValue(CommandStateDto object) {
      return String.valueOf(object.getId());
    }
  }
}
