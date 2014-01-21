package org.obiba.opal.web.gwt.app.client.fs.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface FileUploadModalUiHandlers extends ModalUiHandlers {

  void uploadFile(String filename);

  void submit();
}

