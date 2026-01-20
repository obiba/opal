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

import com.google.common.collect.Lists;
import org.obiba.opal.core.domain.OpalAnalysisResult;
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
    return orientDbService.uniqueResult(OpalAnalysisResult.class,
        "SELECT * FROM OpalAnalysisResult WHERE analysisName = ? AND id = ?", analysisName, resultId);
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(boolean lastResult) {
    Iterable<OpalAnalysisResult> results = orientDbService.list(OpalAnalysisResult.class);
    if (lastResult) {
      return Lists.newArrayList(results).stream()
          .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
          .limit(1)
          .toList();
    }
    return results;
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(String datasource, String table, String analysisName, boolean lastResult)
      throws NoSuchAnalysisException {
    Iterable<OpalAnalysisResult> results = orientDbService.list(OpalAnalysisResult.class,
        "SELECT * FROM OpalAnalysisResult WHERE datasource = ? AND table = ? AND analysisName = ?",
        datasource, table, analysisName);
    if (lastResult) {
      return Lists.newArrayList(results).stream()
          .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
          .limit(1)
          .toList();
    }
    return results;
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
