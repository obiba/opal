/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.obiba.core.util.StreamUtil;
import org.obiba.core.util.StringUtil;
import org.obiba.opal.cli.client.command.options.SparqlCommandOptions;
import org.obiba.opal.elmo.concepts.Opal;
import org.obiba.opal.sesame.repository.OpalRepositoryManager;
import org.openrdf.model.Namespace;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

public class SparqlCommand extends AbstractCommand<SparqlCommandOptions> {

  private OpalRepositoryManager repoManager;

  private RepositoryConnection connection;

  private File output;

  private QueryLanguage language = QueryLanguage.SPARQL;

  private SparqlOptions prompt;

  public void execute() {
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }

    ConfigurableApplicationContext context = null;
    try {
      // Load Spring Context
      System.console().printf("Loading context\n");
      context = loadContext();
      repoManager = (OpalRepositoryManager) context.getBean("opalRepositoryManager");

      connection = repoManager.getDataRepository().getConnection();
      if(connection.getNamespace("opal") == null) {
        connection.setNamespace("opal", Opal.NS);
        connection.setNamespace("rdf", RDF.NAMESPACE);
        connection.setNamespace("rdfs", RDFS.NAMESPACE);
        connection.setNamespace("owl", OWL.NAMESPACE);
      }

      while(prompt()) {
        if(this.prompt.isOutput()) {
          output = this.prompt.getOutput();
          if(output.getName().equals("-")) {
            output = null;
          }
        } else if(this.prompt.isNs()) {
          iterateNamespaces(new NamespaceCallback() {
            public void callback(Namespace ns) {
              System.console().printf("%s:%s\n", ns.getPrefix(), ns.getName());
            }
          });
        } else if(this.prompt.isLanguage()) {
          try {
            language = QueryLanguage.valueOf(prompt.getLanguage());
          } catch(Exception e) {
            System.console().printf("Invalid query language [%s]\n", prompt.getLanguage());
          }
        } else if(this.prompt.isQuery()) {
          query();
        }
      }

    } catch(RuntimeException e) {
      System.err.println(e);
      e.printStackTrace();
    } catch(RepositoryException e) {
      throw new RuntimeException(e);
    } finally {
      if(connection != null) {
        try {
          connection.close();
        } catch(RepositoryException e) {
        }
      }
      if(context != null) {
        context.close();
      }
    }
  }

  private ConfigurableApplicationContext loadContext() {
    return new ClassPathXmlApplicationContext("classpath:/spring/opal-cli/context.xml");
  }

  private void query() throws RepositoryException {
    String queryString = StringUtil.collectionToString(this.prompt.getQuery(), " ");

    FileOutputStream os = null;
    try {
      PrefixBuilderCallback p = new PrefixBuilderCallback();
      iterateNamespaces(p);
      queryString = p.appendQuery(queryString);
      System.console().printf("Parsing [%s]\n", queryString);
      TupleQuery query = connection.prepareTupleQuery(language, queryString);
      SPARQLResultsXMLWriter writer = null;
      if(output != null) {
        writer = new SPARQLResultsXMLWriter(os = new FileOutputStream(output));
      } else {
        writer = new SPARQLResultsXMLWriter(System.out);
      }
      query.evaluate(writer);
    } catch(MalformedQueryException e) {
      System.console().printf("Invalid query: %s\n", e.getMessage());
      return;
    } catch(QueryEvaluationException e) {
      System.console().printf("Cannot evaluate query: %s\n", e.getMessage());
      return;
    } catch(TupleQueryResultHandlerException e) {
      System.console().printf("Cannot handle result: %s\n", e.getMessage());
      return;
    } catch(FileNotFoundException e) {
      System.console().printf("Cannot write to output file: %s\n", e.getMessage());
      return;
    } finally {
      StreamUtil.silentSafeClose(os);
    }
  }

  private boolean prompt() {
    this.prompt = null;
    while(this.prompt == null) {
      System.console().printf("> ");
      String[] args = System.console().readLine().split(" ");
      try {
        this.prompt = CliFactory.parseArguments(SparqlOptions.class, args);
      } catch(RuntimeException e) {
        System.err.println(e.getMessage());
      } catch(ArgumentValidationException e) {
        System.err.println(e.getMessage());
      }
    }
    return !this.prompt.isQuit();
  }

  private void iterateNamespaces(NamespaceCallback callback) throws RepositoryException {
    RepositoryResult<Namespace> result = connection.getNamespaces();
    try {
      while(result.hasNext()) {
        Namespace ns = result.next();
        callback.callback(ns);
      }
    } finally {
      result.close();
    }
  }

  private interface NamespaceCallback {
    public void callback(Namespace ns);
  }

  private class PrefixBuilderCallback implements NamespaceCallback {
    StringBuilder sb = new StringBuilder();

    public void callback(Namespace ns) {
      if(language == QueryLanguage.SPARQL) {
        sb.append("PREFIX ").append(ns.getPrefix()).append(":<").append(ns.getName()).append(">\n");
      } else if(language == QueryLanguage.SERQL) {
        if(sb.length() > 0) {
          sb.append(',');
        }
        sb.append(ns.getPrefix()).append(" = <").append(ns.getName()).append(">\n");
      }
    }

    public String appendQuery(String query) {
      if(language == QueryLanguage.SPARQL) {
        return sb.append(query).toString();
      } else if(language == QueryLanguage.SERQL) {
        return sb.insert(0, query + " USING NAMESPACE ").toString();
      }
      return query;
    }
  }

  public interface SparqlOptions {

    @Option(longName = "ns")
    public boolean isNs();

    @Option(shortName = { "q" }, longName = "quit")
    public boolean isQuit();

    @Option(shortName = "o")
    public File getOutput();

    public boolean isOutput();

    @Option(shortName = "l")
    public String getLanguage();

    public boolean isLanguage();

    @Unparsed()
    public List<String> getQuery();

    public boolean isQuery();
  }
}
