package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.tools.SimpleOrientDbQueryBuilder;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.spi.analysis.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.StreamSupport;

@Component
public class OpalAnalysisServiceImpl implements OpalAnalysisService {

  private static final Logger logger = LoggerFactory.getLogger(OpalAnalysisServiceImpl.class);

  private final OrientDbService orientDbService;

  @Autowired
  public OpalAnalysisServiceImpl(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @Override
  public OpalAnalysis getAnalysis(String datasource, String table, String name) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysis.class.getSimpleName())
      .whereClauses("datasource = ?", "table = ?", "name = ?")
      .build();
    return orientDbService.uniqueResult(OpalAnalysis.class, query, datasource, table, name);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalyses() {
    return orientDbService.list(OpalAnalysis.class);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalysesByDatasource(String datasource) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysis.class.getSimpleName())
      .whereClauses("datasource = ?")
      .order("desc")
      .build();

    return orientDbService.list(OpalAnalysis.class, query, datasource);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalysesByDatasourceAndTable(String datasource,
                                                                String table) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysis.class.getSimpleName())
      .whereClauses("datasource = ?", "table = ?")
      .order("desc")
      .build();

    return orientDbService.list(OpalAnalysis.class, query, datasource, table);
  }

  @Override
  public void save(OpalAnalysis analysis) throws AnalysisAlreadyExistsException {
    OpalAnalysis existingAnalysis = getAnalysis(analysis.getDatasource(), analysis.getTable(), analysis.getName());
    if (existingAnalysis == null) {
      orientDbService.save(analysis, analysis);
    } else {
      throw new AnalysisAlreadyExistsException(analysis.getName());
    }
  }

  @Override
  public void delete(OpalAnalysis analysis, boolean cascade) throws NoSuchAnalysisException {
    orientDbService.delete(analysis);

    String query = SimpleOrientDbQueryBuilder.newInstance()
        .table(OpalAnalysisResult.class.getSimpleName())
        .whereClauses("analysisName = ? ")
        .build();

    StreamSupport
        .stream(orientDbService.list(OpalAnalysisResult.class, query, analysis.getName()).spliterator(), false)
        .forEach(orientDbService::delete);

    deleteAnalysisFiles(Paths.get(Analysis.ANALYSES_HOME.toString(), analysis.getDatasource(), analysis.getTable(), analysis.getName()));
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(OpalAnalysis.class);
  }

  @Override
  @PreDestroy
  public void stop() { }

  private void deleteAnalysisFiles(Path analysisDir) {
    try {
      DefaultOpalFileSystem.deleteDirectoriesAndFilesInPath(analysisDir);
    } catch (IOException e) {
      logger.warn("Unable to delete analysis files at \"{}\"", analysisDir.toString());
    }
  }
}
