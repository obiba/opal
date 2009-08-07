/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map;

import java.util.List;

import org.obiba.opal.core.domain.metadata.DataItem;
import org.obiba.opal.core.domain.metadata.DataItemAttribute;
import org.obiba.opal.elmo.concepts.Opal;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 */
public class SemanticMap {

  private GraphFactory graphFactory;

  private String baseUri;

  private List<ItemRule> itemRules;

  private List<AttributeRule> attributeRules;

  public void setGraphFactory(GraphFactory graphFactory) {
    this.graphFactory = graphFactory;
  }

  public Graph process(DataItem dataItem) {
    GraphBuilder builder = new GraphBuilder(graphFactory.newGraph(), baseUri);

    builder.forResource("DataItem", dataItem.getId().toString());

    URI type = new ValueFactoryImpl().createURI(Opal.NS + "DataItem");
    builder.withRelation(RDF.TYPE, type);

    builder.clearChanged();

    for(ItemRule rule : itemRules) {
      rule.execute(builder, dataItem);
    }

    for(DataItemAttribute attribute : dataItem.getAttributes()) {
      for(AttributeRule rule : attributeRules) {
        rule.execute(builder, attribute);
      }
    }
    return builder.build();
  }
}
