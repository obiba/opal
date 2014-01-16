/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.vcs.git.commands;

import java.util.List;

import org.junit.Test;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.git.OpalGitException;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings("ReuseOfLocalVariable")
public class OpalGitCommitsLogCommandTest extends AbstractOpalGitCommandTest {

  @Test
  public void testCommitsInfoRetrievalWitValidViewPath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME).addPath("TestView")
        .build();
    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos).isNotNull();
    assertThat(commitInfos).isNotEmpty();
    assertThat(commitInfos.get(0).getCommitId()).isNotNull();
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWitInvalidViewPath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME).addPath("DEADBEAF")
        .build();
    command.execute();
  }

  @Test
  public void testCommitsInfoRetrievalWitValidVariablePath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME)
        .addPath("TestView/TOTO_VAR.js").build();

    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos).isNotNull();
    assertThat(commitInfos).isNotEmpty();
    assertThat(commitInfos.get(0).getCommitId()).isNotNull();
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWitInvalidVariablePath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME)
        .addPath("TestView/BAD_VAR.js").build();
    command.execute();
  }

  @Test
  public void testCommitsInfoRetrievalForWholeRepository() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME).build();
    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos).isNotNull();
    assertThat(commitInfos).isNotEmpty();
  }

  /**
   * This is test is equivalent to test 'testCommitsInfoRetrievalForWholeRepository()'
   */
  @Test
  public void testCommitsInfoRetrievalWithEmptyPath() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME).addPath("").build();
    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos).isNotNull();
    assertThat(commitInfos).isNotEmpty();
  }

  @Test
  public void testHeadAndCurrentCommitFlags() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME)
        .addPath("TestView/TOTO_VAR.js").build();

    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos).isNotNull();
    assertThat(commitInfos).isNotEmpty();
    CommitInfo firstCommit = commitInfos.get(0);
    assertThat(firstCommit.getCommitId()).isNotNull();
    assertThat(firstCommit.getIsHead()).isTrue();
    assertThat(firstCommit.getIsCurrent()).isTrue();
  }

  @Test
  public void testNotHeadAndCurrentCommitFlagsForVariable() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME)
        .addPath("TestView/PLACE_NAME.js").build();

    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos).isNotNull();
    assertThat(commitInfos).isNotEmpty();
    CommitInfo firstCommit = commitInfos.get(0);
    assertThat(firstCommit.getCommitId()).isNotNull();
    assertThat(firstCommit.getIsHead()).isFalse();
    assertThat(firstCommit.getIsCurrent()).isTrue();
  }

  @Test
  public void testNotHeadNotCurrentCommitFlagsForView() {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME)).addDatasourceName(DATASOURCE_NAME).addPath("TestView")
        .build();

    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos).isNotNull();
    assertThat(commitInfos.size()).isGreaterThan(5);
    CommitInfo firstCommit = commitInfos.get(5);
    assertThat(firstCommit.getCommitId()).isNotNull();
    assertThat(firstCommit.getIsHead()).isFalse();
    assertThat(firstCommit.getIsCurrent()).isFalse();
  }

  @Test
  public void testHistoryWithDeletedCommits() {
    String datasourceWithDeletions = "Kobe";
    String path = "TestSitting/InstrumentRun.user.js";
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(
        versionControlSystem.getRepository(datasourceWithDeletions)).addDatasourceName(datasourceWithDeletions)
        .addPath(path).build();
    List<CommitInfo> commitInfos = command.execute();
    assertThat(commitInfos).hasSize(3);

    command = new OpalGitCommitsLogCommand.Builder(versionControlSystem.getRepository(datasourceWithDeletions))
        .addDatasourceName(datasourceWithDeletions).addPath(path).addExcludeDeletedRevisions(false).build();
    commitInfos = command.execute();
    assertThat(commitInfos).hasSize(5);

  }

}