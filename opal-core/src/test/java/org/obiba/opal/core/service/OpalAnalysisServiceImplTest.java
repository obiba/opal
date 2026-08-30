/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.domain.OpalAnalysisResultItem;
import org.obiba.opal.core.repository.OpalAnalysisRepository;
import org.obiba.opal.core.repository.OpalAnalysisResultRepository;
import org.obiba.opal.spi.analysis.AnalysisResult;
import org.obiba.opal.spi.analysis.AnalysisStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * An analysis is identified by its datasource, its table and its name; two tables may perfectly well both carry an
 * analysis called "validation". Deleting one of them used to take the results of the other with it, silently, because
 * the results were deleted by name alone.
 */
@ContextConfiguration(classes = OpalAnalysisServiceImplTest.Config.class)
public class OpalAnalysisServiceImplTest extends AbstractConfigDbTest {

  static {
    // Analysis.ANALYSES_HOME is a static built from this property, and the service removes the analysis folder as it
    // deletes. Point it somewhere disposable before the class is loaded.
    if(System.getProperty("OPAL_HOME") == null) {
      try {
        File home = File.createTempFile("opal-analysis-test-", "");
        home.delete();
        home.mkdirs();
        home.deleteOnExit();
        System.setProperty("OPAL_HOME", home.getAbsolutePath());
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Autowired
  private OpalAnalysisService analysisService;

  @Autowired
  private OpalAnalysisRepository analysisRepository;

  @Autowired
  private OpalAnalysisResultRepository resultRepository;

  @Before
  public void clear() {
    resultRepository.deleteAll();
    analysisRepository.deleteAll();
  }

  @Test
  public void test_deleting_an_analysis_leaves_the_results_of_its_namesake_on_another_table() {
    analysisRepository.upsert(analysis("project", "patients", "validation"));
    analysisRepository.upsert(analysis("project", "samples", "validation"));
    resultRepository.upsert(result("r-patients", "validation", "project", "patients"));
    resultRepository.upsert(result("r-samples", "validation", "project", "samples"));

    analysisService.delete(analysisService.getAnalysis("project", "patients", "validation"));

    assertThat(idsOf(resultRepository.findAll())).containsOnly("r-samples");
    assertThat(analysisService.getAnalysis("project", "samples", "validation")).isNotNull();
  }

  @Test
  public void test_deleting_an_analysis_leaves_the_results_of_its_namesake_in_another_project() {
    analysisRepository.upsert(analysis("project", "patients", "validation"));
    analysisRepository.upsert(analysis("other", "patients", "validation"));
    resultRepository.upsert(result("r-project", "validation", "project", "patients"));
    resultRepository.upsert(result("r-other", "validation", "other", "patients"));

    analysisService.delete(analysisService.getAnalysis("project", "patients", "validation"));

    assertThat(idsOf(resultRepository.findAll())).containsOnly("r-other");
  }

  @Test
  public void test_deleting_the_analyses_of_a_table_leaves_the_other_tables_alone() {
    analysisRepository.upsert(analysis("project", "patients", "validation"));
    analysisRepository.upsert(analysis("project", "samples", "validation"));
    resultRepository.upsert(result("r-patients", "validation", "project", "patients"));
    resultRepository.upsert(result("r-samples", "validation", "project", "samples"));

    analysisService.deleteAnalyses("project", "patients");

    assertThat(idsOf(resultRepository.findAll())).containsOnly("r-samples");
  }

  @Test
  public void test_deleting_an_analysis_takes_its_own_results() {
    analysisRepository.upsert(analysis("project", "patients", "validation"));
    resultRepository.upsert(result("r-1", "validation", "project", "patients"));
    resultRepository.upsert(result("r-2", "validation", "project", "patients"));

    analysisService.delete(analysisService.getAnalysis("project", "patients", "validation"));

    assertThat(resultRepository.findAll()).isEmpty();
    assertThat(analysisService.getAnalysis("project", "patients", "validation")).isNull();
  }

  private List<String> idsOf(List<OpalAnalysisResult> results) {
    return results.stream().map(OpalAnalysisResult::getId).toList();
  }

  private OpalAnalysis analysis(String datasource, String table, String name) {
    return OpalAnalysis.Builder.create(null).datasource(datasource).table(table).name(name).build();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private OpalAnalysisResult result(String id, String analysisName, String datasource, String table) {
    return new OpalAnalysisResult(new StubResult(id, analysisName), datasource, table);
  }

  private static class StubResult implements AnalysisResult<OpalAnalysis, OpalAnalysisResultItem> {

    private final String id;

    private final String analysisName;

    private StubResult(String id, String analysisName) {
      this.id = id;
      this.analysisName = analysisName;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getAnalysisName() {
      return analysisName;
    }

    @Override
    public Date getStartDate() {
      return new Date();
    }

    @Override
    public Date getEndDate() {
      return new Date();
    }

    @Override
    public boolean hasResultItems() {
      return false;
    }

    @Override
    public List<OpalAnalysisResultItem> getResultItems() {
      return null;
    }

    @Override
    public AnalysisStatus getStatus() {
      return AnalysisStatus.PASSED;
    }

    @Override
    public String getMessage() {
      return null;
    }
  }

  @Configuration
  public static class Config extends AbstractConfigDbTestConfig {

    @Bean
    public OpalAnalysisService opalAnalysisService(OpalAnalysisRepository opalAnalysisRepository,
                                                   OpalAnalysisResultRepository opalAnalysisResultRepository) {
      return new OpalAnalysisServiceImpl(opalAnalysisRepository, opalAnalysisResultRepository);
    }
  }
}
