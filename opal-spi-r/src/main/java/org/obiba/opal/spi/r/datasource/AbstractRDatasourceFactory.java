package org.obiba.opal.spi.r.datasource;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.opal.spi.r.FileWriteROperation;
import org.obiba.opal.spi.r.ROperation;

import java.io.File;

public abstract class AbstractRDatasourceFactory extends AbstractDatasourceFactory implements RDatasourceFactory  {

  private RSessionHandler sessionHandler;

  @Override
  public void setRSessionHandler(RSessionHandler sessionHandler) {
    this.sessionHandler = sessionHandler;
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

  /**
   * Make the symbol that will refer to the data file.
   *
   * @param file
   * @return
   */
  protected String getSymbol(File file) {
    String symbol = file.getName().replaceAll(" ", "_");
    int suffix = symbol.lastIndexOf(".");
    if (suffix>0) {
      symbol = symbol.substring(0, suffix);
    }
    return symbol;
  }
}
