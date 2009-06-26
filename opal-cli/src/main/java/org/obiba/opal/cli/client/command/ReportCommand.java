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
import org.obiba.opal.sesame.report.ReportBuilder;
import org.obiba.opal.sesame.report.XStreamReportLoader;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.query.QueryLanguage;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class ReportCommand extends AbstractContextLoadingCommand<ReportCommandOptions> {
  private SesameManager manager;

  @Override
  public void executeWithContext() {
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }
    TransactionTemplate transaction = getBean("transactionTemplate");

    transaction.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        try {
          SesameManagerFactory managerFactory = getBean("elmoManagerFactory");
          managerFactory.setQueryLanguage(QueryLanguage.SPARQL);
          manager = managerFactory.createElmoManager();

          CsvSasMartBuilder martBuilder = new CsvSasMartBuilder();
          if(options.getOutput().isDirectory()) {
            martBuilder.setCsvDirectory(options.getOutput());
          } else {
            martBuilder.setCsvFileName(options.getOutput().getAbsolutePath());
          }

          try {
            Report report = new XStreamReportLoader().loadReport(new FileInputStream(options.getReport()));
            ReportBuilder reportBuilder = new ReportBuilder(report, manager);
            reportBuilder.initialize();
            martBuilder.initialize();
            reportBuilder.build(martBuilder);
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
        return null;
      }
    });

  }

}
