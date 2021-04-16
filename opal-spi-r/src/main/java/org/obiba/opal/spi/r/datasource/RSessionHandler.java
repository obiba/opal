/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource;

import org.obiba.opal.spi.r.ROperationTemplate;

/**
 * Wrapper of a R session to facilitate handling.
 */
public interface RSessionHandler {

  /**
   * Get the R session for executing operations.
   *
   */
  ROperationTemplate getSession();

  /**
   * To be called on datasource dispose in order to clean up the R session.
   */
  void onDispose();

}
