/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

/**
 *
 */
public class FileSelectorPresenter extends WidgetPresenter<FileSelectorPresenter.Display> {

  //
  // Instance Variables
  //

  FileSystemTreePresenter fileSystemTreePresenter;

  FolderDetailsPresenter folderDetailsPresenter;

  private FileSelectorMode mode = FileSelectorMode.FILE;

  //
  // Constructors
  //

  @Inject
  public FileSelectorPresenter(Display display, EventBus eventBus, FileSystemTreePresenter fileSystemTreePresenter, FolderDetailsPresenter folderDetailsPresenter) {
    super(display, eventBus);

    this.fileSystemTreePresenter = fileSystemTreePresenter;
    this.folderDetailsPresenter = folderDetailsPresenter;
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    getDisplay().getFileSystemTreePanel().add(fileSystemTreePresenter.getDisplay().asWidget());
    getDisplay().getFolderDetailsPanel().add(folderDetailsPresenter.getDisplay().asWidget());

    fileSystemTreePresenter.bind();
    folderDetailsPresenter.bind();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().setMode(mode);
    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
    fileSystemTreePresenter.refreshDisplay();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  public void setMode(FileSelectorMode mode) {
    this.mode = mode;
  }

  //
  // Inner Classes / Interfaces
  //

  public enum FileSelectorMode {
    FILE, EXISTING_FILE, FOLDER, EXISTING_FOLDER
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    void setMode(FileSelectorMode mode);

    HasWidgets getFileSystemTreePanel();

    HasWidgets getFolderDetailsPanel();
  }
}
