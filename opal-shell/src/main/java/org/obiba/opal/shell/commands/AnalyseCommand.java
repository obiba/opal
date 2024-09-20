/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.views.View;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.OpalAnalysisResultService;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.r.magma.MagmaAssignROperation;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RCacheHelper;
import org.obiba.opal.r.service.RServerSession;
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

import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@CommandUsage(
    description = "Applies analysis on a table or a set of variables.",
    syntax = "Syntax: analyse [--project PROJECT] [--analyses ANALYSE-JSON-LIST]")
public class AnalyseCommand extends AbstractOpalRuntimeDependentCommand<AnalyseCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(AnalyseCommand.class);

  private static final String INVALID_NAME_CHARACTERS = "#%&{}\\\\<>*?/$!'\\:@";

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  @Autowired
  private ProjectService projectService;

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private TransactionTemplate txTemplate;

  @Autowired
  private DataExportService dataExportService;

  @Autowired
  private RCacheHelper rCacheHelper;

  @Autowired
  private OpalAnalysisResultService analysisResultService;

  @Autowired
  private OpalAnalysisService analysisService;

  @Override
  public int execute() {
    Project project = projectService.getProject(options.getProject());
    Datasource datasource = project.getDatasource();

    List<AnalyseCommandOptions.AnalyseOptions> analyses = options.getAnalyses();
    AnalysisValueTableResolver valueTableResolver = new AnalysisValueTableResolver();
    Set<String> loadedTables = Sets.newHashSet();

    try (RSessionHandlerImpl sessionHandler = new RSessionHandlerImpl()) {

      analyses.forEach(analyseOptions -> {
        ensureValidName(analyseOptions.getName());

        ValueTable table = datasource.getValueTable(analyseOptions.getTable());
        String pluginName = analyseOptions.getPlugin();
        RAnalysisService rAnalysisService = (RAnalysisService) opalRuntime.getServicePlugin(pluginName);
        rAnalysisService.setOpalFileSystemPathResolver(new OpalPathResolver());

        // if analysis already exists use the one from database
        OpalAnalysis existingAnalysis = analysisService.getAnalysis(options.getProject(), analyseOptions.getTable(), analyseOptions.getName());

        String variables = analyseOptions.getVariables();

        if (existingAnalysis != null) {
          variables = String.join(",", existingAnalysis.getVariables());
          log.warn("Analysis {} already exists, using existing one instead of provided options with params \"{}\"", existingAnalysis.toString(), analyseOptions.getParams().toString());
        }

        ValueTable targetValueTable = valueTableResolver.resolve(table, variables);
        String tibbleName = targetValueTable.getName();

        if (!loadedTables.contains(tibbleName)) {
          loadedTables.add(tibbleName);
          new ValueTableToTibbleWriter().write(targetValueTable, sessionHandler);
        }

        String templateName = analyseOptions.getTemplate();
        log.info("Analysing {} table using {} routines.", tibbleName, String.format("%s::%s", pluginName, templateName));

        RAnalysis.Builder builder = existingAnalysis == null ?
            fromOptions(options.getProject(), analyseOptions) : fromExistingOpalAnalysis(existingAnalysis);

        if (!variables.isEmpty()) {
          builder.variables(StreamSupport.stream(targetValueTable.getVariables().spliterator(), false)
              .map(Variable::getName)
              .collect(Collectors.toList()));
        }

        RAnalysis analysis = builder.session(sessionHandler.getSession()).symbol(RUtils.getSymbol(tibbleName)).build();

        if (existingAnalysis == null)
          analysisService.save(OpalAnalysis.Builder.create(datasource.getName(), table.getName(), analysis).build());

        RAnalysisResult result = rAnalysisService.analyse(analysis);

        log.info("Analysed {} table with status {}.", tibbleName, result.getStatus());
        log.debug("Analysis result:\nstarted: {}\nended: {}\nstatus: {}\nmessage: {}\nreport: {}",
            result.getStartDate(),
            result.getEndDate(),
            result.getStatus(),
            result.getMessage(),
            result.getReportPath());

        analysisResultService.save(new OpalAnalysisResult(result, options.getProject(), analyseOptions.getTable()));
      });

    }

    return 0;
  }

  private RAnalysis.Builder fromOptions(String project, AnalyseCommandOptions.AnalyseOptions analyseOptions) {
    return RAnalysis.create(
        project,
        analyseOptions.getTable(),
        analyseOptions.getName(),
        analyseOptions.getPlugin(),
        analyseOptions.getTemplate()).parameters(analyseOptions.getParams());
  }

  private RAnalysis.Builder fromExistingOpalAnalysis(OpalAnalysis existingAnalysis) {
    return RAnalysis.create(
        existingAnalysis.getDatasource(),
        existingAnalysis.getTable(),
        existingAnalysis.getName(),
        existingAnalysis.getPluginName(),
        existingAnalysis.getTemplateName()).parameters(existingAnalysis.getParameters());
  }

  private class AnalysisValueTableResolver {

    ValueTable resolve(ValueTable source, String variableNamesCsv) {
      List<String> variableNames = getVariableNames(variableNamesCsv);
      if (variableNames.isEmpty()) return source;

      return createView(source, variableNames);
    }

    private ValueTable createView(ValueTable table, List<String> variableNames) {
      String viewName = table.getName() + "View";
      ValueTable view = View.Builder
          .newView(viewName, table)
          .select(variable -> variableNames.contains(variable.getName()))
          .build();

      if (view.getVariableCount() == 0) {
        throw new RuntimeException(String.format("Invalid variable names provided: %s", variableNames.toString()));
      }

      return view;
    }

    private List<String> getVariableNames(String variableNamesCsv) {
      List<String> variableNames = Lists.newArrayList();
      if (variableNamesCsv != null && !"".equals(variableNamesCsv)) {
        variableNames = Lists.newArrayList(variableNamesCsv.split("\\s*,\\s*"));
      }

      return variableNames;
    }

  }

  private void ensureValidName(String analysisName) {
    Pattern pattern = Pattern.compile(String.format("[%s]", INVALID_NAME_CHARACTERS));
    Matcher matcher = pattern.matcher(analysisName);
    if (matcher.find()) {
      throw new IllegalArgumentException("Analysis name cannot contain these characters: " + INVALID_NAME_CHARACTERS);
    }
  }

  private class ValueTableToTibbleWriter {

    void write(@NotNull ValueTable valueTable, RSessionHandler rSessionHandler) {
      if (valueTable.getValueSetCount() > 0) {
        String finalSymbol = RUtils.getSymbol(valueTable.getName());
        rSessionHandler
            .getSession()
            .execute(new MagmaAssignROperation(finalSymbol, valueTable, dataExportService, rCacheHelper, "id"));
      }
    }

  }

  private class RSessionHandlerImpl implements RSessionHandler, Closeable {

    private final RServerSession rSession;

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
        FileObject fileObject = opalFileSystemService.getFileSystem().getRoot().resolveFile(path);
        // check security
        if (fileObject.exists()) {
          if (!fileObject.isWriteable()) {
            throw new IllegalArgumentException("File cannot be written: " + path);
          }
        } else if (!fileObject.getParent().isWriteable()) {
          throw new IllegalArgumentException("File cannot be written: " + path);
        }
        return opalFileSystemService.getFileSystem().getLocalFile(fileObject);
      } catch (FileSystemException e) {
        throw new IllegalArgumentException("Failed resolving file path: " + path);
      }

    }
  }
}
