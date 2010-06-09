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

import java.util.Date;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.FolderDetailsPresenter.Display;
import org.obiba.opal.web.gwt.app.client.presenter.FolderDetailsPresenter.HasFieldUpdater;
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class FolderDetailsView extends Composite implements Display {

  @UiTemplate("FolderDetailsView.ui.xml")
  interface FolderDetailsUiBinder extends UiBinder<DockLayoutPanel, FolderDetailsView> {
  }

  private static FolderDetailsUiBinder uiBinder = GWT.create(FolderDetailsUiBinder.class);

  @UiField
  CellTable<FileDto> table;

  private FileNameColumn fileNameColumn;

  private Translations translations = GWT.create(Translations.class);

  public FolderDetailsView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  @Override
  public void renderRows(final FileDto folder) {
    table.setData(0, table.getPageSize(), JsArrays.toList(folder.getChildrenArray()));
    table.setDataSize(folder.getChildrenArray().length(), true);
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

  private void initTable() {
    addTableColumns();
  }

  private void addTableColumns() {
    table.addColumn(fileNameColumn = new FileNameColumn() {

      @Override
      public String getValue(FileDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<FileDto>() {

      @Override
      public String getValue(FileDto object) {
        return isFolder(object) ? String.valueOf((long) object.getSize()) : "";
      }
    }, translations.sizeLabel());

    table.addColumn(new LastModifiedTimeColumn() {

      @Override
      public Date getValue(FileDto object) {
        return new Date((long) object.getLastModifiedTime());
      }
    }, translations.lastModifiedLabel());

  }

  private abstract class FileNameColumn extends Column<FileDto, String> implements HasFieldUpdater {
    public FileNameColumn() {
      super(new ClickableTextCell());
    }
  }

  private static abstract class LastModifiedTimeColumn extends Column<FileDto, Date> {
    public LastModifiedTimeColumn() {
      super(new DateCell());
    }
  }

  private boolean isFolder(FileDto file) {
    // TODO Replace the expression below by the text in comment if we find a solution to the enum bug in protobuf
    /* file.getType() == FileDto.FileType.FILE */
    return file.getSize() > 0;
  }

  @Override
  public HasFieldUpdater getFileNameColumn() {
    return fileNameColumn;
  }
}
