package org.obiba.opal.web.gwt.app.client.project.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DatasourceUiHandlers extends UiHandlers {

  void onImportData();

  void onExportData();

  void onCopyData();

  void onAddTable();

  void onAddView();

  void onDownloadDictionary();

}
