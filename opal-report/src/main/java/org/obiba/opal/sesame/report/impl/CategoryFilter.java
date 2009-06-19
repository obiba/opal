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
import org.obiba.opal.elmo.owl.concepts.CategoricalVariableClass;
import org.obiba.opal.elmo.owl.concepts.DataItemClass;
import org.obiba.opal.sesame.report.IDataItemFilter;
import org.obiba.opal.sesame.report.ReportQueryBuilder;
import org.openrdf.elmo.Entity;
import org.openrdf.elmo.sesame.SesameManager;

/**
 * 
 */
public class CategoryFilter implements IDataItemFilter {

  QName categoricalQName;

  public boolean accept(DataItemClass dataItem) {
    categoricalQName = new QName(Opal.NS, "CategoricalVariable");
    Set<?> superClasses = dataItem.getRdfsSubClassOf();
    for(Object superClass : superClasses) {
      Entity elmoEntity = (Entity) superClass;
      if(elmoEntity != null && elmoEntity.getQName() != null) {
        if(categoricalQName.equals(elmoEntity.getQName())) {
          CategoricalVariableClass cvc = (CategoricalVariableClass) dataItem;
          return cvc.isMultiple() == false;
        }
      }
    }
    // No-say on everything else.
    return true;
  }

  public void contribute(ReportQueryBuilder builder, SesameManager manager) {
  }
}
