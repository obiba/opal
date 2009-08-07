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

import org.obiba.opal.core.domain.metadata.DataItem;
import org.openrdf.model.URI;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 *
 */
public class DataItemPropertyRule implements ItemRule {

  private String property;

  private URI uri;

  public void execute(GraphBuilder builder, DataItem item) {
    String value = doReadValue(item);
    if(value != null) {
      builder.withLiteral(uri, value);
    }
  }

  protected String doReadValue(DataItem item) {
    BeanWrapper wrapper = doGetBeanWrapper(item);
    Object value = doGetBeanWrapper(item).getPropertyValue(property);
    return (String) wrapper.convertIfNecessary(value, String.class);
  }

  protected BeanWrapper doGetBeanWrapper(DataItem item) {
    return new BeanWrapperImpl(item);
  }

}
