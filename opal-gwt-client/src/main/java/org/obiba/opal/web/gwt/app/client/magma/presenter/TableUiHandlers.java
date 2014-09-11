package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.List;

import org.obiba.opal.web.model.client.magma.VariableDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TableUiHandlers extends UiHandlers {

  void onExportData();

  void onCopyData();

  void onDownloadDictionary();

  void onDownloadView();

  void onAddVariable();

  void onAddVariablesFromFile();

  void onAddVariablesToView(List<VariableDto> variables);

  void onEdit();

  void onEditWhere();

  void onRemove();

  void onIndexClear();

  void onIndexNow();

  void onIndexCancel();

  void onIndexSchedule();

  void onDeleteVariables(List<VariableDto> variables);

  void onCrossVariables();

  void onApplyAttribute(List<VariableDto> selectedItems);

  void onShowDictionary();

  void onShowValues();

  void onDeleteAttribute(List<VariableDto> selectedItems);

  void onVariablesFilterUpdate(String filter);
}
