/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

/**
 * Upload R file and source it.
 */
public class SourceROperation extends AbstractROperation {

  private final String utilsScript;

  public SourceROperation(String utilsScript) {
    this.utilsScript = utilsScript;
  }

  @Override
  public void doWithConnection() {
    try (InputStream is = new ClassPathResource(utilsScript).getInputStream();) {
      writeFile(utilsScript, is);
      eval(String.format("base::source('%s')", utilsScript));
    } catch (Exception e) {
      throw new RRuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return String.format("%s <- source(%s)", utilsScript);
  }

}
