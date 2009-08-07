/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map.condition;

import org.obiba.opal.core.domain.metadata.DataItemAttribute;
import org.obiba.opal.map.AttributeRuleCondition;

/**
 *
 */
public class AttributeNameCondition implements AttributeRuleCondition {

  private String name;

  private boolean notNull;

  public boolean isApplicable(DataItemAttribute dataItemAttribute) {
    return name.equals(dataItemAttribute.getName()) && (notNull ? dataItemAttribute.getValue() != null : true);
  }

}
