package org.obiba.opal.core.service;

public class NoSuchDatasourceTableAnalysisException extends RuntimeException {

  private final String datasource;
  private final String table;
  private String analysisId;

  public String getDatasource() {
    return datasource;
  }

  public String getTable() {
    return table;
  }

  public String getAnalysisId() {
    return analysisId;
  }

  public NoSuchDatasourceTableAnalysisException(String datasource, String table, String analysisId) {
    super(String.format("No Analysis with id %s for project %s and table %s.", datasource, table, analysisId));
    this.datasource = datasource;
    this.table = table;
    this.analysisId = analysisId;
  }

}
