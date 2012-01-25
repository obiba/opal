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
import org.obiba.opal.web.gwt.app.client.widgets.celltable.DateTimeColumn;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.FileDto.FileType;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.gwtplatform.mvp.client.ViewImpl;

public class FolderDetailsView extends ViewImpl implements Display {

  private static final long KB = 1024l;

  private static final long MB = KB * KB;

  private static final long GB = MB * KB;

  @UiTemplate("FolderDetailsView.ui.xml")
  interface FolderDetailsUiBinder extends UiBinder<Widget, FolderDetailsView> {
  }

  private static FolderDetailsUiBinder uiBinder = GWT.create(FolderDetailsUiBinder.class);

  private final Widget widget;

  @UiField
  CellTable<FileDto> table;

  private FileNameColumn fileNameColumn;

  private Translations translations = GWT.create(Translations.class);

  private boolean displaysFiles = true;

  public FolderDetailsView() {
    widget = uiBinder.createAndBindUi(this);
    initTable();
  }

  public void setDisplaysFiles(boolean display) {
    displaysFiles = display;
  }

  public void clearSelection() {
    getTableSelectionModel().setSelected(getTableSelectionModel().getSelectedObject(), false);
  }

  @SuppressWarnings("unchecked")
  public SingleSelectionModel<FileDto> getTableSelectionModel() {
    return (SingleSelectionModel<FileDto>) table.getSelectionModel();
  }

  @SuppressWarnings("unchecked")
  public void renderRows(final FileDto folder) {
    JsArray<FileDto> children = (folder.getChildrenCount() != 0) ? filterChildren(folder.getChildrenArray()) : (JsArray<FileDto>) JsArray.createArray();

    if(!folder.getName().equals("root")) {
      FileDto parent = FileDtos.getParent(folder);
      parent.setName("..");
      children.unshift(parent);
    }

    int fileCount = children.length();
    table.setPageSize(fileCount);
    table.setRowCount(fileCount, true);
    table.setRowData(0, sortFileList(JsArrays.toList(children, 0, fileCount)));
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

  public HandlerRegistration addFileSelectionHandler(FileSelectionHandler fileSelectionHandler) {
    return fileNameColumn.addFileSelectionHandler(fileSelectionHandler);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  private void initTable() {
    addTableColumns();
    table.addStyleName("folder-details");
    table.setSelectionModel(new SingleSelectionModel<FileDto>());
  }

  private void addTableColumns() {
    table.addColumn(fileNameColumn = new FileNameColumn(), translations.nameLabel());

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
        return !object.getName().equals("..") ? new Date((long) object.getLastModifiedTime()) : null;
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

  private class FileNameColumn extends Column<FileDto, String> {

    private List<FileSelectionHandler> fileSelectionHandlers;

    public FileNameColumn() {
      super(new ClickableTextCell(new AbstractSafeHtmlRenderer<String>() {
        @Override
        public SafeHtml render(String object) {
          return new SafeHtmlBuilder().appendHtmlConstant("<a>").appendEscaped(object).appendHtmlConstant("</a>").toSafeHtml();
        }
      }));

      fileSelectionHandlers = new ArrayList<FileSelectionHandler>();

      setFieldUpdater(new FieldUpdater<FileDto, String>() {
        public void update(int rowIndex, FileDto dto, String value) {
          for(FileSelectionHandler handler : fileSelectionHandlers) {
            handler.onFileSelection(dto);
          }
        }
      });
    }

    @Override
    public String getCellStyleNames(Context context, FileDto dto) {
      FileType type = dto.getType();
      if(type.isFileType(FileType.FOLDER) && dto.getName().equals("..")) {
        return "folder-up";
      }
      return dto.getType().getName().toLowerCase();
    }

    public HandlerRegistration addFileSelectionHandler(final FileSelectionHandler handler) {
      fileSelectionHandlers.add(handler);

      return new HandlerRegistration() {

        public void removeHandler() {
          fileSelectionHandlers.remove(handler);
        }
      };
    }

    @Override
    public String getValue(FileDto dto) {
      return dto.getName();
    }

  }

}
