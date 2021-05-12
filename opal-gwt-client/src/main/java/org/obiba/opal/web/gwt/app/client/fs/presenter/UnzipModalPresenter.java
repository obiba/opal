package org.obiba.opal.web.gwt.app.client.fs.presenter;

import org.obiba.opal.web.gwt.app.client.fs.event.UnzipRequestEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class UnzipModalPresenter extends ModalPresenterWidget<UnzipModalPresenter.Display> implements UnzipModalUiHandlers {

  public interface Display extends PopupView, HasUiHandlers<UnzipModalUiHandlers> {
    void hideDialog();
    HasText getPassword();
    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);
  }

  private FileDto currentFolder;
  private FileDto selectedArchive;

  private final FileSelectionPresenter fileSelectionPresenter;

  @Inject
  public UnzipModalPresenter(EventBus eventBus, Display view, FileSelectionPresenter fileSelectionPresenter) {
    super(eventBus, view);
    getView().setUiHandlers(this);
    this.fileSelectionPresenter = fileSelectionPresenter;
  }

  public void initialize(FileDto folder, FileDto archive) {
    fileSelectionPresenter.getView().setFile(folder.getPath());
    this.currentFolder = folder;
    this.selectedArchive = archive;
  }

  @Override
  protected void onBind() {
    fileSelectionPresenter.bind();
    if (currentFolder != null) fileSelectionPresenter.getView().setFile(currentFolder.getPath());
    fileSelectionPresenter.setFileSelectionType(FileSelectorPresenter.FileSelectionType.FOLDER);
    getView().setFileSelectorWidgetDisplay(fileSelectionPresenter.getView());
  }

  @Override
  protected void onUnbind() {
    fileSelectionPresenter.unbind();
  }

  @Override
  public void onUnzip() {
    fileSelectionPresenter.getSelectedFile();
    fireEvent(new UnzipRequestEvent.Builder(selectedArchive.getPath(), fileSelectionPresenter.getSelectedFile()).password(getView().getPassword().getText()).build());
    getView().hideDialog();
  }
}
