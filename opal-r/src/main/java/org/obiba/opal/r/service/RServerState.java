/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import java.util.List;

public interface RServerState {

  String getName();

  String getCluster();

  String getVersion();

  boolean isRunning();

  List<String> getTags();

  int getRSessionsCount();

  int getBusyRSessionsCount();

  int getSystemCores();

  int getSystemFreeMemory();
}
