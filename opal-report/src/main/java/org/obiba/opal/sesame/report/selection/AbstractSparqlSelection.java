package org.obiba.opal.sesame.report.selection;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.opal.map.ResourceFactory;
import org.obiba.opal.sesame.report.QueryUtil;
import org.obiba.opal.sesame.repository.OpalRepositoryManager;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractSparqlSelection {
  
  private static final Logger log = LoggerFactory.getLogger(AbstractSparqlSelection.class);

  private OpalRepositoryManager repositoryManager;

  private ResourceFactory resourceFactory;

  private Map<String, Value> bindings;

  public void setOpalRepositoryManager(OpalRepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }
  
  public void setResourceFactory(ResourceFactory resourceFactory) {
    this.resourceFactory = resourceFactory;
  }

  protected ValueFactory getValueFactory() {
    return repositoryManager.getCatalogRepository().getValueFactory();
  }

  protected ResourceFactory getResourceFactory() {
    return resourceFactory;
  }

  protected Set<URI> doSelectDataItem(String sparql) throws RuntimeException {
    RepositoryConnection connection = null;

    try {
      connection = repositoryManager.getCatalogRepository().getConnection();
      StringBuilder builder = new StringBuilder(sparql);
      QueryUtil.prefixQuery(connection, builder);
      log.debug("Executing query: {}", builder);
      TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, builder.toString());

      for(Map.Entry<String, Value> entry : getBindings().entrySet()) {
        log.debug("{}={}", entry.getKey(), entry.getValue());
        query.setBinding(entry.getKey(), entry.getValue());
      }

      TupleQueryResult tqr = query.evaluate();

      Set<URI> items = new LinkedHashSet<URI>();
      while(tqr.hasNext()) {
        BindingSet set = tqr.next();
        Binding b = set.getBinding("dataItem");
        items.add((URI) b.getValue());
      }
      tqr.close();
      log.debug("Query returned {} items", items.size());
      return items;
    } catch(RepositoryException e) {
      throw new RuntimeException(e);
    } catch(MalformedQueryException e) {
      throw new RuntimeException(e);
    } catch(QueryEvaluationException e) {
      throw new RuntimeException(e);
    } finally {
      if(connection != null) {
        try {
          connection.close();
        } catch(RepositoryException e) {
        }
      }
    }
  }

  protected void setBinding(String name, Value value) {
    getBindings().put(name, value);
  }

  protected Map<String, Value> getBindings() {
    return bindings != null ? bindings : (bindings = new HashMap<String, Value>());
  }

}
