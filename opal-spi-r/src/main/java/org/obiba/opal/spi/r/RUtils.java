package org.obiba.opal.spi.r;

import java.io.File;

public class RUtils {

  /**
   * Make the R symbol corresponding to the provided string.
   *
   * @param name
   * @return
   */
  public static String getSymbol(String name) {
    String symbol = name.replaceAll(" ", "_");
    int suffix = symbol.lastIndexOf(".");
    if (suffix>0) {
      symbol = symbol.substring(0, suffix);
    }
    return symbol;
  }

  /**
   * Make the R symbol that will refer to the data file.
   *
   * @param file
   * @return
   */
  public static String getSymbol(File file) {
    return getSymbol(file.getName());
  }
}
