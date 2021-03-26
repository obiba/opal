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

/**
 * A service for executing SQL queries on a project's tables.
 */
public interface SQLService extends SystemService {

  /**
   * Execute a SQL query in the context of a datasource.
   *
   * @param datasource
   * @param query
   * @param idName
   * @return
   */
  String execute(String datasource, String query, String idName);

}
