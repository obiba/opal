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

import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.rock.RockService;

import java.util.List;

/**
 * Management of the cluster inner R services.
 */
public interface RServerClusterService extends RServerService {

  /**
   * Check for an empty cluster of R servers.
   *
   * @return
   */
  boolean isEmpty();

  List<RServerService> getRServerServices();

  RServerService getRServerService(String sname);

  void addRServerService(RServerService rServerService);

}
