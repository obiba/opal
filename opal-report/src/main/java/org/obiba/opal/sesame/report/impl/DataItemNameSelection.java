/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.report.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.sesame.report.IDataItemSelection;
import org.obiba.opal.sesame.report.ReportQueryBuilder;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.model.URI;

/**
 * 
 */
public class DataItemNameSelection implements IDataItemSelection {

  private String baseUri;

  private Set<String> names;

  public Set<DataItem> getSelection(SesameManager manager) {

    Set<DataItem> vars = new LinkedHashSet<DataItem>();
    for(String name : names) {
      DataItem dataItem = manager.find(DataItem.class, getQName(name));
      if(dataItem == null) {
        throw new IllegalArgumentException("No such variable " + getQName(name));
      }
      vars.add(dataItem);
    }

    return Collections.unmodifiableSet(vars);
  }

  private QName getQName(String name) {
    return QName.valueOf(baseUri + name);
  }

  public void contribute(ReportQueryBuilder builder, SesameManager manager) {
    String varDataBindingName = builder.getVariableDataBindingName();
    for(String name : names) {
      String binding = builder.nextBinding();
      QName qname = getQName(name);
      URI uri = manager.getConnection().getValueFactory().createURI(qname.getNamespaceURI(), qname.getLocalPart());

      // ?varData rdf:type ?tmp
      StringBuilder sb = new StringBuilder().append(varDataBindingName).append(" rdf:type ").append(binding);
      builder.leftJoin(sb);
      // where ?tmp == <uri>
      builder.withBinding(binding, uri);
    }
  }
}
