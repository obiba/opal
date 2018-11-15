package org.obiba.opal.shell.commands;

import com.google.common.collect.Sets;

import java.io.Closeable;
import java.io.File;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.OpalAnalysisResultService;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.r.magma.MagmaAssignROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.shell.commands.options.AnalyseCommandOptions;
import org.obiba.opal.spi.analysis.AnalysisService;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.obiba.opal.spi.r.RUtils;
import org.obiba.opal.spi.r.analysis.RAnalysis;
import org.obiba.opal.spi.r.analysis.RAnalysisResult;
import org.obiba.opal.spi.r.analysis.RAnalysisService;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;


@CommandUsage(
  description = "Applies analysis on a table or a set of variables.",
  syntax = "Syntax: analyse [--project PROJECT] [--analyses ANALYSE-JSON-LIST]")
public class AnalyseCommand extends AbstractOpalRuntimeDependentCommand<AnalyseCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(AnalyseCommand.class);

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private ProjectService projectService;

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private TransactionTemplate txTemplate;

  @Autowired
  private OpalAnalysisResultService analysisResultService;

  @Autowired
  private OpalAnalysisService analysisService;

  @Override
  public int execute() {
    // TODO validate subject permissions

    String projectName = options.getProject();
    Project project = projectService.getProject(projectName);
    Datasource datasource = project.getDatasource();

    List<AnalyseCommandOptions.AnalyseOptions> analyses = options.getAnalyses();

    Set<String> loadedTables = Sets.newHashSet();
    try (RSessionHandlerImpl sessionHandler = new RSessionHandlerImpl()) {

      analyses.forEach(analyseOptions -> {
        String tableName = analyseOptions.getTable();
        if (!datasource.hasValueTable(tableName)) {
          throw new NoSuchValueTableException(tableName);
        }

        String pluginName = analyseOptions.getPlugin();
        RAnalysisService rAnalysisService = (RAnalysisService) opalRuntime.getServicePlugin(pluginName);
        rAnalysisService.setOpalFileSystemPathResolver(new OpalPathResolver());

        if (!loadedTables.contains(tableName)) {
          loadedTables.add(tableName);
          new ValueTableToTibbleWriter().write(datasource, tableName, sessionHandler);
        }

        String templateName = analyseOptions.getTemplate();
        log.info("Analysing {} table using {} routines.", tableName, String.format("%s::%s", pluginName, templateName));

        RAnalysis analysis = RAnalysis.create(analyseOptions.getName(), analyseOptions.getTemplate())
          .session(sessionHandler.getSession())
          .symbol(RUtils.getSymbol(tableName))
          .parameters(analyseOptions.getParams())
          .build();

        analysisService.save(new OpalAnalysis(analysis));

        RAnalysisResult result = rAnalysisService.analyse(analysis);

        log.info("Analysis result:\nstarted: {}\nended: {}\nstatus: {}\nmessage: {}\nreport: {}",
          result.getStartDate(),
          result.getEndDate(),
          result.getStatus(),
          result.getMessage(),
          result.getReportPath());

        analysisResultService.save(new OpalAnalysisResult(result));
      });

    }

    return 0;
  }


  private class ValueTableToTibbleWriter {

    void write(@NotNull Datasource datasource, String tableName, RSessionHandler rSessionHandler) {
      ValueTable valueTable = datasource.getValueTable(tableName);
      if (valueTable.getValueSetCount() > 0) {
        rSessionHandler
          .getSession()
          .execute(new MagmaAssignROperation(RUtils.getSymbol(tableName), valueTable, txTemplate, "id"));
      }
    }

  }

  private class RSessionHandlerImpl implements RSessionHandler, Closeable {
    private final OpalRSession rSession;

    RSessionHandlerImpl() {
      rSession = opalRSessionManager.newSubjectRSession();
      rSession.setExecutionContext("Analyse");
    }

    @Override
    public ROperationTemplate getSession() {
      return rSession;
    }

    @Override
    public void onDispose() {
      opalRSessionManager.removeRSession(rSession.getId());
    }

    @Override
    public void close() {
      onDispose();
    }
  }

  private class OpalPathResolver implements AnalysisService.OpalFileSystemPathResolver {

    @Override
    public File resolve(String path) {
      try {
        FileObject fileObject = opalRuntime.getFileSystem().getRoot().resolveFile(path);
        // check security
        if (fileObject.exists()) {
          if (!fileObject.isWriteable()) {
            throw new IllegalArgumentException("File cannot be written: " + path);
          }
        } else if (!fileObject.getParent().isWriteable()) {
          throw new IllegalArgumentException("File cannot be written: " + path);
        }
        return opalRuntime.getFileSystem().getLocalFile(fileObject);
      } catch (FileSystemException e) {
        throw new IllegalArgumentException("Failed resolving file path: " + path);
      }

    }
  }
}
