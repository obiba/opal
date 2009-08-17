package org.obiba.opal.jdbcmart.batch;

import java.util.Date;

import org.obiba.opal.core.domain.data.Dataset;
import org.springframework.batch.item.database.HibernateCursorItemReader;

public class DatasetReader extends HibernateCursorItemReader<Dataset> {

  private Date startDate;

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  @Override
  protected void doOpen() throws Exception {
    super.doOpen();
  }

}
