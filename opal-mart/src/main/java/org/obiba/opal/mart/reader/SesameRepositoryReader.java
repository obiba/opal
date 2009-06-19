package org.obiba.opal.mart.reader;

import javax.xml.namespace.QName;

import org.obiba.opal.elmo.owl.concepts.DataItemClass;
import org.obiba.opal.mart.QueryResult;
import org.obiba.opal.mart.QueryResultReader;
import org.openrdf.OpenRDFException;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class SesameRepositoryReader implements QueryResultReader, ItemStream {

  private SesameManagerFactory managerFactory;

  private String query;

  private SesameManager manager;

  private TupleQueryResult tqr;

  public void setQuery(String query) {
    this.query = query;
  }

  public void setManager(SesameManagerFactory managerFactory) {
    this.managerFactory = managerFactory;
  }

  public void update(ExecutionContext executionContext) throws ItemStreamException {
  }

  public void open(ExecutionContext executionContext) throws ItemStreamException {
    manager = managerFactory.createElmoManager();
    try {
      TupleQuery tq = manager.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
      tqr = tq.evaluate();
    } catch (OpenRDFException e) {
      throw new ItemStreamException(e);
    }
  }

  public void close() throws ItemStreamException {
    if (tqr != null) {
      try {
        tqr.close();
      } catch (QueryEvaluationException e) {
        throw new ItemStreamException(e);
      }
    }

    if (manager != null) {
      manager.close();
    }
  }

  public QueryResult read() throws Exception, UnexpectedInputException, ParseException {
    if (tqr.hasNext()) {
      return new TupleQueryQueryResult(tqr.next());
    }
    return null;
  }

  private class TupleQueryQueryResult implements QueryResult {

    private BindingSet bindingSet;

    public TupleQueryQueryResult(BindingSet bindingSet) {
      this.bindingSet = bindingSet;
    }

    public DataItemClass getDataItemClass() {
      Binding varBinding = bindingSet.getBinding("var");
      URI varURI = (URI) varBinding.getValue();
      QName varQName = new QName(varURI.getNamespace(), varURI.getLocalName());
      return manager.find(DataItemClass.class, varQName);
    }

    public int getOccurrence() {
      Binding occurrence = bindingSet.getBinding("occ");
      if (occurrence == null) {
        return 0;
      }
      return (Integer) manager.getLiteralManager().getObject((Literal) occurrence.getValue());
    }

    public String getEntityId() {
      return bindingSet.getBinding("sid").getValue().stringValue();
    }

    public Object getValue() {
      Binding value = bindingSet.getBinding("value");
      if (value == null) {
        return null;
      }
      return manager.getLiteralManager().getObject((Literal) value.getValue());
    }

    public boolean hasOccurrence() {
      Binding occurrence = bindingSet.getBinding("occ");
      return occurrence != null;
    }

  }

}
