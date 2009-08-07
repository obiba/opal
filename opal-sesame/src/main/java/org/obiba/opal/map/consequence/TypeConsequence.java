/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map.consequence;

import org.obiba.opal.core.domain.metadata.DataItemAttribute;
import org.obiba.opal.map.AttributeRuleConsequence;
import org.obiba.opal.map.GraphBuilder;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 */
public class TypeConsequence implements AttributeRuleConsequence {

  private URI uri;

  public void apply(GraphBuilder builder, DataItemAttribute dataItemAttribute) {
    builder.withRelation(RDF.TYPE, uri);
  }

}
