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

import java.io.File;

public abstract class AbstractAnalysisService<T extends Analysis, U extends AnalysisResult> implements AnalysisService<T, U> {

  private AnalysisService.OpalFileSystemPathResolver pathResolver;

  protected File resolvePath(String virtualPath) {
    return pathResolver == null ? new File(virtualPath) : pathResolver.resolve(virtualPath);
  }

  /**
   * Sets an instance of an Opal file system path resolver.
   *
   * @param resolver
   */
  public void setOpalFileSystemPathResolver(AnalysisService.OpalFileSystemPathResolver resolver) {
    this.pathResolver = resolver;
  }
}
