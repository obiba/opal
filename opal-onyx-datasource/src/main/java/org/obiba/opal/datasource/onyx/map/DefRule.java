/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx.map;

import java.util.List;

import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.opal.core.domain.metadata.DataItem;
import org.obiba.opal.elmo.concepts.Opal;
import org.obiba.opal.map.GraphBuilder;
import org.obiba.opal.map.ItemRule;
import org.openrdf.model.impl.URIImpl;

/**
 *
 */
public class DefRule implements ItemRule {

  private IVariablePathNamingStrategy pathNamingStrategy;

  public void setPathNamingStrategy(IVariablePathNamingStrategy pathNamingStrategy) {
    this.pathNamingStrategy = pathNamingStrategy;
  }

  public void execute(GraphBuilder builder, DataItem item) {
    String path = item.getName();

    List<String> parts = pathNamingStrategy.getNormalizedNames(path);

    if(parts.size() > 2) {
      String defName = parts.get(1);
      builder.withRelation(new URIImpl(Opal.NS + "dataEntryForm"), "DataEntryForm", defName);
    }
  }
}
