package org.obiba.opal.web.gwt.app.client.magma.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DatasourceUiHandlers extends UiHandlers {

  void onImportData();

  void onExportData();

  void onCopyData();

  void onAddTable();

  void onAddUpdateTables();

  void onAddView();

  void onDownloadDictionary();

}
