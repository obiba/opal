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

import org.obiba.opal.core.domain.metadata.DataItemAttribute;
import org.obiba.opal.elmo.concepts.Opal;
import org.obiba.opal.map.AttributeRuleConsequence;
import org.obiba.opal.map.GraphBuilder;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 */
public class CategoryConsequence extends AbstractOnyxRule implements AttributeRuleConsequence {

  public void apply(GraphBuilder builder, DataItemAttribute dataItemAttribute) {
    String path = dataItemAttribute.getDataItem().getName();

    List<String> parts = getPathNamingStrategy().getNormalizedNames(path);
    parts.remove(parts.size() - 1);
    String parentPath = mergeParts(parts);

    URI resource = getResourceFactory().findResource(Opal.NS + "path", parentPath);
    if(resource != null) {
      builder.withRelation(new URIImpl(Opal.NS + "isCategoryFor"), resource);
      builder.withInverseRelation(new URIImpl(Opal.NS + "hasCategory"), resource);
    }

  }
}
