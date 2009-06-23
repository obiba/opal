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

import org.obiba.opal.elmo.concepts.Category;
import org.obiba.opal.elmo.concepts.MissingCategory;
import org.obiba.opal.elmo.owl.concepts.DataItemClass;
import org.obiba.opal.sesame.report.IDataItemFilter;
import org.obiba.opal.sesame.report.ReportQueryBuilder;
import org.openrdf.elmo.Entity;
import org.openrdf.elmo.sesame.SesameManager;

/**
 * Filters categories of CategoricalVariables that are have multiple set to false
 */
public class CategoryFilter implements IDataItemFilter {

  public boolean accept(DataItemClass dataItem) {
    Set<?> superClasses = dataItem.getRdfsSubClassOf();
    for(Object superClass : superClasses) {
      Entity elmoEntity = (Entity) superClass;
      if(isCategory(elmoEntity.getQName())) {
        boolean parentMultiple = dataItem.getParent().isMultiple();
        return parentMultiple == true;
      }
    }
    // No-say on everything else.
    return true;
  }

  boolean isCategory(QName qname) {
    if(qname != null) {
      return Category.QNAME.equals(qname) || MissingCategory.QNAME.equals(qname);
    }
    return false;
  }

  public void contribute(ReportQueryBuilder builder, SesameManager manager) {
    // Filter out categories that have a parent CategoricalVariable that is NOT multiple
    builder.filterVariableCriteria("opal:parent [ rdfs:subClassOf opal:CategoricalVariable ; opal:multiple false]");
  }
}
