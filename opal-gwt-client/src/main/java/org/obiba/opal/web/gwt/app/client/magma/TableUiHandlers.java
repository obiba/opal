/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma;

import java.util.List;

import org.obiba.opal.web.model.client.magma.VariableDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TableUiHandlers extends UiHandlers {

  void onReconnectResourceView();

  void onExportData();

  void onCopyData();

  void onCopyView();

  void onDownloadDictionary();

  void onDownloadView();

  void onSearchVariables();

  void onAddVariable();

  void onAddVariablesFromFile();

  void onAddVariablesToView(List<VariableDto> variables);

  void onAddVariablesToCart(List<VariableDto> variables);

  void onAnalyseVariables(List<VariableDto> variables);

  void onEdit();

  void onEditWhere();

  void onRemove();

  void onIndexClear();

  void onIndexNow();

  void onIndexCancel();

  void onIndexSchedule();

  void onDeleteVariables(List<VariableDto> variables);

  void onCrossVariables();

  void onApplyTaxonomyAttribute(List<VariableDto> selectedItems);

  void onApplyCustomAttribute(List<VariableDto> selectedItems);

  void onShowDictionary();

  void onShowValues();

  void onShowAnalyses();

  void onDeleteTaxonomyAttribute(List<VariableDto> selectedItems);

  void onDeleteCustomAttribute(List<VariableDto> selectedItems);

  void onVariablesFilterUpdate(String filter);

  void onDownloadAnalyses();

  void onSQLQuery();
}
