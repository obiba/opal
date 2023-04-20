/*
 * Copyright (c) 2023 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.datashield.cfg;

public interface RestrictedROperation {

  /**
   * Get the rewritten R script, compliant with restrictions.
   *
   * @return
   */
  String restrictedScript();
}
