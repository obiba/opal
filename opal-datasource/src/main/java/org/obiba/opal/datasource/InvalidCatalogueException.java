package org.obiba.opal.datasource;

public class InvalidCatalogueException extends RuntimeException {

  private static final long serialVersionUID = -2006089050503031975L;

  private String datasource;

  private String name;

  public InvalidCatalogueException(String datasource, String name) {
    super("Invalid catalogue " + datasource + ":" + name);
    this.datasource = datasource;
    this.name = name;
  }

  public String getDatasource() {
    return datasource;
  }

  public String getName() {
    return name;
  }
}
