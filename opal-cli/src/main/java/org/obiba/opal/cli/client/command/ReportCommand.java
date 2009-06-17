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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.obiba.core.util.StringUtil;
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
import org.obiba.opal.sesame.report.Report;
import org.obiba.opal.sesame.report.XStreamReportLoader;
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
      CsvSasMartBuilder martBuilder = new CsvSasMartBuilder();
      martBuilder.setCsvDirectory(this.options.getOutput());

      try {
        martBuilder.initialize();
        Report report = new XStreamReportLoader().loadReport(new FileInputStream(options.getReport()));
        report.build(manager, martBuilder);
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

}
