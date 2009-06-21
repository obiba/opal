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
import java.io.OutputStream;
import java.util.List;

import org.obiba.core.util.StreamUtil;
import org.obiba.core.util.StringUtil;
import org.obiba.opal.cli.client.command.options.QueryCommandOptions;
import org.obiba.opal.elmo.concepts.Opal;
import org.obiba.opal.sesame.repository.OpalRepositoryManager;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.owl.OntologyWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.rss.RssWriter;
import org.openrdf.rio.turtle.TurtleWriter;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

public class QueryCommand extends AbstractContextLoadingCommand<QueryCommandOptions> {

  private OpalRepositoryManager repoManager;

  private RepositoryConnection connection;

  private File output;

  private QueryLanguage language = QueryLanguage.SPARQL;

  private PromptOptions prompt;

  @Override
  public void executeWithContext() {
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }

    try {
      repoManager = getBean("opalRepositoryManager");
      connection = repoManager.getDataRepository().getConnection();
      executeWithConnection();

    } catch(RuntimeException e) {
      throw e;
    } catch(RepositoryException e) {
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

  protected void executeWithConnection() throws RepositoryException {
    while(prompt()) {
      if(this.prompt.isOutput()) {
        output = this.prompt.getOutput();
        if(output.getName().equals("-")) {
          output = null;
        }
        System.console().printf("Results will be output to: %s\n", (output != null ? output.getAbsolutePath() : "stdout"));
      } else if(this.prompt.isNs()) {
        iterateNamespaces(new NamespaceCallback() {
          public void callback(Namespace ns) {
            System.console().printf("%s:%s\n", ns.getPrefix(), ns.getName());
          }
        });
      } else if(this.prompt.isLanguage()) {
        try {
          language = QueryLanguage.valueOf(prompt.getLanguage());
          System.console().printf("Query language set to [%s]\n", language.getName());
        } catch(Exception e) {
          System.console().printf("Invalid query language [%s]\n", prompt.getLanguage());
        }
      } else if(this.prompt.isStats()) {
        stats();
      } else if(this.prompt.isDump()) {
        dump(prompt.getDump());
      } else if(this.prompt.isQuery()) {
        query();
      }
    }
  }

  private void stats() {
    try {
      System.console().printf("Computing statistics...\n");
      RepositoryResult<Statement> rr = connection.getStatements(null, RDF.TYPE, connection.getValueFactory().createURI(Opal.NS + "Participant"), false);
      int p = rr.asList().size();
      System.console().printf("Number of participants: %d\n", p);
      System.console().printf("Number of statements: %d\n", connection.size());
    } catch(RepositoryException e) {
      throw new RuntimeException();
    }
  }

  private void dump(String handlerType) {
    DumpHandler handler = null;
    try {
      handler = DumpHandler.valueOf(handlerType.toUpperCase());
    } catch(RuntimeException e) {
      System.console().printf("Invalid dump handler type. Type must be one of %s\n", StringUtil.arrayToString((Object[]) DumpHandler.values()));
      return;
    }

    if(output == null) {
      System.console().printf("No output file specified. Specify one with -o option\n");
      return;
    }

    FileOutputStream os = null;
    try {
      os = new FileOutputStream(output);
      connection.export(handler.createHandler(os));
    } catch(Exception e) {
      System.console().printf("Error writing ontology: %s", e.getMessage());
    } finally {
      StreamUtil.silentSafeClose(os);
    }
  }

  private void query() throws RepositoryException {
    String queryString = StringUtil.collectionToString(this.prompt.getQuery(), " ");
    if(queryString == null || queryString.trim().length() == 0) {
      return;
    }

    FileOutputStream os = null;
    try {
      PrefixBuilderCallback p = new PrefixBuilderCallback();
      iterateNamespaces(p);
      queryString = p.appendQuery(queryString);
      System.console().printf("Parsing [%s]\n", queryString);
      TupleQuery query = connection.prepareTupleQuery(language, queryString);
      System.console().printf(query.toString());
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
        this.prompt = CliFactory.parseArguments(PromptOptions.class, args);
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

  public interface PromptOptions {

    @Option(longName = "ns")
    public boolean isNs();

    @Option(shortName = "d", longName = "dump")
    public String getDump();

    public boolean isDump();

    @Option(shortName = { "q" }, longName = "quit")
    public boolean isQuit();

    @Option(shortName = "o")
    public File getOutput();

    public boolean isOutput();

    @Option(shortName = "l")
    public String getLanguage();

    public boolean isLanguage();

    @Option(shortName = "s")
    public boolean isStats();

    @Unparsed()
    public List<String> getQuery();

    public boolean isQuery();
  }

  public enum DumpHandler {
    OWL {
      @Override
      public RDFHandler createHandler(OutputStream output) {
        return new OntologyWriter(output);
      }
    },
    RDFXML {
      @Override
      public RDFHandler createHandler(OutputStream output) {
        return new RDFXMLWriter(output);
      }
    },
    N3 {
      @Override
      public RDFHandler createHandler(OutputStream output) {
        return new N3Writer(output);
      }
    },
    TURTLE {
      @Override
      public RDFHandler createHandler(OutputStream output) {
        return new TurtleWriter(output);
      }
    },
    RSS {
      @Override
      public RDFHandler createHandler(OutputStream output) {
        return new RssWriter(output);
      }
    };

    public abstract RDFHandler createHandler(OutputStream output);
  }
}
