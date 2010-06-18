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

import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.HasUrl;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

public class FolderDetailsView extends Composite implements Display {

  private static final long KB = 1024l;

  private static final long MB = KB * KB;

  private static final long GB = MB * KB;

  @UiTemplate("FolderDetailsView.ui.xml")
  interface FolderDetailsUiBinder extends UiBinder<Widget, FolderDetailsView> {
  }

  private static FolderDetailsUiBinder uiBinder = GWT.create(FolderDetailsUiBinder.class);

  @UiField
  CellTable<FileDto> table;

  @UiField
  Frame downloader;

  private FileNameColumn fileNameColumn;

  // Adaptor for Frame
  private final HasUrl hasUrlImpl;

  private Translations translations = GWT.create(Translations.class);

  public FolderDetailsView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
    // This iFrame is used to download files in the background. Its URL is meant to be changed to that of the file's
    // location, the browser will then take care of downloading it.
    downloader.setVisible(false);

    // Adapt Frame to HasUrl
    hasUrlImpl = new HasUrl() {

      @Override
      public String getUrl() {
        return downloader.getUrl();
      }

      @Override
      public void setUrl(String url) {
        downloader.setUrl(url);
      }

    };
  }

  @Override
  public void renderRows(final FileDto folder) {
    JsArray<FileDto> children = folder.getChildrenArray();
    int fileCount = children.length();
    table.setPageSize(fileCount);
    table.setDataSize(fileCount, true);
    table.setData(0, fileCount, sortFileList(JsArrays.toList(children, 0, fileCount)));
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
  public HasFieldUpdater<FileDto, String> getFileNameColumn() {
    return fileNameColumn;
  }

  @Override
  public HasUrl getFileDownloader() {
    return hasUrlImpl;
  }

  private void initTable() {
    addTableColumns();
  }

  private void addTableColumns() {
    table.addColumn(fileNameColumn = new FileNameColumn() {

      @Override
      public String getValue(FileDto object) {
        if(object.getSymbolicLink()) {
          return "..";
        }
        return object.getName();
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

    table.addColumn(new LastModifiedTimeColumn() {

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

  private abstract class FileNameColumn extends Column<FileDto, String> implements HasFieldUpdater<FileDto, String> {
    public FileNameColumn() {
      super(new ClickableTextCell());
    }
  }

  private static abstract class LastModifiedTimeColumn extends Column<FileDto, Date> {
    public LastModifiedTimeColumn() {
      super(new DateCell(DateTimeFormat.getShortDateTimeFormat()));
    }
  }

}
