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
