/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 *
 */
public class RepositoryResourceFactory implements ResourceFactory {

  private Repository repository;

  private String baseUri;

  private Map<URI, Map<String, URI>> resourceCache = new HashMap<URI, Map<String, URI>>();

  private Map<URI, String> typeLabelCache = new HashMap<URI, String>();

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public URI findResource(String property, String value) {
    RepositoryConnection connection = null;
    try {
      connection = doGetConnection();
      TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, "select ?s {?s ?property ?value}");
      q.setBinding("property", repository.getValueFactory().createURI(property));
      q.setBinding("value", repository.getValueFactory().createLiteral(value));
      TupleQueryResult result = q.evaluate();
      if(result.hasNext()) {
        return (URI) result.next().getBinding("s").getValue();
      }
      return null;
    } catch(QueryEvaluationException e) {
      throw new RuntimeException(e);
    } catch(RepositoryException e) {
      throw new RuntimeException(e);
    } catch(MalformedQueryException e) {
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

  public URI findResource(URI type, String identifier) {
    try {
      String resourceLabel = doGetLabel(type);
      URI resource = repository.getValueFactory().createURI(baseUri + "/" + resourceLabel + "/" + identifier);
      return resource;
    } catch(RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

  public URI getResource(URI type, String identifier) {
    try {
      String resourceLabel = doGetLabel(type);
      URI resource = repository.getValueFactory().createURI(baseUri + "/" + resourceLabel + "/" + identifier);
      cacheResource(type, identifier, resource);
      return resource;
    } catch(RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

  protected RepositoryConnection doGetConnection() throws RepositoryException {
    return repository.getConnection();
  }

  protected void cacheResource(URI type, String name, URI resource) throws RepositoryException {
    Map<String, URI> typeCache = resourceCache.get(type);
    if(typeCache == null) {
      resourceCache.put(type, typeCache = new HashMap<String, URI>());
    }

    if(typeCache.containsKey(name)) {
      RepositoryConnection connection = doGetConnection();
      try {
        doAddResource(connection, type, resource);
      } finally {
        if(connection != null) {
          connection.close();
        }
      }
    }
    typeCache.put(name, resource);
  }

  protected void doAddResource(RepositoryConnection connection, URI type, URI resource) throws RepositoryException {
    connection.add(resource, RDF.TYPE, type);
  }

  protected String doFindLabel(RepositoryConnection connection, URI type) throws RepositoryException {
    RepositoryResult<Statement> labels = connection.getStatements(type, RDFS.LABEL, null, false);
    try {
      if(labels == null || labels.hasNext() == false) {
        throw new IllegalStateException("cannot determine rdfs:label for type " + type);
      }
      return labels.next().getObject().stringValue();
    } finally {
      if(labels != null) {
        labels.close();
      }
    }
  }

  protected String doGetLabel(URI type) throws RepositoryException {
    String label = typeLabelCache.get(type);
    if(label == null) {
      RepositoryConnection connection = doGetConnection();
      try {
        label = doFindLabel(connection, type);
        typeLabelCache.put(type, label);
      } finally {
        if(connection != null) {
          connection.close();
        }
      }
    }
    return label;
  }
}
