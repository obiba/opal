package org.obiba.opal.web.gwt.app.client.fs.presenter;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FileExplorerUiHandlers extends UiHandlers {

  void onAddFolder();

  void onUploadFile();

  void onDelete();

  void onDownload();

  void onCopy();

  void onCut();

  void onPaste();

}
