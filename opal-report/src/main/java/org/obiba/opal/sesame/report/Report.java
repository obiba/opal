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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.obiba.opal.core.mart.sas.ISasMartBuilder;
import org.obiba.opal.elmo.owl.concepts.ContinuousVariableClass;
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

  private boolean withOccurrence;

  private List<IDataItemSelection> selections;

  private List<IDataItemFilter> filters;

  public void build(SesameManager manager, ISasMartBuilder martBuilder) {

    List<DataItemClass> reportedItems = new LinkedList<DataItemClass>();
    Map<QName, Integer> varIndex = new HashMap<QName, Integer>();
    ArrayList<String> names = new ArrayList<String>();

    ReportQueryBuilder builder = new ReportQueryBuilder();

    if(withOccurrence) {
      martBuilder.enableOccurrences();
      builder.withOccurrence();
    }

    for(IDataItemSelection s : selections) {
      for(DataItemClass dataItem : s.getSelection(manager)) {
        boolean accept = true;
        if(filters != null) {
          for(IDataItemFilter filter : filters) {
            if(filter.accept(dataItem) == false) {
              log.debug("filtered: {}", dataItem.getQName());
              accept = false;
              break;
            }
          }
        }
        if(accept == true) {
          int itemIndex = reportedItems.size();
          StringBuilder name = new StringBuilder(dataItem.getQName().getLocalPart());
          if(dataItem instanceof ContinuousVariableClass) {
            ContinuousVariableClass cv = (ContinuousVariableClass) dataItem;
            if(cv.getUnit() != null) {
              name.append(" (").append(cv.getUnit()).append(")");
            }
          }
          names.add(name.toString());
          varIndex.put(dataItem.getQName(), itemIndex);
          reportedItems.add(dataItem);
          log.debug("reported item {}: {}", itemIndex, dataItem.getQName());
        }

      }
    }
    martBuilder.setVariableNames(names.toArray(new String[names.size()]));

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
      int currentOccurrence = 0;
      TupleQueryResult trq = query.evaluate();
      while(trq.hasNext()) {
        BindingSet set = trq.next();
        Binding entityBinding = set.getBinding("sid");
        Binding occurrenceBinding = set.getBinding("occ");
        Binding varBinding = set.getBinding("var");
        Binding valueBinding = set.getBinding("value");

        // log.debug("{} {} {}", new Object[] { entityBinding, varBinding, valueBinding });

        String sid = entityBinding.getValue().stringValue();
        if(currentsid == null) {
          currentsid = sid;
          if(withOccurrence) {
            if(occurrenceBinding == null) {
              throw new IllegalStateException("No occurrence for var " + varBinding.getValue());
            }
            currentOccurrence = (Integer) manager.getLiteralManager().getObject((Literal) occurrenceBinding.getValue());
          }
        }

        int occurrence = 0;
        if(withOccurrence) {
          occurrence = (Integer) manager.getLiteralManager().getObject((Literal) occurrenceBinding.getValue());
        }

        if(sid.equals(currentsid) == false || currentOccurrence != occurrence) {
          martBuilder.withData(currentsid, currentOccurrence, values);
          values = new Object[varIndex.size()];
          currentsid = sid;
          currentOccurrence = occurrence;
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
      if(currentsid != null) {
        martBuilder.withData(currentsid, currentOccurrence, values);
      }
    } catch(Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
