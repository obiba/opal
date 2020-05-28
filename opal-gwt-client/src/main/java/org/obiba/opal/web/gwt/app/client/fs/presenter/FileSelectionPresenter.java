/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionUpdatedEvent;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class FileSelectionPresenter extends PresenterWidget<FileSelectionPresenter.Display>
    implements FileSelectionUiHandlers {

  //
  // Instance Variables
  //

  private FileSelectorPresenter.FileSelectionType fileSelectionType = FileSelectorPresenter.FileSelectionType.FILE;

  private FileSelectorPresenter.FileSelectionType fileTypeSelected;

  //
  // Constructors
  //

  @Inject
  public FileSelectionPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  public void onBrowse() {
    fireEvent(new FileSelectionRequestEvent(this, fileSelectionType));
  }

  //
  // Methods
  //

  public String getSelectedFile() {
    return getView().getFile();
  }

  public void setSelectedFile(String selectedFile) {
    getView().setFile(selectedFile);
  }

  public FileSelectorPresenter.FileSelectionType getFileSelectionType() {
    return fileSelectionType;
  }

  public void setFileSelectionType(FileSelectorPresenter.FileSelectionType fileSelectionType) {
    this.fileSelectionType = fileSelectionType;
  }

  public FileSelectorPresenter.FileSelectionType getFileTypeSelected() {
    return fileTypeSelected;
  }

  private void addEventHandlers() {
    addRegisteredHandler(FileSelectionEvent.getType(), new FileSelectionEvent.Handler() {

      @Override
          public void onFileSelection(FileSelectionEvent event) {
            if(FileSelectionPresenter.this.equals(event.getSource())) {
              fileTypeSelected = event.getSelectedFile().getSelectionType();
              getView().setFile(event.getSelectedFile().getSelectionPath());
              fireEvent(new FileSelectionUpdatedEvent(FileSelectionPresenter.this));
            }
          }

        });
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View, HasUiHandlers<FileSelectionUiHandlers> {

    String getFile();

    HasText getFileText();

    void setFile(String text);

    void clearFile();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    void setFieldWidth(String width);

  }

}
