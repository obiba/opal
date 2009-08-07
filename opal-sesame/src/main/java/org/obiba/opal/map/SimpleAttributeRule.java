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

import java.util.Map;

import org.obiba.opal.core.domain.metadata.DataItemAttribute;
import org.openrdf.model.URI;

/**
 *
 */
public class SimpleAttributeRule implements AttributeRule {

  private Map<String, URI> map;

  public void execute(GraphBuilder builder, DataItemAttribute attribute) {
    if(attribute.getValue() != null) {
      if(map.containsKey(attribute.getName())) {
        builder.withLiteral(map.get(attribute.getName()), attribute.getValue(), attribute.getLocale());
      }
    }
  }

}
