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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.obiba.opal.cli.client.command.options.ReportCommandOptions;
import org.obiba.opal.core.mart.sas.ISasMartBuilder;
import org.obiba.opal.core.mart.sas.impl.CsvSasMartBuilder;
import org.obiba.opal.elmo.OpalOntologyManager;
import org.obiba.opal.elmo.concepts.Category;
import org.obiba.opal.elmo.concepts.DataVariable;
import org.obiba.opal.elmo.concepts.Opal;
import org.obiba.opal.elmo.owl.concepts.CategoryClass;
import org.obiba.opal.elmo.owl.concepts.DataEntryFormClass;
import org.obiba.opal.elmo.owl.concepts.DataItemClass;
import org.openrdf.OpenRDFException;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

public class ReportCommand extends AbstractContextLoadingCommand<ReportCommandOptions> {
  private SesameManager manager;

  @Override
  public void executeWithContext() {
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }
    SesameManagerFactory managerFactory = getBean("elmoManagerFactory");

    try {
      managerFactory.setQueryLanguage(QueryLanguage.SPARQL);
      manager = managerFactory.createElmoManager();

      RepositoryConnection connection = manager.getConnection();
      if(connection.getNamespace("opal") == null) {
        connection.setNamespace("opal", Opal.NS);
        connection.setNamespace("rdf", RDF.NAMESPACE);
        connection.setNamespace("rdfs", RDFS.NAMESPACE);
        connection.setNamespace("owl", OWL.NAMESPACE);
      }
      ReportType type = parseReportType();

      CsvSasMartBuilder martBuilder = new CsvSasMartBuilder();
      martBuilder.setCsvDirectory(this.options.getOutput());

      try {
        martBuilder.initialize();
        type.createBuilder(manager, this.options.getOptions()).build(martBuilder);
      } catch(Exception e) {
        throw new RuntimeException(e);
      } finally {
        try {
          martBuilder.shutdown();
        } catch(Exception e) {
        }
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    } finally {
      if(manager != null) {
        manager.close();
      }
    }

  }

  public ReportType parseReportType() {
    return ReportType.valueOf(this.options.getType().toUpperCase());
  }

  public enum ReportType {
    DEF {
      public ReportBuilder createBuilder(SesameManager manager, List<String> options) {
        return new DefReportBuilder(manager, options);
      }
    };

    public abstract ReportBuilder createBuilder(SesameManager manager, List<String> options);
  }

  public interface ReportBuilder {

    public void build(ISasMartBuilder martBuilder);

  }

  public static class DefReportBuilder implements ReportBuilder {

    private final OpalOntologyManager opal;

    private final List<String> options;

    private final SesameManager manager;

    public DefReportBuilder(SesameManager manager, List<String> options) {
      this.manager = manager;
      this.options = options;
      try {
        opal = new OpalOntologyManager();
      } catch(OpenRDFException e) {
        throw new RuntimeException(e);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    public boolean accept(DataItemClass variable) {
      if(variable.isMultiple() == true) {
        return false;
      }
      Set<org.openrdf.concepts.rdfs.Class> superClasses = variable.getRdfsSubClassOf();
      if(superClasses.contains(opal.getOpalClass(DataVariable.class))) {
        return false;
      }
      if(superClasses.contains(opal.getOpalClass(Category.class))) {
        // Only include category if parent is multiple
        CategoryClass cc = manager.designateEntity(variable, CategoryClass.class);
        return cc.getParent().isMultiple();
      }
      return true;
    }

    public void build(ISasMartBuilder martBuilder) {
      QName defQName = QName.valueOf(options.get(0));
      DataEntryFormClass defClass = manager.find(DataEntryFormClass.class, defQName);
      if(defClass == null) {
        System.out.println("No such DEF " + defQName);
        return;
      }

      Map<String, Integer> varIndex = new HashMap<String, Integer>();

      Set<DataItemClass> dataVariables = defClass.getDataVariables();
      String names[] = new String[dataVariables.size()];
      int i = 0;
      for(DataItemClass var : dataVariables) {
        if(accept(var)) {
          String varName = var.getQName().getLocalPart();
          varIndex.put(varName, i);
          names[i++] = varName;
        }

      }
      martBuilder.setVariableNames(names);

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT ?sid ?var ?value {?entity opal:identifier ?sid . ?entity rdf:type opal:Participant . ?ds opal:isForEntity ?entity . ?varData opal:withinDataset ?ds . ?varData rdf:type ?var . ?var opal:withinDataEntryForm ?def . OPTIONAL { {?varData opal:dataValue ?value} UNION {?varData opal:hasCategory ?c . ?c rdf:type ?cVar . ?cVar opal:code ?value} UNION {?varData rdf:type [ opal:code ?value ] }} } ORDER BY ?sid");
      SparqlUtil.prefixQuery(manager.getConnection(), builder);

      try {
        TupleQuery query = manager.getConnection().prepareTupleQuery(builder.toString());
        query.setBinding("def", manager.getConnection().getValueFactory().createURI(defQName.getNamespaceURI(), defQName.getLocalPart()));
        System.out.println(query.toString());

        Object values[] = new Object[varIndex.size()];

        String currentsid = null;
        TupleQueryResult trq = query.evaluate();
        while(trq.hasNext()) {
          BindingSet set = trq.next();
          Binding entityBinding = set.getBinding("sid");
          Binding varBinding = set.getBinding("var");
          Binding valueBinding = set.getBinding("value");

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
          Integer index = varIndex.get(varURI.getLocalName());
          // Set value if there is one to set and if variable is included in report.
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
}
