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
import com.google.common.collect.Lists;
import org.obiba.magma.ValueType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPRaw;

import java.util.Base64;
import java.util.List;

/**
 * Save a R object (of class tibble) into a file in the R session.
 */
public class DataSaveROperation extends AbstractROperation {

  private enum WriteCmd {

    SAS("haven::write_sas","sas7bdat"),

    DTA("haven::write_dta","dta"),

    SAV("haven::write_sav","sav"),

    CSV("utils::write.table","csv","tsv") {
      @Override
      public String getCommand(String symbol, String path) {
        if (path.endsWith(".tsv"))
          return String.format("%s(`%s`, file='%s', row.names=FALSE, sep='\\t')", command, symbol, path);
        else
          return String.format("%s(`%s`, file='%s', row.names=FALSE, sep=',')", command, symbol, path);
      }
    };

    protected final String command;

    protected final List<String> extensions;

    WriteCmd(String cmd, String... extensions) {
      this.command = cmd;
      this.extensions = Lists.newArrayList(extensions);
    }

    public String getCommand(String symbol, String path) {
      return String.format("%s(`%s`, '%s')", command, symbol, path);
    }

    public static WriteCmd forPath(String path) {
      for (WriteCmd cmd : WriteCmd.values()) {
        for (String extension : cmd.extensions) {
          if (path.endsWith("." + extension)) return cmd;
        }
      }
      throw new IllegalArgumentException("Cannot find a R writer for file: " + path);
    }
  }

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
    WriteCmd writeCmd = WriteCmd.forPath(destination);
    String path = prepareDestinationInR();
    // make sure haven is available
    ensurePackage("haven");
    eval("library(haven)", false);
    ensurePackage("tibble");
    eval("library(tibble)", false);
    // ensure symbol refers to a tibble
    REXP isTibble = eval(String.format("is.tibble(`%s`)", symbol), false);
    if (isTibble.isLogical()) {
      REXPLogical isTibbleLogical = (REXPLogical) isTibble;
      if (isTibbleLogical.length() == 0 || !isTibbleLogical.isTRUE()[0]) throw new IllegalArgumentException(symbol + " is not a tibble.");
    } else {
      throw new IllegalArgumentException("Cannot determine if " + symbol + " is a tibble.");
    }
    eval(writeCmd.getCommand(symbol, path), false);
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
