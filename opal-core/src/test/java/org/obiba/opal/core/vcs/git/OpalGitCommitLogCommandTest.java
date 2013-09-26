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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.git.commands.OpalGitCommitLogCommand;
import org.obiba.opal.core.vcs.git.commands.OpalGitCommitsLogCommand;
import org.obiba.opal.core.vcs.git.support.TestOpalGitVersionControlSystem;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class OpalGitCommitLogCommandTest {

  private static final String COMMIT_ID = "448b81ed146cc76751c3b10b89e80cc99da63427";

  private static final String BAD_COMMIT_ID = "DeadBeefDeadBeefDeadBeefDeadBeefDeadBeef";

  private static final String DATASOURCE_NAME = "opal-data2";

  private AtomicReference<TestOpalGitVersionControlSystem> vcs = new AtomicReference<TestOpalGitVersionControlSystem>();

  private AtomicReference<OpalGitCommitLogCommand> command = new AtomicReference<OpalGitCommitLogCommand>();

  @Before
  public void setup() {
    vcs.set(new TestOpalGitVersionControlSystem());
    command.set(new OpalGitCommitLogCommand(vcs.get().getRepository(DATASOURCE_NAME), DATASOURCE_NAME));
  }

  @Test(expected = OpalGitException.class)
  public void testCreateCommandWithNullRepository() {
    new OpalGitCommitsLogCommand(null);
  }

  @Test
  public void testCommitsInfoRetrievalWithValidCommitId() {
    try {
      CommitInfo commitInfo = command.get().addPath("TestView").addCommitId(COMMIT_ID).execute();
      assertThat(commitInfo, not(is(nullValue())));
      assertThat(commitInfo.getCommitId(), is(COMMIT_ID));
      assertThat(commitInfo.getAuthor(), is("administrator"));
      assertThat(commitInfo.getComment(), is("Update TestView"));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWithInvalidCommitId() {
    command.get().addPath("TestView").addCommitId(BAD_COMMIT_ID).execute();
  }

  @Test
  public void testCommitsInfoRetrievalWithValidVariable() {
    try {
      CommitInfo commitInfo = command.get().addPath("TestView/TOTO_VAR.js").addCommitId(COMMIT_ID).execute();
      assertThat(commitInfo, not(is(nullValue())));
      assertThat(commitInfo.getCommitId(), is(COMMIT_ID));
      assertThat(commitInfo.getAuthor(), is("administrator"));
      assertThat(commitInfo.getComment(), is("Update TestView"));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWithInvalidVariable() {
    command.get().addPath("TestView").addPath("TestView/BAD_VAR.js").addCommitId(COMMIT_ID).execute();
  }

}

