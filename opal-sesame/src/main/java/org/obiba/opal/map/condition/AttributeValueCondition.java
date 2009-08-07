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

/**
 *
 */
public class AttributeValueCondition extends AttributeNameCondition {

  private String value;

  @Override
  public boolean isApplicable(DataItemAttribute dataItemAttribute) {
    return super.isApplicable(dataItemAttribute) && value.equals(dataItemAttribute.getValue());
  }

}
