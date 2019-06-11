package org.obiba.opal.r.datasource;

import com.google.common.base.Strings;
import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.rosuda.REngine.Rserve.RConnection;

import javax.validation.constraints.NotNull;

public class RAssignDatasourceFactory extends AbstractDatasourceFactory {

  private final String symbol;

  private final RConnection rConnection;

  private String idColumnName;

  private RSessionHandler rSessionHandler;

  private boolean withMissings = true;

  public RAssignDatasourceFactory(String name, String symbol, RConnection rConnection) {
    setName(name);
    this.symbol = symbol;
    this.rConnection = rConnection;
  }

  public void setIdColumnName(String idColumnName) {
    this.idColumnName = idColumnName;
  }

  public void setWithMissings(boolean withMissings) {
    this.withMissings = withMissings;
  }

  public void setrSessionHandler(RSessionHandler rSessionHandler) {
    this.rSessionHandler = rSessionHandler;
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    RAssignDatasource ds;
    if (rSessionHandler != null)
      ds = new RAssignDatasource(getName(), symbol, rSessionHandler);
    else
      ds = new RAssignDatasource(getName(), symbol, rConnection);
    ds.setMultilines(true);
    ds.setEntityIdName(Strings.isNullOrEmpty(idColumnName) ? "id" : idColumnName);
    ds.setWithMissings(withMissings);
    return ds;
  }

}
