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

import org.obiba.opal.web.model.client.magma.TableDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DatasourceUiHandlers extends UiHandlers {

  void onImportData();

  void onExportData();

  void onCopyData();

  void onAddTable();

  void onAddUpdateTables();

  void onAddView();

  void onRefresh();

  void onDownloadDictionary();

  void onDeleteTables(List<TableDto> tables);

  void onTablesFilterUpdate(String filter);

  void onSearchVariables();

  void onRestoreViews();

  void onBackupViews();
}
