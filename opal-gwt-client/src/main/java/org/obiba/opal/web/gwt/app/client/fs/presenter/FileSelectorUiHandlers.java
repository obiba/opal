package org.obiba.opal.web.gwt.app.client.fs.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FileSelectorUiHandlers extends UiHandlers {

  void uploadFile();
  void selectFolder();
  void cancel();
  void createFolder();

}
