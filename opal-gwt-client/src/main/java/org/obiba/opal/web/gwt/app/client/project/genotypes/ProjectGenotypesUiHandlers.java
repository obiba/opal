/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.genotypes;

import com.gwtplatform.mvp.client.UiHandlers;
import org.obiba.opal.web.model.client.opal.VCFSummaryDto;

import java.util.Collection;

public interface ProjectGenotypesUiHandlers extends UiHandlers {

  void onExportVcfFiles();

  void onImportVcfFiles();

  void onEditMappingTable();

  void onAddMappingTable();

  void onDeleteMappingTable();

  void onRemoveVcfFile(Collection<VCFSummaryDto> vcfSummaryDto);

  void onDownloadStatistics(VCFSummaryDto vcfSummaryDto);

  void onFilterUpdate(String filter);

  void onMappingTableNavigateTo();

  void onMappingTableNavigateToVariable(String variable);

  void onRefresh();
}
