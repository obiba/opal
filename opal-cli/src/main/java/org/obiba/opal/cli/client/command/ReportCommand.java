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

import org.obiba.opal.cli.client.command.options.ReportCommandOptions;
import org.obiba.opal.core.mart.sas.impl.CsvSasMartBuilder;
import org.obiba.opal.sesame.report.Report;
import org.obiba.opal.sesame.report.XStreamReportLoader;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.query.QueryLanguage;

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

      CsvSasMartBuilder martBuilder = new CsvSasMartBuilder();
      if(this.options.getOutput().isDirectory()) {
        martBuilder.setCsvDirectory(this.options.getOutput());
      } else {
        martBuilder.setCsvFileName(this.options.getOutput().getAbsolutePath());
      }

      try {
        Report report = new XStreamReportLoader().loadReport(new FileInputStream(options.getReport()));

        martBuilder.initialize();
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
