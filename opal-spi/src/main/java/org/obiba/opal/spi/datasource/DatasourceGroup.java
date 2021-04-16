/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.datasource;

/**
 * Enumerated groups of a datasource.
 */
public enum DatasourceGroup {

  FILE,      // file based datasource, usually requires a path in opal file system
  SERVER,    // server based datasource, usually requires a URL and credentials

}
