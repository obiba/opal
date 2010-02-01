package org.obiba.opal.jdbcmart.batch;

import java.util.List;

import org.obiba.opal.sesame.report.Report;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class DataItemSetReader implements ItemReader<Report> {
  //
  // Instance Variables
  //
  private List<Report> reports;

  private int index;

  //
  // Constructors
  //

  public DataItemSetReader() {
  }

  public void setReports(List<Report> reports) {
    this.reports = reports;
  }

  //
  // ItemReader Methods
  //

  public Report read() throws Exception, UnexpectedInputException, ParseException {
    return index < reports.size() ? reports.get(index++) : null;
  }

  public void close() throws ItemStreamException {

  }


  public void update(ExecutionContext executionContext) throws ItemStreamException {
  }

}
