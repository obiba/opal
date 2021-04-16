/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.analysis;

import java.util.NoSuchElementException;

public class NoSuchAnalysisTemplateException extends NoSuchElementException {

  private final String name;

  public NoSuchAnalysisTemplateException(String name) {
    this.name = name;
  }

  /**
   * The name of the {@link AnalysisTemplate} that could not be found.
   *
   * @return
   */
  public String getName() {
    return name;
  }
}
