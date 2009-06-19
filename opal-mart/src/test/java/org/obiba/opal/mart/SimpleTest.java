package org.obiba.opal.mart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.test.AbstractJobTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "simple-job-context.xml" })
public class SimpleTest extends AbstractJobTests {

  @Test
  public void doIt() throws Exception {
    launchJob();
  }

}
