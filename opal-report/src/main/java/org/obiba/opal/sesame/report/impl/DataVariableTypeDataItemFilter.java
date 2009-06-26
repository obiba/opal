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

import java.util.Set;

import javax.xml.namespace.QName;

import org.obiba.opal.elmo.concepts.Opal;
import org.obiba.opal.elmo.owl.concepts.DataItemClass;
import org.obiba.opal.sesame.report.IDataItemFilter;
import org.obiba.opal.sesame.report.ReportQueryBuilder;
import org.openrdf.elmo.Entity;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.model.URI;

/**
 * 
 */
public class DataVariableTypeDataItemFilter implements IDataItemFilter {

  private String type;

  private boolean filterOut = true;

  private QName superClassQName;

  public boolean accept(DataItemClass dataItem) {
    Set<?> superClasses = dataItem.getRdfsSubClassOf();
    for(Object superClass : superClasses) {
      Entity elmoEntity = (Entity) superClass;
      if(elmoEntity.getQName() != null && elmoEntity.getQName().equals(getQName())) {
        // QName matches, accept only if filterOut is false;
        return filterOut == false;
      }
    }
    // No-say on everything else.
    return true;
  }

  protected QName getQName() {
    if(superClassQName == null) {
      superClassQName = new QName(Opal.NS, type);
    }
    return superClassQName;
  }

  public void contribute(ReportQueryBuilder builder, SesameManager manager) {
    URI superClassUri = manager.getConnection().getValueFactory().createURI(getQName().getNamespaceURI(), getQName().getLocalPart());
    if(filterOut == false) {
      builder.joinVariablePredicateValue("rdfs:subClassOf", superClassUri);
    } else {
      // Left join and add filter on variable not bound
//      builder.filterVariablePredicateValue("rdfs:subClassOf", superClassUri);
    }
  }
}
