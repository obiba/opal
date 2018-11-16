package org.obiba.opal.spi.analysis;

public enum AnalysisReportType {
  HTML("html_document"),
  PDF("pdf_document");

  private final String output;

  AnalysisReportType(String output) {
    this.output = output;
  }

  public String getOutput() {
    return output;
  }

  /**
   * @param type
   * @return Returns proper type or HTML as default
   */
  public static AnalysisReportType safeValueOf(String type) {
    try {
      return valueOf(AnalysisReportType.class, type.replaceAll("'", "").toUpperCase());
    } catch (IllegalArgumentException | NullPointerException ignored) {
      return HTML;
    }
  }
}
