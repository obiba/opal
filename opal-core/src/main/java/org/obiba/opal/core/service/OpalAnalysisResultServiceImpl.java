/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.tools.SimpleOrientDbQueryBuilder;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.spi.analysis.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OpalAnalysisResultServiceImpl implements OpalAnalysisResultService {

  private static final Logger logger = LoggerFactory.getLogger(OpalAnalysisResultServiceImpl.class);

  private final OrientDbService orientDbService;

  @Autowired
  public OpalAnalysisResultServiceImpl(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  @Override
  public OpalAnalysisResult getAnalysisResult(String analysisName, String resultId) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysisResult.class.getSimpleName())
      .whereClauses("analysisName = ?", "id = ?")
      .build();

    return orientDbService.uniqueResult(OpalAnalysisResult.class, query, analysisName, resultId);
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(boolean lastResult) {
    SimpleOrientDbQueryBuilder builder = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysisResult.class.getSimpleName())
      .order("desc");

    if (lastResult) {
      builder.limit(1);
    }

    return orientDbService.list(OpalAnalysisResult.class, builder.build());
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(String datasource, String table, String analysisName, boolean lastResult)
      throws NoSuchAnalysisException {
    SimpleOrientDbQueryBuilder builder = SimpleOrientDbQueryBuilder.newInstance()
      .table(OpalAnalysisResult.class.getSimpleName())
      .whereClauses("datasource = ?", "table = ?", "analysisName = ?")
      .order("desc");

    if (lastResult) {
      builder.limit(1);
    }

    return orientDbService.list(OpalAnalysisResult.class, builder.build(), datasource, table, analysisName);
  }

  @Override
  public void save(OpalAnalysisResult analysisResult) throws ConstraintViolationException {
    orientDbService.save(analysisResult, analysisResult);
  }

  @Override
  public void delete(OpalAnalysisResult analysisResult) throws NoSuchAnalysisResultException {
    orientDbService.delete(analysisResult);

    deleteAnalysisResultFiles(Paths.get(Analysis.ANALYSES_HOME.toString(),analysisResult.getDatasource(), analysisResult.getTable(), analysisResult.getAnalysisName(), "results", analysisResult.getId()));
  }

  @Override
  public void start() {
    orientDbService.createUniqueIndex(OpalAnalysisResult.class);
  }

  @Override
  public void stop() {
  }

  private void deleteAnalysisResultFiles(Path analysisResultDir) {
    try {
      DefaultOpalFileSystem.deleteDirectoriesAndFilesInPath(analysisResultDir);
    } catch (IOException e) {
      logger.warn("Unable to delete analysis files at \"{}\"", analysisResultDir.toString());
    }
  }
}
