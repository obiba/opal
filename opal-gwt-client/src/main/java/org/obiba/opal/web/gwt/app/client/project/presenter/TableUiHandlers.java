package org.obiba.opal.web.gwt.app.client.project.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TableUiHandlers extends UiHandlers {

  void onDatasourceSelection();

  void onNextTable();

  void onPreviousTable();

  void onExportData();

  void onCopyData();

  void onDownloadDictionary();

  void onDownloadView();

  void onEdit();

  void onRemove();

}
