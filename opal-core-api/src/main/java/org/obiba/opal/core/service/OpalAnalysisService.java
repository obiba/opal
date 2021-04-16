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

import org.obiba.opal.core.domain.OpalAnalysis;

public interface OpalAnalysisService extends SystemService {

  OpalAnalysis getAnalysis(String datasource, String table, String name);

  Iterable<OpalAnalysis> getAnalyses();

  Iterable<OpalAnalysis> getAnalysesByDatasource(String datasource);

  Iterable<OpalAnalysis> getAnalysesByDatasourceAndTable(String datasource, String table);

  void save(OpalAnalysis analysis) throws AnalysisAlreadyExistsException;

  void delete(OpalAnalysis analysis) throws NoSuchAnalysisException;

  void deleteAnalyses(String datasource);

  void deleteAnalyses(String datasource, String table);
}
