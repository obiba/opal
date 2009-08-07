/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map.support;

import java.io.InputStreamReader;

import org.obiba.opal.map.SemanticMap;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

/**
 *
 */
public class XStreamSemanticMapFactory implements FactoryBean {

  private Resource resource;

  private XStream xstream;

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setXstream(XStream xstream) {
    this.xstream = xstream;
  }

  public Object getObject() throws Exception {
    return xstream.fromXML(new InputStreamReader(resource.getInputStream(), "ISO-8859-1"));
  }

  public Class getObjectType() {
    return SemanticMap.class;
  }

  public boolean isSingleton() {
    return false;
  }

}
