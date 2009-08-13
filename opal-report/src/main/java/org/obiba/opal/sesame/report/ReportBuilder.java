package org.obiba.opal.sesame.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.obiba.opal.core.mart.sas.ISasMartBuilder;
import org.obiba.opal.elmo.concepts.CategoricalItem;
import org.obiba.opal.elmo.concepts.Category;
import org.obiba.opal.elmo.concepts.DataItem;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportBuilder {

  private static final Logger log = LoggerFactory.getLogger(ReportBuilder.class);

  private Report report;

  private SesameManager manager;

  private Map<QName, IVariableValueHandler> variableValueHandlers = new HashMap<QName, IVariableValueHandler>();

  private Map<QName, IVariableNullValueHandler> variableNullValueHandlers = new HashMap<QName, IVariableNullValueHandler>();

  List<DataItem> reportedItems = new LinkedList<DataItem>();

  Map<QName, Integer> varIndex = new HashMap<QName, Integer>();

  ArrayList<String> names = new ArrayList<String>();

  public ReportBuilder(Report report, SesameManager manager) {
    this.report = report;
    this.manager = manager;
  }

  public void initialize() {
    List<IDataItemFilter> filters = report.getFilters();
    for(IDataItemSelection s : report.getSelections()) {
      for(DataItem dataItem : s.getSelection(manager)) {
        boolean accept = true;
        for(IDataItemFilter filter : filters) {
          if(filter.accept(dataItem) == false) {
            log.debug("filtered: {}", dataItem.getQName());
            accept = false;
            break;
          }
        }
        if(accept == true) {
          int itemIndex = reportedItems.size();
          StringBuilder name = new StringBuilder(dataItem.getQName().getLocalPart());
          names.add(name.toString());
          varIndex.put(dataItem.getQName(), itemIndex);
          reportedItems.add(dataItem);
//          createValueHandler(dataItem);
          log.debug("reported item {}: {}", itemIndex, dataItem.getQName());
        }

      }
    }

  }

  public void build(ISasMartBuilder martBuilder) {

    ReportQueryBuilder builder = new ReportQueryBuilder();

    if(report.isWithOccurrence()) {
      martBuilder.enableOccurrences();
      builder.withOccurrence();
    }

    martBuilder.setVariableNames(names.toArray(new String[names.size()]));

    for(IDataItemSelection selection : report.getSelections()) {
      selection.contribute(builder, manager);
    }
    for(IDataItemFilter filter : report.getFilters()) {
      filter.contribute(builder, manager);
    }

    try {
      TupleQuery query = builder.build(manager);
      log.info("Evaluating query.");
      TupleQueryResult trq = query.evaluate();
      log.info("Evaluation done. Creating report.");

      ReportItem currentItem = null;
      while(trq.hasNext()) {
        BindingSet set = trq.next();
        Binding entityBinding = set.getBinding("sid");
        Binding occurrenceBinding = set.getBinding("occ");
        Binding varBinding = set.getBinding("var");

        log.trace("bindingSet {}", set);

        String sid = entityBinding.getValue().stringValue();
        int occurrence = 0;
        if(report.isWithOccurrence()) {
          if(occurrenceBinding == null) {
            throw new IllegalStateException("No occurrence for var " + varBinding.getValue());
          }
          occurrence = (Integer) manager.getLiteralManager().getObject((Literal) occurrenceBinding.getValue());
        }

        if(currentItem == null) {
          currentItem = new ReportItem(sid, occurrence);
        }

        if(currentItem.isSameItem(sid, occurrence) == false) {
          addToReport(martBuilder, currentItem);
          currentItem = new ReportItem(sid, occurrence);
        }

        URI varURI = (URI) varBinding.getValue();
        QName varQName = new QName(varURI.getNamespace(), varURI.getLocalName());
        Integer index = varIndex.get(varQName);

        if(index != null) {
          IVariableValueHandler handler = variableValueHandlers.get(varQName);
          if(handler != null) {
            currentItem.values[index] = handler.getValue(manager, varQName, set);
          }
        }
      }
      if(currentItem != null) {
        addToReport(martBuilder, currentItem);
      }
    } catch(Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void addToReport(ISasMartBuilder martBuilder, ReportItem item) {
    // Handler missing values
    Object values[] = item.values;
    for(Entry<QName, Integer> entry : this.varIndex.entrySet()) {
      QName varQName = entry.getKey();
      Integer index = entry.getValue();
      if(values[index] == null) {
        IVariableNullValueHandler nullHandler = this.variableNullValueHandlers.get(varQName);
        if(nullHandler != null) {
          values[index] = nullHandler.getNullValue(manager, varQName);
        }
      }
    }

    martBuilder.withData(item.id, item.occurrence, values);
  }

  private interface IVariableValueHandler {
    Object getValue(SesameManager manager, QName variable, BindingSet set);
  }

  private interface IVariableNullValueHandler {
    Object getNullValue(SesameManager manager, QName variable);
  }

  private static class ContinuousVariableValueHandler implements IVariableValueHandler {
    static final ContinuousVariableValueHandler instance = new ContinuousVariableValueHandler();

    public Object getValue(SesameManager manager, QName variable, BindingSet set) {
      Binding valueBinding = set.getBinding("value");
      if(valueBinding != null) {
        return manager.getLiteralManager().getObject((Literal) valueBinding.getValue());
      }
      return null;
    }
  }

  private class SingleChoiceCategoryValueHandler implements IVariableValueHandler {

    private Map<String, Integer> categoryCode = new HashMap<String, Integer>();

    SingleChoiceCategoryValueHandler(DataItem dataItem) {
      CategoricalItem categorical = (CategoricalItem) dataItem;
      for(Category category : categorical.getCategories()) {
        categoryCode.put(category.getRdfsLabel(), category.getCode());
      }

    }

    public Object getValue(SesameManager manager, QName variable, BindingSet set) {
      Binding valueBinding = set.getBinding("value");
      if(valueBinding != null) {
        return valueBinding.getValue().stringValue();
      }
      return null;
    }

  }

  private static class MultipleChoiceCategoryValueHandler implements IVariableValueHandler, IVariableNullValueHandler {

    static final MultipleChoiceCategoryValueHandler instance = new MultipleChoiceCategoryValueHandler();

    public Object getValue(SesameManager manager, QName variable, BindingSet set) {
      return 1;
    }

    public Object getNullValue(SesameManager manager, QName variable) {
      return 0;
    }

  }

  private class ReportItem {
    private String id;

    private int occurrence;

    private Object[] values;

    public ReportItem(String id, int occurrence) {
      this.id = id;
      this.occurrence = occurrence;
      values = new Object[varIndex.size()];
    }

    public boolean isSameItem(String id, int occurrence) {
      return this.id.equals(id) && this.occurrence == occurrence;
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj instanceof ReportItem) {
        ReportItem rhs = (ReportItem) obj;
        return this.id.equals(rhs.id) && this.occurrence == rhs.occurrence;
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      int h = 37;
      h += h * id.hashCode();
      h += h * occurrence;
      return h;
    }
  }
}
