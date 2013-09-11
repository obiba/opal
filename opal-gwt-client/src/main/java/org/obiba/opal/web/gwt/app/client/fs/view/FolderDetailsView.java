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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.FileSelectionHandler;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.ValueRenderingHelper;
import org.obiba.opal.web.gwt.app.client.ui.celltable.DateTimeColumn;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.FileDto.FileType;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.HandlerRegistration;
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

  @UiTemplate("FolderDetailsView.ui.xml")
  interface FolderDetailsUiBinder extends UiBinder<Widget, FolderDetailsView> {}

  private static final FolderDetailsUiBinder uiBinder = GWT.create(FolderDetailsUiBinder.class);

  private final Widget widget;

  @UiField
  CellTable<FileDto> table;

  private FileNameColumn fileNameColumn;

  private final Translations translations = GWT.create(Translations.class);

  private boolean displaysFiles = true;

  public FolderDetailsView() {
    widget = uiBinder.createAndBindUi(this);
    initTable();
  }

  @Override
  public void setDisplaysFiles(boolean display) {
    displaysFiles = display;
  }

  @Override
  public void clearSelection() {
    getTableSelectionModel().setSelected(getTableSelectionModel().getSelectedObject(), false);
  }

  @Override
  @SuppressWarnings("unchecked")
  public SingleSelectionModel<FileDto> getTableSelectionModel() {
    return (SingleSelectionModel<FileDto>) table.getSelectionModel();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void renderRows(FileDto folder) {
    JsArray<FileDto> children = folder.getChildrenCount() == 0
        ? (JsArray<FileDto>) JsArray.createArray()
        : filterChildren(folder.getChildrenArray());

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

  @Override
  public HandlerRegistration addFileSelectionHandler(FileSelectionHandler fileSelectionHandler) {
    return fileNameColumn.addFileSelectionHandler(fileSelectionHandler);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  private void initTable() {
    addTableColumns();
    table.setSelectionModel(new SingleSelectionModel<FileDto>());
  }

  private void addTableColumns() {
    table.addColumn(fileNameColumn = new FileNameColumn(), translations.nameLabel());

    table.addColumn(new TextColumn<FileDto>() {

      @Override
      public String getValue(FileDto object) {
        return object.getType().isFileType(FileDto.FileType.FILE) ? ValueRenderingHelper
            .getSizeWithUnit(object.getSize()) : "";
      }

    }, translations.sizeLabel());

    table.addColumn(new DateTimeColumn<FileDto>() {

      @Nullable
      @Override
      public Date getValue(FileDto object) {
        return "..".equals(object.getName()) ? null : new Date((long) object.getLastModifiedTime());
      }
    }, translations.lastModifiedLabel());

  }

  /**
   * Returns a sorted copy of the specified file list.
   * <p/>
   * This method simply puts folders ahead of regular files; no additional sorting is performed.
   *
   * @param fileList the list to be sorted
   * @return the sorted list
   */
  private List<FileDto> sortFileList(Iterable<FileDto> fileList) {
    List<FileDto> sortedList = new ArrayList<FileDto>();

    Collection<FileDto> folderList = new ArrayList<FileDto>();
    Collection<FileDto> regularList = new ArrayList<FileDto>();
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

    private final List<FileSelectionHandler> fileSelectionHandlers;

    private FileNameColumn() {
      super(new ClickableTextCell(new AbstractSafeHtmlRenderer<String>() {
        @Override
        public SafeHtml render(String object) {
          FileDto file = FileDto.parse(object);
          String icon = "icon-file";
          if(file.getType().getName().equals(FileType.FOLDER.getName())) {
            icon = "icon-folder-close-alt";
          }
          if (!file.getReadable()) {
            icon = "icon-lock";
          }
          return new SafeHtmlBuilder().appendHtmlConstant("<i class=\"" + icon + "\"></i> <a>")
              .appendEscaped(file.getName()).appendHtmlConstant("</a>").toSafeHtml();
        }
      }));

      fileSelectionHandlers = new ArrayList<FileSelectionHandler>();

      setFieldUpdater(new FieldUpdater<FileDto, String>() {
        @Override
        public void update(int rowIndex, FileDto dto, String value) {
          if(dto.getReadable()) {
            for(FileSelectionHandler handler : fileSelectionHandlers) {
              handler.onFileSelection(dto);
            }
          }
        }
      });
    }

    public HandlerRegistration addFileSelectionHandler(final FileSelectionHandler handler) {
      fileSelectionHandlers.add(handler);

      return new HandlerRegistration() {

        @Override
        public void removeHandler() {
          fileSelectionHandlers.remove(handler);
        }
      };
    }

    @Override
    public String getValue(FileDto dto) {
      // hack because we need some info to make the markup
      return FileDto.stringify(dto);
    }

  }

}
