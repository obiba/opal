package org.obiba.opal.jdbcmart.batch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.obiba.opal.batch.StartDateIncrementer;
import org.obiba.opal.core.domain.data.Dataset;
import org.springframework.batch.item.database.HibernateCursorItemReader;

public class DatasetReader extends HibernateCursorItemReader<Dataset> {
  //
  // Constants
  //

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  //
  // Instance Variables
  //

  private Map<String, Object> jobParameters;

  //
  // Constructors
  //

  public DatasetReader() {
    jobParameters = new LinkedHashMap<String, Object>();
  }

  //
  // HibernateCursorItemReader Methods
  //

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Date startDate = (Date) jobParameters.get(StartDateIncrementer.START_DATE_KEY);
    if(startDate != null) {
      super.setQueryString("from Dataset d where d.creationDate >= '" + DATE_FORMAT.format(startDate) + "'");
    } else {
      super.setQueryString("from Dataset d");
    }
  }

  @Override
  protected void doOpen() throws Exception {
    super.doOpen();
  }

  //
  // Methods
  //

  public void setJobParameters(Map<String, Object> jobParameters) {
    if(jobParameters != null) {
      this.jobParameters.putAll(jobParameters);
    }
  }
}
