package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.List;

import org.obiba.opal.web.model.client.magma.VariableDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TableUiHandlers extends UiHandlers {

  void onNextTable();

  void onPreviousTable();

  void onExportData();

  void onCopyData();

  void onDownloadDictionary();

  void onDownloadView();

  void onAddVariablesToView(List<VariableDto> variables);

  void onEdit();

  void onRemove();

  void onIndexClear();

  void onIndexNow();

  void onIndexCancel();

  void onIndexSchedule();

  void onFromTable(String tableFullName);

}
