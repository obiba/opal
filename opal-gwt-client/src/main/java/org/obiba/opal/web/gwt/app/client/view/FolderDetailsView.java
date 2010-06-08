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
import org.obiba.opal.web.gwt.app.client.presenter.FolderDetailsPresenter.Display;
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 *
 */
public class FolderDetailsView extends Composite implements Display {

  @UiTemplate("FolderDetailsView.ui.xml")
  interface FolderDetailsUiBinder extends UiBinder<DockLayoutPanel, FolderDetailsView> {
  }

  private static FolderDetailsUiBinder uiBinder = GWT.create(FolderDetailsUiBinder.class);

  @UiField
  CellTable<FileDto> table;

  SelectionModel<FileDto> selectionModel = new SingleSelectionModel<FileDto>();

  private Translations translations = GWT.create(Translations.class);

  //
  // Constructors
  //

  public FolderDetailsView() {
    initWidget(uiBinder.createAndBindUi(this));

    initTable();
  }

  //
  // JobListPresenter.Display Methods
  //

  @Override
  public SelectionModel<FileDto> getTableSelection() {
    return null;
  }

  @Override
  public void renderRows(final FileDto rows) {
    table.setData(0, table.getPageSize(), JsArrays.toList(rows.getChildrenArray()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void clear() {
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
  }

  private void addTableColumns() {
    table.addColumn(new TextColumn<FileDto>() {

      @Override
      public String getValue(FileDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<FileDto>() {

      @Override
      public String getValue(FileDto object) {
        return String.valueOf(object.getSize());
      }
    }, translations.sizeLabel());

    table.addColumn(new TextColumn<FileDto>() {

      @Override
      public String getValue(FileDto object) {
        return String.valueOf(object.getLastModifiedTime());
      }
    }, translations.lastModifiedLabel());

  }

}
