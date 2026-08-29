/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.repository;

import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpalAnalysisResultRepository extends JpaRepository<OpalAnalysisResult, String> {

  Optional<OpalAnalysisResult> findByAnalysisNameAndId(String analysisName, String id);

  List<OpalAnalysisResult> findByAnalysisName(String analysisName);

  List<OpalAnalysisResult> findAllByOrderByCreatedDesc();

  Optional<OpalAnalysisResult> findFirstByOrderByCreatedDesc();

  List<OpalAnalysisResult> findByDatasourceAndTableAndAnalysisNameOrderByCreatedDesc(
      String datasource, String table, String analysisName);

  Optional<OpalAnalysisResult> findFirstByDatasourceAndTableAndAnalysisNameOrderByCreatedDesc(
      String datasource, String table, String analysisName);

  /**
   * The result carries the identifier assigned by the analysis that produced it, so it is already the key.
   */
  default OpalAnalysisResult upsert(OpalAnalysisResult result) {
    return save(result);
  }
}
