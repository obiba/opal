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

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.opal.spi.r.FileWriteROperation;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.datasource.magma.RSymbolWriter;

import java.io.File;

public abstract class AbstractRDatasourceFactory extends AbstractDatasourceFactory implements RDatasourceFactory  {

  private RSessionHandler sessionHandler;

  @Override
  public void setRSessionHandler(RSessionHandler sessionHandler) {
    this.sessionHandler = sessionHandler;
    setName(sessionHandler.getSession().toString());
  }

  @Override
  public RSymbolWriter createSymbolWriter() {
    throw new NoSuchMethodError("R symbol writing is not supported");
  }

  protected RSessionHandler getRSessionHandler() {
    return sessionHandler;
  }

  /**
   * Copy file into the R session workspace.
   *
   * @param file
   */
  protected void prepareFile(File file) {
    if (file != null && file.exists()) {
      // copy file(s) to R session
      execute(new FileWriteROperation(file.getName(), file));
    }
  }

  /**
   * Execute an operation in the R session.
   *
   * @param rop
   */
  protected void execute(ROperation rop) {
    sessionHandler.getSession().execute(rop);
  }

}
