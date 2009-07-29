/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.support;

import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.datasource.DatasourceService;
import org.springframework.batch.item.ItemProcessor;

/**
 * Skips {@link Dataset} entries already present. A dataset already exists if it is for the same entity from the same
 * datasource with the same extraction date.
 */
public class ExistingDatasetProcessor implements ItemProcessor<Dataset, Dataset> {

  private DatasourceService datasourceService;

  public void setDatasourceService(DatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  public Dataset process(Dataset item) throws Exception {
    // If dataset already exists, then skip it (return null)
    return datasourceService.hasDataset(item.getEntity(), item.getCatalogue(), item.getExtractionDate()) ? null : item;
  }
}
