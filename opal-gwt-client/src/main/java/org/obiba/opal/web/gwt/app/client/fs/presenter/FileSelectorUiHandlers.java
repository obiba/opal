package org.obiba.opal.web.gwt.app.client.fs.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface FileSelectorUiHandlers extends ModalUiHandlers {

  void uploadFile();
  void selectFolder();
  void cancel();
  void createFolder();

}
