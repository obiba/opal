/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.

 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.FileSelectionHandler;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.resources.OpalResources;
import org.obiba.opal.web.gwt.user.cellview.client.DateTimeColumn;
import org.obiba.opal.web.model.client.FileDto;
import org.obiba.opal.web.model.client.FileDto.FileType;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class FolderDetailsView extends Composite implements Display {

  private static final long KB = 1024l;

  private static final long MB = KB * KB;

  private static final long GB = MB * KB;

  private static final String FOLDER_UP_HTML = "<img src=\"image/folder-up.png\" />";

  @UiTemplate("FolderDetailsView.ui.xml")
  interface FolderDetailsUiBinder extends UiBinder<Widget, FolderDetailsView> {
  }

  private static FolderDetailsUiBinder uiBinder = GWT.create(FolderDetailsUiBinder.class);

  @UiField
  CellTable<FileDto> table;

  private SingleSelectionModel<FileDto> tableSelectionModel;

  private FileNameColumn fileNameColumn;

  private Translations translations = GWT.create(Translations.class);

  private boolean displaysFiles = true;

  public FolderDetailsView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  public void setDisplaysFiles(boolean display) {
    displaysFiles = display;
  }

  public void setSelectionEnabled(boolean enabled) {
    table.setSelectionEnabled(enabled);
  }

  public void clearSelection() {
    if(table.isSelectionEnabled()) {
      getTableSelectionModel().setSelected(getTableSelectionModel().getSelectedObject(), false);
    }
  }

  @SuppressWarnings("unchecked")
  public void renderRows(final FileDto folder) {
    JsArray<FileDto> children = (folder.getChildrenCount() != 0) ? filterChildren(folder.getChildrenArray()) : (JsArray<FileDto>) JsArray.createArray();

    if(!folder.getName().equals("root")) {
      FileDto parent = FileDtos.getParent(folder);
      parent.setName("..");
      children.set(0, parent);
    }

    int fileCount = children.length();
    table.setPageSize(fileCount);
    table.setDataSize(fileCount, true);
    table.setData(0, fileCount, sortFileList(JsArrays.toList(children, 0, fileCount)));
  }

  @SuppressWarnings("unchecked")
  private JsArray<FileDto> filterChildren(JsArray<FileDto> children) {
    if(displaysFiles) {
      return children;
    }

    JsArray<FileDto> foldersOnly = (JsArray<FileDto>) JsArray.createArray();
    for(int i = 0; i < children.length(); i++) {
      FileDto child = children.get(i);
      if(child.getType().isFileType(FileType.FOLDER)) {
        foldersOnly.push(child);
      }
    }

    return foldersOnly;
  }

  public void addFileSelectionHandler(FileSelectionHandler fileSelectionHandler) {
    fileNameColumn.addFileSelectionHandler(fileSelectionHandler);
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

    table.addStyleName("folder-details");
    OpalResources.INSTANCE.css().ensureInjected();

    tableSelectionModel = new SingleSelectionModel<FileDto>();
    table.setSelectionModel(tableSelectionModel);
  }

  private void addTableColumns() {
    table.addColumn(fileNameColumn = new FileNameColumn() {

      @Override
      public String getValue(FileDto dto) {
        if(dto.getName().equals("..")) {
          return FOLDER_UP_HTML;
        } else {
          return createFolderChildHtml(dto);
        }
      }

      private String createFolderChildHtml(FileDto dto) {
        StringBuilder sb = new StringBuilder();

        sb.append("<span class=\"");
        if(dto.getType().isFileType(FileDto.FileType.FILE)) {
          sb.append("file\">");
        } else {
          sb.append("folder\">");
        }
        sb.append(dto.getName());
        sb.append("</span>");

        return sb.toString();
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<FileDto>() {

      @Override
      public String getValue(FileDto object) {
        return object.getType().isFileType(FileDto.FileType.FILE) ? getFileSizeWithUnit(object) : "";
      }

      private String getFileSizeWithUnit(FileDto object) {
        double fileSize = object.getSize();
        if(fileSize < KB) {
          return ((long) fileSize) + " B";
        } else if(fileSize < MB) {
          double fileSizeInKB = fileSize / KB;
          long iPart = (long) fileSizeInKB;
          long fPart = Math.round((fileSizeInKB - iPart) * 10);
          return iPart + "." + fPart + " KB";
        } else if(fileSize < GB) {
          double fileSizeInMB = fileSize / MB;
          long iPart = (long) fileSizeInMB;
          long fPart = Math.round((fileSizeInMB - iPart) * 10);
          return iPart + "." + fPart + " MB";
        } else {
          double fileSizeInGB = fileSize / GB;
          long iPart = (long) fileSizeInGB;
          long fPart = Math.round((fileSizeInGB - iPart) * 10);
          return iPart + "." + fPart + " GB";
        }
      }
    }, translations.sizeLabel());

    table.addColumn(new DateTimeColumn<FileDto>() {

      @Override
      public Date getValue(FileDto object) {
        return new Date((long) object.getLastModifiedTime());
      }
    }, translations.lastModifiedLabel());

  }

  /**
   * Returns a sorted copy of the specified file list.
   * 
   * This method simply puts folders ahead of regular files; no additional sorting is performed.
   * 
   * @param fileList the list to be sorted
   * @return the sorted list
   */
  private List<FileDto> sortFileList(List<FileDto> fileList) {
    List<FileDto> sortedList = new ArrayList<FileDto>();

    List<FileDto> folderList = new ArrayList<FileDto>();
    List<FileDto> regularList = new ArrayList<FileDto>();
    for(FileDto file : fileList) {
      if(file.getType().isFileType(FileDto.FileType.FOLDER)) {
        folderList.add(file);
      } else {
        regularList.add(file);
      }
    }

    sortedList.addAll(folderList);
    sortedList.addAll(regularList);

    return sortedList;
  }

  private abstract class FileNameColumn extends Column<FileDto, String> {

    private List<FileSelectionHandler> fileSelectionHandlers;

    public FileNameColumn() {
      super(new ClickableTextCell());

      fileSelectionHandlers = new ArrayList<FileSelectionHandler>();

      setFieldUpdater(new FieldUpdater<FileDto, String>() {
        public void update(int rowIndex, FileDto dto, String value) {
          for(FileSelectionHandler handler : fileSelectionHandlers) {
            handler.onFileSelection(dto);
          }
        }
      });
    }

    public void addFileSelectionHandler(final FileSelectionHandler handler) {
      fileSelectionHandlers.add(handler);
    }
  }

  public SingleSelectionModel<FileDto> getTableSelectionModel() {
    return tableSelectionModel;
  }

}
