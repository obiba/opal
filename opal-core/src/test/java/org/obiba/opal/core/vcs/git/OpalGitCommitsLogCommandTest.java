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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.git.commands.OpalGitCommitsLogCommand;
import org.obiba.opal.core.vcs.git.support.TestOpalGitVersionControlSystem;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OpalGitCommitsLogCommandTest {

  private static final String DATASOURCE_NAME = "opal-data2";

  private AtomicReference<TestOpalGitVersionControlSystem> vcs = new AtomicReference<TestOpalGitVersionControlSystem>();

  private AtomicReference<OpalGitCommitsLogCommand> command = new AtomicReference<OpalGitCommitsLogCommand>();

  @Before
  public void setup() {
    vcs.set(new TestOpalGitVersionControlSystem());
    command.set(new OpalGitCommitsLogCommand(vcs.get().getRepository(DATASOURCE_NAME), DATASOURCE_NAME));
  }

  @Test(expected = OpalGitException.class)
  public void testCreateCommandWithNullRepository() {
    new OpalGitCommitsLogCommand(null);
  }

  @Test
  public void testCommitsInfoRetrievalWitValidViewPath() {
    try {
      List<CommitInfo> commitInfos = command.get().addPath("TestView").execute();
      assertThat(commitInfos, not(is(nullValue())));
      assertTrue(commitInfos.size() > 0);
      assertThat(commitInfos.get(0).getCommitId(), not(is(nullValue())));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWitInvalidViewPath() {
    command.get().addPath("DUMMY").execute();
  }

  @Test
  public void testCommitsInfoRetrievalWitValidVariablePath() {
    try {
      List<CommitInfo> commitInfos = command.get().addPath("TestView/TOTO_VAR.js").execute();
      assertThat(commitInfos, not(is(nullValue())));
      assertThat(commitInfos.size(), not(is(0)));
      assertThat(commitInfos.get(0).getCommitId(), not(is(nullValue())));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWitInvalidVariablePath() {
    command.get().addPath("TestView/BAD_VAR.js").execute();
  }

  @Test
  public void testCommitsInfoRetrievalForWholeRepository() {
    try {
      List<CommitInfo> commitInfos = command.get().execute();
      assertThat(commitInfos, not(is(nullValue())));
      assertTrue(commitInfos.size() > 0);
    } catch(Exception e) {
      Assert.fail();
    }
  }

  /**
   * This is test is equivalent to test 'testCommitsInfoRetrievalForWholeRepository()'
   */
  @Test
  public void testCommitsInfoRetrievalWithEmptyPath() {
    try {
      List<CommitInfo> commitInfos = command.get().addPath("").execute();
      assertThat(commitInfos, not(is(nullValue())));
      assertTrue(commitInfos.size() > 0);
    } catch(Exception e) {
      Assert.fail();
    }
  }

}

