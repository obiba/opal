package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.List;

import org.obiba.opal.web.model.client.magma.TableDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DatasourceUiHandlers extends UiHandlers {

  void onImportData();

  void onExportData();

  void onCopyData();

  void onAddTable();

  void onAddUpdateTables();

  void onAddView();

  void onDownloadDictionary();

  void onDeleteTables(List<TableDto> tables);

  void onTablesFilterUpdate(String filter);
}
