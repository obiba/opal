package org.obiba.opal.jdbcmart.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class CreateSchemaTasklet implements Tasklet {
  //
  // Instance Variables
  //
  
  private DataSource dataSource;
  
  //
  // Tasklet Methods
  //
  
  public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
    JdbcTemplate template = new JdbcTemplate(dataSource);
    
    // To be removed. For testing the connection.
    template.execute("create table opalreport (item1 varchar(255), item2 varchar(255));");
    
    return RepeatStatus.FINISHED;
  }

  //
  // Methods
  //
  
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
