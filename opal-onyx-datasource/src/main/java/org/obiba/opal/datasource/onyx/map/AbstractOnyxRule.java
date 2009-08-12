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
import org.obiba.opal.map.ResourceFactory;

/**
 *
 */
public class AbstractOnyxRule {

  private IVariablePathNamingStrategy pathNamingStrategy;

  private ResourceFactory resourceFactory;

  public void setPathNamingStrategy(IVariablePathNamingStrategy pathNamingStrategy) {
    this.pathNamingStrategy = pathNamingStrategy;
  }

  public void setResourceFactory(ResourceFactory resourceFactory) {
    this.resourceFactory = resourceFactory;
  }

  public IVariablePathNamingStrategy getPathNamingStrategy() {
    return pathNamingStrategy;
  }

  public ResourceFactory getResourceFactory() {
    return resourceFactory;
  }

  protected String mergeParts(List<String> parts) {
    StringBuilder sb = new StringBuilder();
    for(String part : parts) {
      if(sb.length() > 0) {
        sb.append(pathNamingStrategy.getPathSeparator());
      }
      sb.append(part);
    }
    return sb.toString();
  }
}
