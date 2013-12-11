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

import org.junit.Test;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.git.commands.OpalGitCommitLogCommand;
import org.obiba.opal.core.vcs.git.support.TestOpalGitVersionControlSystem;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class OpalGitCommitLogCommandTest {

  private static final String COMMIT_ID = "448b81ed146cc76751c3b10b89e80cc99da63427";

  private static final String BAD_COMMIT_ID = "DeadBeefDeadBeefDeadBeefDeadBeefDeadBeef";

  private static final String DATASOURCE_NAME = "opal-data2";

  private static final TestOpalGitVersionControlSystem vcs = new TestOpalGitVersionControlSystem();

  @Test(expected = OpalGitException.class)
  public void testCreateCommandWithNullRepository() {
    new OpalGitCommitLogCommand.Builder(null, "", COMMIT_ID).build();
  }

  @Test
  public void testCommitInfoRetrievalWithValidCommitId() {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME),
        "TestView", COMMIT_ID).build();
    CommitInfo commitInfo = command.execute();
    assertThat(commitInfo, not(is(nullValue())));
    assertThat(commitInfo.getCommitId(), is(COMMIT_ID));
    assertThat(commitInfo.getAuthor(), is("administrator"));
    assertThat(commitInfo.getComment(), is("Update TestView"));
  }

  @Test(expected = OpalGitException.class)
  public void testCommitInfoRetrievalWithInvalidCommitId() {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME),
        "TestView", BAD_COMMIT_ID).build();
    command.execute();
  }

  @Test
  public void testCommitInfoRetrievalWithValidVariable() {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME),
        "TestView/TOTO_VAR.js", COMMIT_ID).build();
    CommitInfo commitInfo = command.execute();
    assertThat(commitInfo, not(is(nullValue())));
    assertThat(commitInfo.getCommitId(), is(COMMIT_ID));
    assertThat(commitInfo.getAuthor(), is("administrator"));
    assertThat(commitInfo.getComment(), is("Update TestView"));
  }

  @Test(expected = OpalGitException.class)
  public void testCommitInfoRetrievalWithInvalidVariable() {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME),
        "TestView/BAD_VAR.js", COMMIT_ID).build();
    command.execute();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommitInfoRetrievalWithNoPath() {
    new OpalGitCommitLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME), null, COMMIT_ID).build().execute();
  }

}

