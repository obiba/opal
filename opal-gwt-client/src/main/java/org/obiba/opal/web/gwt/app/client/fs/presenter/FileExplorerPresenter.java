/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.fs.FileDownloadSelectionHandler;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class FileExplorerPresenter extends WidgetPresenter<FileExplorerPresenter.Display> {

  public interface Display extends WidgetDisplay {
    public ScrollPanel getFileSystemTree();

    public ScrollPanel getFolderDetailsPanel();

    public HasClickHandlers getFileUploadButton();
  }

  @Inject
  Provider<FileSystemTreePresenter> fileSystemTreePresenterProvider;

  @Inject
  Provider<FolderDetailsPresenter> folderDetailsPresenterProvider;

  @Inject
  Provider<FileUploadDialogPresenter> fileUploadDialogPresenterProvider;

  @Inject
  Provider<FileDownloadPresenter> fileDownloadPresenterProvider;

  FileSystemTreePresenter fileSystemTreePresenter;

  FolderDetailsPresenter folderDetailsPresenter;

  FileUploadDialogPresenter fileUploadDialogPresenter;

  FileDownloadPresenter fileDownloadPresenter;

  FileDto currentFolder;

  @Inject
  public FileExplorerPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addEventHandlers();
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    getDisplay().getFileSystemTree().remove(fileSystemTreePresenter.getDisplay().asWidget());
    getDisplay().getFolderDetailsPanel().remove(folderDetailsPresenter.getDisplay().asWidget());
  }

  @Override
  public void refreshDisplay() {

  }

  @Override
  public void revealDisplay() {

  }

  protected void initDisplayComponents() {

    fileSystemTreePresenter = fileSystemTreePresenterProvider.get();

    fileDownloadPresenter = fileDownloadPresenterProvider.get();
    folderDetailsPresenter = folderDetailsPresenterProvider.get();
    folderDetailsPresenter.getDisplay().getFileNameColumn().addFileSelectionHandler(new FileDownloadSelectionHandler(fileDownloadPresenter));

    getDisplay().getFileSystemTree().add(fileSystemTreePresenter.getDisplay().asWidget());
    getDisplay().getFolderDetailsPanel().add(folderDetailsPresenter.getDisplay().asWidget());
    fileSystemTreePresenter.bind();
    folderDetailsPresenter.bind();
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getFileUploadButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        fileUploadDialogPresenter = fileUploadDialogPresenterProvider.get();
        fileUploadDialogPresenter.setCurrentFolder(currentFolder);
        fileUploadDialogPresenter.bind();
        fileUploadDialogPresenter.revealDisplay();
      }
    }));

    super.registerHandler(eventBus.addHandler(FileSystemTreeFolderSelectionChangeEvent.getType(), new FileSystemTreeFolderSelectionChangeEvent.Handler() {

      @Override
      public void onFolderSelectionChange(FileSystemTreeFolderSelectionChangeEvent event) {
        currentFolder = event.getFolder();
      }

    }));

    super.registerHandler(eventBus.addHandler(FolderSelectionChangeEvent.getType(), new FolderSelectionChangeEvent.Handler() {

      @Override
      public void onFolderSelectionChange(FolderSelectionChangeEvent event) {
        currentFolder = event.getFolder();
      }

    }));

  }
}
