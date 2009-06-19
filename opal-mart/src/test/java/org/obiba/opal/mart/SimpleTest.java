package org.obiba.opal.mart;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.AbstractJobTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "simple-job-context.xml" })
public class SimpleTest extends AbstractJobTests {

  @Test
  public void doIt() throws Exception {
    File result = new File("result.csv");
    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString("query", "");
    builder.addString("output.filename", "file:"+result.getAbsolutePath());
    launchJob(builder.toJobParameters());
  }

}
