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

import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.inject.Inject;

public class FileUploadDialogPresenter extends WidgetPresenter<FileUploadDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HasClickHandlers getUploadButton();

    HasClickHandlers getCancelButton();

    String getFilename();

    HandlerRegistration addSubmitHandler(FormPanel.SubmitHandler handler);

    HandlerRegistration addSubmitCompleteHandler(FormPanel.SubmitCompleteHandler handler);

    void submit(String url);

    HasText getRemoteFolderName();

    HasText getErrorMsg();
  }

  private Translations translations = GWT.create(Translations.class);

  private FileDto currentFolder;

  @Inject
  public FileUploadDialogPresenter(Display display, EventBus eventBus) {
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
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    String folderName = currentFolder.getName();
    getDisplay().getRemoteFolderName().setText(folderName.equals("root") ? translations.fileSystemLabel() : folderName);
    getDisplay().showDialog();
  }

  protected void initDisplayComponents() {
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getUploadButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        uploadFile();
      }
    }));

    super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    super.registerHandler(getDisplay().addSubmitHandler(new FormPanel.SubmitHandler() {
      @Override
      public void onSubmit(SubmitEvent event) {
        if(getDisplay().getFilename().equals("")) {
          getDisplay().getErrorMsg().setText(translations.fileMustBeSelected());
          event.cancel();
        }
      }
    }));

    super.registerHandler(getDisplay().addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
      public void onSubmitComplete(SubmitCompleteEvent event) {
        getDisplay().hideDialog();
        eventBus.fireEvent(new FileUploadedEvent());
      }
    }));

  }

  private void uploadFile() {
    String url = "/ws/files" + currentFolder.getPath() + '/' + getDisplay().getFilename();
    getDisplay().submit(url);
  }

  /**
   * @param currentFolder
   */
  public void setCurrentFolder(FileDto currentFolder) {
    this.currentFolder = currentFolder;
  }
}
