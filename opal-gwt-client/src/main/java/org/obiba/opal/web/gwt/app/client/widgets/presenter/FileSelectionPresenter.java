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

import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

/**
 *
 */
public class FileSelectionPresenter extends WidgetPresenter<FileSelectionPresenter.Display> {

  //
  // Instance Variables
  //

  private FileSelectionType fileSelectionType = FileSelectionType.FILE;

  //
  // Constructors
  //

  @Inject
  public FileSelectionPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  //
  // Methods
  // 

  public String getSelectedFile() {
    return getDisplay().getFileField().getText();
  }

  public void setSelectedFile(String selectedFile) {
    getDisplay().getFileField().setText(selectedFile);
  }

  public FileSelectionType getFileSelectionType() {
    return fileSelectionType;
  }

  public void setFileSelectionType(FileSelectionType fileSelectionType) {
    this.fileSelectionType = fileSelectionType;
  }

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(FileSelectionEvent.getType(), new FileSelectionEvent.Handler() {

      @Override
      public void onFileSelection(FileSelectionEvent event) {
        getDisplay().getFileField().setText(event.getSelectedFile());
      }

    }));

    super.registerHandler(getDisplay().getBrowseWidget().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new FileSelectionRequiredEvent(FileSelectionPresenter.this, fileSelectionType));
      }
    }));

  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    HasClickHandlers getBrowseWidget();

    HasText getFileField();

    void setEnabled(boolean enabled);

    void setWidth(String width);

  }

}
