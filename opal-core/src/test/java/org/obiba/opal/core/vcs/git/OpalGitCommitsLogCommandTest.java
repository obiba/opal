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

import org.junit.Test;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.git.commands.OpalGitCommitsLogCommand;
import org.obiba.opal.core.vcs.git.support.TestOpalGitVersionControlSystem;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OpalGitCommitsLogCommandTest {

  private static final String DATASOURCE_NAME = "opal-data2";

  private static final TestOpalGitVersionControlSystem vcs = new TestOpalGitVersionControlSystem();

  @Test(expected = OpalGitException.class)
  public void testCreateCommandWithNullRepository() {
    new OpalGitCommitsLogCommand.Builder(null).build();
  }

  @Test
  public void testCommitsInfoRetrievalWitValidViewPath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).addPath("TestView").build();
    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos, not(is(nullValue())));
    assertTrue(commitInfos.size() > 0);
    assertThat(commitInfos.get(0).getCommitId(), not(is(nullValue())));
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWitInvalidViewPath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).addPath("DEADBEAF").build();
    command.execute();
  }

  @Test
  public void testCommitsInfoRetrievalWitValidVariablePath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).addPath("TestView/TOTO_VAR.js").build();

    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos, not(is(nullValue())));
    assertThat(commitInfos.size(), not(is(0)));
    assertThat(commitInfos.get(0).getCommitId(), not(is(nullValue())));
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWitInvalidVariablePath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).addPath("TestView/BAD_VAR.js").build();
    command.execute();
  }

  @Test
  public void testCommitsInfoRetrievalForWholeRepository() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).build();
    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos, not(is(nullValue())));
    assertTrue(commitInfos.size() > 0);
  }

  /**
   * This is test is equivalent to test 'testCommitsInfoRetrievalForWholeRepository()'
   */
  @Test
  public void testCommitsInfoRetrievalWithEmptyPath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).addPath("").build();
    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos, not(is(nullValue())));
    assertTrue(commitInfos.size() > 0);
  }

  @Test
  public void testHeadAndCurrentCommitFlags() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).addPath("TestView/TOTO_VAR.js").build();

    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos, not(is(nullValue())));
    assertThat(commitInfos.size(), not(is(0)));
    CommitInfo firstCommit = commitInfos.get(0);
    assertThat(firstCommit.getCommitId(), not(is(nullValue())));
    assertThat(firstCommit.getIsHead(), is(true));
    assertThat(firstCommit.getIsCurrent(), is(true));
  }

  @Test
  public void testNotHeadAndCurrentCommitFlagsForVariable() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).addPath("TestView/PLACE_NAME.js").build();

    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos, not(is(nullValue())));
    assertThat(commitInfos.size(), not(is(0)));
    CommitInfo firstCommit = commitInfos.get(0);
    assertThat(firstCommit.getCommitId(), not(is(nullValue())));
    assertThat(firstCommit.getIsHead(), is(false));
    assertThat(firstCommit.getIsCurrent(), is(true));
  }

  @Test
  public void testNotHeadNotCurrentCommitFlagsForView() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(vcs.getRepository(DATASOURCE_NAME))
        .addDatasourceName(DATASOURCE_NAME).addPath("TestView").build();

    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos, not(is(nullValue())));
    assertThat(commitInfos.size(), greaterThan((5)));
    CommitInfo firstCommit = commitInfos.get(5);
    assertThat(firstCommit.getCommitId(), not(is(nullValue())));
    assertThat(firstCommit.getIsHead(), is(false));
    assertThat(firstCommit.getIsCurrent(), is(false));
  }

}