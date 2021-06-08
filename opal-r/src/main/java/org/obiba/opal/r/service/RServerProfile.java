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

/**
 * Bridge between the requested profile and the R servers cluster.
 */
public interface RServerProfile {

  /**
   * Get the name of the profile (supposed to be unique).
   *
   * @return
   */
  String getName();

  /**
   * Get the cluster to which it applies.
   *
   * @return
   */
  String getCluster();
}
