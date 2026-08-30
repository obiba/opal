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
import org.obiba.opal.core.repository.OpalAnalysisResultRepository;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.spi.analysis.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Collections;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OpalAnalysisResultServiceImpl implements OpalAnalysisResultService {

  private static final Logger logger = LoggerFactory.getLogger(OpalAnalysisResultServiceImpl.class);

  private final OpalAnalysisResultRepository opalAnalysisResultRepository;

  @Autowired
  public OpalAnalysisResultServiceImpl(OpalAnalysisResultRepository opalAnalysisResultRepository) {
    this.opalAnalysisResultRepository = opalAnalysisResultRepository;
  }

  @Override
  public OpalAnalysisResult getAnalysisResult(String analysisName, String resultId) {
    return opalAnalysisResultRepository.findByAnalysisNameAndId(analysisName, resultId).orElse(null);
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(boolean lastResult) {
    // The OrientDB query this replaces was built with no where clause, which made it "WHERE null" and returned
    // nothing. Nothing calls this method, so the behaviour it was reaching for is implemented rather than the one it
    // had.
    if (lastResult) {
      return opalAnalysisResultRepository.findFirstByOrderByCreatedDesc()
          .map(Collections::singletonList).orElse(Collections.emptyList());
    }
    return opalAnalysisResultRepository.findAllByOrderByCreatedDesc();
  }

  @Override
  public Iterable<OpalAnalysisResult> getAnalysisResults(String datasource, String table, String analysisName, boolean lastResult)
      throws NoSuchAnalysisException {
    if (lastResult) {
      return opalAnalysisResultRepository
          .findFirstByDatasourceAndTableAndAnalysisNameOrderByCreatedDesc(datasource, table, analysisName)
          .map(Collections::singletonList).orElse(Collections.emptyList());
    }
    return opalAnalysisResultRepository
        .findByDatasourceAndTableAndAnalysisNameOrderByCreatedDesc(datasource, table, analysisName);
  }

  @Override
  public void save(OpalAnalysisResult analysisResult) throws ConstraintViolationException {
    opalAnalysisResultRepository.upsert(analysisResult);
  }

  @Override
  public void delete(OpalAnalysisResult analysisResult) throws NoSuchAnalysisResultException {
    opalAnalysisResultRepository.delete(analysisResult);

    deleteAnalysisResultFiles(Paths.get(Analysis.ANALYSES_HOME.toString(),analysisResult.getDatasource(), analysisResult.getTable(), analysisResult.getAnalysisName(), "results", analysisResult.getId()));
  }

  @Override
  public void start() {

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
