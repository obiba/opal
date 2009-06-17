/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.report;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.obiba.opal.core.mart.sas.ISasMartBuilder;
import org.obiba.opal.elmo.owl.concepts.DataItemClass;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class Report {

  private static final Logger log = LoggerFactory.getLogger(Report.class);

  private List<IDataItemSelection> selections;

  private List<IDataItemFilter> filters;

  public void build(SesameManager manager, ISasMartBuilder martBuilder) {

    List<DataItemClass> reportedItems = new LinkedList<DataItemClass>();

    Map<QName, Integer> varIndex = new HashMap<QName, Integer>();

    ReportQueryBuilder builder = new ReportQueryBuilder();

    for(IDataItemSelection s : selections) {
      for(DataItemClass dataItem : s.getSelection(manager)) {
        if(filters != null) {
          for(IDataItemFilter filter : filters) {
            if(filter.accept(dataItem)) {
              reportedItems.add(dataItem);
              log.debug("reportedItem: {}", dataItem.getQName());
            } else {
              log.debug("filtered reportedItem: {}", dataItem.getQName());
            }
          }
        } else {
          reportedItems.add(dataItem);
          log.debug("reportedItem: {}", dataItem.getQName());
        }
      }
    }

    String names[] = new String[reportedItems.size()];
    int i = 0;
    for(DataItemClass dataItem : reportedItems) {
      names[i] = dataItem.getQName().getLocalPart();
      varIndex.put(dataItem.getQName(), i++);
      log.debug("Var {}={}", i, dataItem.getQName());
    }
    martBuilder.setVariableNames(names);

    for(IDataItemSelection selection : selections) {
      selection.contribute(builder, manager);
    }
    if(filters != null) {
      for(IDataItemFilter filter : filters) {
        filter.contribute(builder, manager);
      }
    }

    try {
      TupleQuery query = builder.build(manager);
      Object values[] = new Object[varIndex.size()];

      String currentsid = null;
      TupleQueryResult trq = query.evaluate();
      while(trq.hasNext()) {
        BindingSet set = trq.next();
        Binding entityBinding = set.getBinding("sid");
        Binding varBinding = set.getBinding("var");
        Binding valueBinding = set.getBinding("value");

        // log.debug("{} {} {}", new Object[] { entityBinding, varBinding, valueBinding });

        String sid = entityBinding.getValue().stringValue();
        if(currentsid == null) {
          currentsid = sid;
        }

        if(sid.equals(currentsid) == false) {
          martBuilder.withData(sid, values);
          values = new Object[varIndex.size()];
          currentsid = sid;
        }

        URI varURI = (URI) varBinding.getValue();
        QName varQName = new QName(varURI.getNamespace(), varURI.getLocalName());
        Integer index = varIndex.get(varQName);
        // Set value if there is one to set and if variable is included in
        // report.
        if(valueBinding != null && index != null) {
          values[index] = manager.getLiteralManager().getObject((Literal) valueBinding.getValue());
        }
      }
    } catch(Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
