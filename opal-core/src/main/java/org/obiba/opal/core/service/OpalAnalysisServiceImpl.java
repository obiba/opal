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

import com.google.common.eventbus.Subscribe;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.repository.OpalAnalysisRepository;
import org.obiba.opal.core.repository.OpalAnalysisResultRepository;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.event.DatasourceDeletedEvent;
import org.obiba.opal.core.event.ValueTableDeletedEvent;
import org.obiba.opal.core.event.ValueTableRenamedEvent;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.spi.analysis.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.StreamSupport;

@Component
public class OpalAnalysisServiceImpl implements OpalAnalysisService {

  private static final Logger logger = LoggerFactory.getLogger(OpalAnalysisServiceImpl.class);

  private final OpalAnalysisRepository opalAnalysisRepository;

  private final OpalAnalysisResultRepository opalAnalysisResultRepository;

  @Autowired
  public OpalAnalysisServiceImpl(OpalAnalysisRepository opalAnalysisRepository,
                                 OpalAnalysisResultRepository opalAnalysisResultRepository) {
    this.opalAnalysisRepository = opalAnalysisRepository;
    this.opalAnalysisResultRepository = opalAnalysisResultRepository;
  }

  @Override
  public OpalAnalysis getAnalysis(String datasource, String table, String name) {
    return opalAnalysisRepository.findByDatasourceAndTableAndName(datasource, table, name).orElse(null);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalyses() {
    return opalAnalysisRepository.findAll();
  }

  @Override
  public Iterable<OpalAnalysis> getAnalysesByDatasource(String datasource) {
    return opalAnalysisRepository.findByDatasourceOrderByCreatedDesc(datasource);
  }

  @Override
  public Iterable<OpalAnalysis> getAnalysesByDatasourceAndTable(String datasource,
                                                                String table) {
    return opalAnalysisRepository.findByDatasourceAndTableOrderByCreatedDesc(datasource, table);
  }

  @Override
  public void save(OpalAnalysis analysis) throws AnalysisAlreadyExistsException {
    OpalAnalysis existingAnalysis = getAnalysis(analysis.getDatasource(), analysis.getTable(), analysis.getName());
    if (existingAnalysis == null) {
      opalAnalysisRepository.upsert(analysis);
    } else {
      throw new AnalysisAlreadyExistsException(analysis.getName());
    }
  }

  @Override
  public void delete(OpalAnalysis analysis) throws NoSuchAnalysisException {
    opalAnalysisRepository.deleteByKey(analysis);
    opalAnalysisResultRepository.deleteAll(opalAnalysisResultRepository.findByAnalysisName(analysis.getName()));

    deleteAnalysisFiles(Paths.get(Analysis.ANALYSES_HOME.toString(), analysis.getDatasource(), analysis.getTable(), analysis.getName()));
  }

  @Override
  public void deleteAnalyses(String datasource) {
    getAnalysesByDatasource(datasource).forEach(this::delete);
  }

  @Override
  public void deleteAnalyses(String datasource, String table) {
    getAnalysesByDatasourceAndTable(datasource, table).forEach(this::delete);
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() { }

  private void deleteAnalysisFiles(Path analysisDir) {
    try {
      DefaultOpalFileSystem.deleteDirectoriesAndFilesInPath(analysisDir);
    } catch (NoSuchFileException e) {
      // ignore
    } catch (IOException e) {
      logger.warn("Unable to delete analysis files at \"{}\"", analysisDir.toString());
    }
  }

  @Subscribe
  public void onValueTableDeleted(ValueTableDeletedEvent event) {
    String datasourceName = event.getValueTable().getDatasource().getName();
    String valueTableName = event.getValueTable().getName();
    deleteAnalyses(datasourceName, valueTableName);

    try {
      DefaultOpalFileSystem.deleteDirectoriesAndFilesInPath(
          Paths.get(Analysis.ANALYSES_HOME.toAbsolutePath().toString(), datasourceName, valueTableName)
      );
    } catch (NoSuchFileException e) {
      // ignore
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  @Subscribe
  public void onValueTableRenamed(ValueTableRenamedEvent event) {
    // TODO
  }

  @Subscribe
  public void onDatasourceDeleted(DatasourceDeletedEvent event) {
    String datasourceName = event.getDatasource().getName();
    deleteAnalyses(datasourceName);

    try {
      DefaultOpalFileSystem.deleteDirectoriesAndFilesInPath(
        Paths.get(Analysis.ANALYSES_HOME.toAbsolutePath().toString(), datasourceName)
      );
    } catch (NoSuchFileException e) {
      // ignore
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }

  }
}
