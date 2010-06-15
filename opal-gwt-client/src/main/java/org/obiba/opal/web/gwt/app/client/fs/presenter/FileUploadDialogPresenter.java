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

import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.inject.Inject;

public class FileUploadDialogPresenter extends WidgetPresenter<FileUploadDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {
    DialogBox getDialog();

    Button getUploadButton();

    Button getCancelButton();

    FileUpload getFileToUpload();

    FormPanel getUploadForm();

    Hidden getRemoteFolder();

    Label getRemoteFolderName();

    VerticalPanel getInputFieldPanel();
  }

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
    getDisplay().getDialog().show();
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
        getDisplay().getDialog().hide();
      }
    }));

    super.registerHandler(eventBus.addHandler(FolderSelectionChangeEvent.getType(), new FolderSelectionChangeEvent.Handler() {

      @Override
      public void onFolderSelectionChange(FolderSelectionChangeEvent event) {
        getDisplay().getRemoteFolder().setValue(event.getFolder().getPath());
      }

    }));

    super.registerHandler(eventBus.addHandler(FileSystemTreeFolderSelectionChangeEvent.getType(), new FileSystemTreeFolderSelectionChangeEvent.Handler() {

      @Override
      public void onFolderSelectionChange(FolderSelectionChangeEvent event) {
        getDisplay().getRemoteFolder().setValue(event.getFolder().getPath());
        getDisplay().getRemoteFolderName().setText(event.getFolder().getName());
      }

    }));

    super.registerHandler(getDisplay().getUploadForm().addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
      public void onSubmitComplete(SubmitCompleteEvent event) {
        getDisplay().getDialog().hide();
        eventBus.fireEvent(new FileUploadedEvent());
      }
    }));

  }

  private void uploadFile() {
    FormPanel form = getDisplay().getUploadForm();
    String url = "/ws/files" + getDisplay().getRemoteFolder().getValue() + "/" + getDisplay().getFileToUpload().getFilename();
    form.setAction(url);
    form.submit();
  }
}
