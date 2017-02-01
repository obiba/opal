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
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;

import java.util.List;

/**
 * Read a file to a R object (of class tibble) in the R session.
 */
public class DataReadROperation extends AbstractROperation {

  private enum ReadCmd {

    SAS("haven::read_sas","sas7bdat"),

    DTA("haven::read_dta","dta"),

    SAV("haven::read_sav","sav");

    protected final String command;

    protected final List<String> extensions;

    ReadCmd(String cmd, String... extensions) {
      this.command = cmd;
      this.extensions = Lists.newArrayList(extensions);
    }

    public String getCommand(String path) {
      return String.format("%s('%s')", command, path);
    }

    public static ReadCmd forPath(String path) {
      for (ReadCmd cmd : ReadCmd.values()) {
        for (String extension : cmd.extensions) {
          if (path.endsWith("." + extension)) return cmd;
        }
      }
      throw new IllegalArgumentException("Cannot find a R reader for file: " + path);
    }
  }

  private final String symbol;

  private final String source;

  public DataReadROperation(String symbol, String source) {
    this.symbol = symbol;
    this.source = source;
  }

  @Override
  public void doWithConnection() {
    if(Strings.isNullOrEmpty(source)) return;
    // extract destination file
    ReadCmd readCmd = ReadCmd.forPath(source);
    // make sure haven is available
    ensurePackage("haven");
    eval("library(haven)", false);
    ensurePackage("tibble");
    eval("library(tibble)", false);
    eval(String.format("base::assign('%s', %s)", symbol, readCmd.getCommand(source)), false);
  }

  @Override
  public String toString() {
    return String.format("%s <- %s", symbol, source);
  }
}
