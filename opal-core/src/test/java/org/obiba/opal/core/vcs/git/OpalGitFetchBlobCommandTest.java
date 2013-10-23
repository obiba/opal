/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.vcs.git;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.git.commands.OpalGitFetchBlobCommand;
import org.obiba.opal.core.vcs.git.support.TestOpalGitVersionControlSystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OpalGitFetchBlobCommandTest {

  private static final String COMMIT_ID = "95f47bac573f3c4da5c0731c263eb0638e45e992";

  private static final String BAD_COMMIT_ID = "DeadBeefDeadBeefDeadBeefDeadBeefDeadBeef";

  private static final String DATASOURCE_NAME = "opal-data2";

  private static final TestOpalGitVersionControlSystem vcs = new TestOpalGitVersionControlSystem();

  @Test(expected = OpalGitException.class)
  public void testBlobRetrievalWithInvalidViewPath() {
    new OpalGitFetchBlobCommand.Builder(vcs.getRepository(DATASOURCE_NAME), "Zozo/PLACE_NAME.js", COMMIT_ID)
        .addDatasourceName(DATASOURCE_NAME).build().execute();
  }

  @Test(expected = OpalGitException.class)
  public void testBlobRetrievalWithInvalidCommit() {
    new OpalGitFetchBlobCommand.Builder(vcs.getRepository(DATASOURCE_NAME), "TestView/PLACE_NAME.js", BAD_COMMIT_ID)
        .addDatasourceName(DATASOURCE_NAME).build().execute();
  }

  @Test
  public void testBlobRetrievalWithValidVariablePath() {
    try {
      OpalGitFetchBlobCommand command = new OpalGitFetchBlobCommand.Builder(vcs.getRepository(DATASOURCE_NAME),
          "TestView/PLACE_NAME.js", COMMIT_ID).addDatasourceName(DATASOURCE_NAME).build();
      String blob = command.execute();
      assertThat(blob, equalTo("$('PLACE_NAME')"));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test(expected = OpalGitException.class)
  public void testBlobRetrievalWithFolderPath() {
    OpalGitFetchBlobCommand command = new OpalGitFetchBlobCommand.Builder(vcs.getRepository(DATASOURCE_NAME),
        "TestView", COMMIT_ID).addDatasourceName(DATASOURCE_NAME).build();
    command.execute();
  }
}

