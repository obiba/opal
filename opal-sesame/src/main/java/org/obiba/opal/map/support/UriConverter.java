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

import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class UriConverter implements Converter {

  private Set<Namespace> namespaces;

  public void setNamespaces(Set<Namespace> namespaces) {
    this.namespaces = namespaces;
  }

  public boolean canConvert(Class type) {
    return URI.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    throw new UnsupportedOperationException();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String value = reader.getValue();

    if(value != null) {
      String parts[] = value.split(":");
      String prefix = parts[0];
      for(Namespace ns : namespaces) {
        if(ns.getPrefix().equals(prefix)) {
          return new URIImpl(ns.getName() + parts[1]);
        }
      }
      return new URIImpl(value);
    }
    return null;
  }
}