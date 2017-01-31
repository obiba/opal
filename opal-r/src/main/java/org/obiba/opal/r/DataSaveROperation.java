/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r;

import com.google.common.base.Strings;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPRaw;

import java.util.Base64;

/**
 * Save a R object (of class tibble) into a file in the R session.
 */
public class DataSaveROperation extends AbstractROperation {

  private final String symbol;

  private final String destination;

  public DataSaveROperation(String symbol, String destination) {
    this.symbol = symbol;
    this.destination = destination;
  }

  @Override
  public void doWithConnection() {
    if(Strings.isNullOrEmpty(destination)) return;
    // extract destination file
    String writeCmd;
    if (destination.endsWith(".sas7bdat")) writeCmd = "write_sas";
    else if (destination.endsWith(".sav")) writeCmd = "write_sav";
    else if (destination.endsWith(".dat")) writeCmd = "write_dta";
    else return;
    String path = prepareDestinationInR();
    // make sure haven is available
    ensurePackage("haven");
    eval("library(haven)", false);
    ensurePackage("tibble");
    eval("library(tibble)", false);
    // ensure symbol refers to a tibble
    REXP isTibble = eval(String.format("is.tibble(%s)", symbol), false);
    if (isTibble.isLogical()) {
      REXPLogical isTibbleLogical = (REXPLogical) isTibble;
      if (isTibbleLogical.length() == 0 || !isTibbleLogical.isTRUE()[0]) throw new IllegalArgumentException(symbol + " is not a tibble.");
    } else {
      throw new IllegalArgumentException(symbol + "Cannot determine if " + symbol + " is a tibble.");
    }
    String cmd = String.format("%s(%s, '%s')", writeCmd, symbol, path);
    eval(cmd, false);
  }

  private String prepareDestinationInR() {
    String path = destination;
    if (path.startsWith("/")) path = destination.substring(1);
    if (path.contains("/")) {
      // make sure destination directory exists
      String rscript = String.format("base::dir.create('%s', showWarnings=FALSE, recursive=TRUE)", path.substring(0, path.lastIndexOf("/")));
      eval(rscript, false);
    }
    return path;
  }

  @Override
  public String toString() {
    return String.format("%s -> %s", symbol, destination);
  }
}
