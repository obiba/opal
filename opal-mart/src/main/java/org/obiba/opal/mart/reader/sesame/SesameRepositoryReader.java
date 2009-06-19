package org.obiba.opal.mart.reader.sesame;

import org.obiba.opal.mart.QueryResult;
import org.obiba.opal.mart.QueryResultReader;
import org.openrdf.OpenRDFException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class SesameRepositoryReader implements QueryResultReader, ItemStream {

  private Repository repository;

  private BindingSetMapper<QueryResult> bindingSetMapper;

  private String query;

  private TupleQueryResult tqr;

  private int currentItemCount = 0;

  public void setQuery(String query) {
    this.query = query;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public void setBindingSetMapper(BindingSetMapper<QueryResult> bindingSetMapper) {
    this.bindingSetMapper = bindingSetMapper;
  }

  public void update(ExecutionContext executionContext) throws ItemStreamException {
  }

  public void open(ExecutionContext executionContext) throws ItemStreamException {
    try {
      TupleQuery tq = repository.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
      tqr = tq.evaluate();
    } catch(OpenRDFException e) {
      throw new ItemStreamException(e);
    }
  }

  public void close() throws ItemStreamException {
    if(tqr != null) {
      try {
        tqr.close();
      } catch(QueryEvaluationException e) {
        throw new ItemStreamException(e);
      }
    }
  }

  public QueryResult read() throws Exception, UnexpectedInputException, ParseException {
    if(tqr.hasNext()) {
      return bindingSetMapper.mapBindingSet(tqr.next(), currentItemCount++);
    }
    return null;
  }

}
