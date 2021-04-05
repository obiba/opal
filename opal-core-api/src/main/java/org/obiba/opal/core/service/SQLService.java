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

import javax.annotation.Nullable;
import java.io.File;

/**
 * A service for executing SQL queries on a project's tables.
 */
public interface SQLService extends SystemService {

  String DEFAULT_ID_COLUMN = "_id";

  /**
   * Execute a SQL query in the context of a datasource and output result as a JSON object.
   *
   * @param datasource Datasource context, for table name resoltuion, can be null
   * @param query
   * @param idName
   * @return
   */
  File executeToJSON(@Nullable String datasource, String query, String idName);

  /**
   * Execute a SQL query in the context of a datasource and dump output to a temporary
   * CSV file.
   *
   * @param datasource Datasource context, for table name resoltuion, can be null
   * @param query
   * @param idName
   * @return
   */
  File executeToCSV(@Nullable String datasource, String query, String idName);

}
