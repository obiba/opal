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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class FileExplorerPresenter extends WidgetPresenter<FileExplorerPresenter.Display> {

  public interface Display extends WidgetDisplay {
    ScrollPanel getFileSystemTree();

    ScrollPanel getFolderDetailsPanel();

    public Button getFileUploadButton();
  }

  @Inject
  Provider<FileSystemTreePresenter> fileSystemTreePresenterProvider;

  @Inject
  Provider<FolderDetailsPresenter> folderDetailsPresenterProvider;

  @Inject
  Provider<FileUploadDialogPresenter> fileUploadDialogPresenterProvider;

  FileSystemTreePresenter fileSystemTreePresenter;

  FolderDetailsPresenter folderDetailsPresenter;

  FileUploadDialogPresenter fileUploadDialogPresenter;

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
    folderDetailsPresenter = folderDetailsPresenterProvider.get();
    fileUploadDialogPresenter = fileUploadDialogPresenterProvider.get();
    getDisplay().getFileSystemTree().add(fileSystemTreePresenter.getDisplay().asWidget());
    getDisplay().getFolderDetailsPanel().add(folderDetailsPresenter.getDisplay().asWidget());
    fileSystemTreePresenter.bind();
    folderDetailsPresenter.bind();
    fileUploadDialogPresenter.bind();
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getFileUploadButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        fileUploadDialogPresenter.revealDisplay();
      }
    }));

  }
}
