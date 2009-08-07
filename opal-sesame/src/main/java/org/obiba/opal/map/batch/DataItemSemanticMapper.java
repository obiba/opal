/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map.batch;

import org.obiba.opal.core.domain.metadata.DataItem;
import org.obiba.opal.map.SemanticMap;
import org.openrdf.model.Graph;
import org.springframework.batch.item.ItemProcessor;

/**
 *
 */
public class DataItemSemanticMapper implements ItemProcessor<DataItem, Graph> {

  private SemanticMap semanticMap;

  public void setSemanticMap(SemanticMap semanticMap) {
    this.semanticMap = semanticMap;
  }

  public Graph process(DataItem item) throws Exception {
    return semanticMap.process(item);
  }

}
