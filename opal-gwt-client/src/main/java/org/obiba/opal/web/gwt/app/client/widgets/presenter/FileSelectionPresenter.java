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

import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionUpdateEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class FileSelectionPresenter extends PresenterWidget<FileSelectionPresenter.Display> {

  //
  // Instance Variables
  //

  private FileSelectionType fileSelectionType = FileSelectionType.FILE;

  private FileSelectionType fileTypeSelected;

  //
  // Constructors
  //

  @Inject
  public FileSelectionPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    addEventHandlers();
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

  public FileSelectionType getFileSelectionType() {
    return fileSelectionType;
  }

  public void setFileSelectionType(FileSelectionType fileSelectionType) {
    this.fileSelectionType = fileSelectionType;
  }

  public FileSelectionType getFileTypeSelected() {
    return fileTypeSelected;
  }

  private void addEventHandlers() {
    registerHandler(getEventBus().addHandler(FileSelectionEvent.getType(), new FileSelectionEvent.Handler() {

      @Override
      public void onFileSelection(FileSelectionEvent event) {
        if(FileSelectionPresenter.this.equals(event.getSource())) {
          fileTypeSelected = event.getSelectedFile().getSelectionType();
          getView().setFile(event.getSelectedFile().getSelectionPath());
          getEventBus().fireEvent(new FileSelectionUpdateEvent(FileSelectionPresenter.this));
        }
      }

    }));

    registerHandler(getView().addBrowseClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new FileSelectionRequiredEvent(FileSelectionPresenter.this, fileSelectionType));
      }
    }));

  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    HandlerRegistration addBrowseClickHandler(ClickHandler handler);

    String getFile();

    HasText getFileText();

    void setFile(String text);

    void clearFile();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    void setFieldWidth(String width);

  }

}
