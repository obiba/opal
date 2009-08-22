package org.obiba.opal.jdbcmart.batch;

import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.sesame.report.DataItemSet;

public interface IColumnNamingStrategy {

  public void prepare(DataItemSet dataItemSet);

  public String getColumnName(DataItem item);

}
