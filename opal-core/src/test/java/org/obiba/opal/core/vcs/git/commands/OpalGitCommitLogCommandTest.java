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

import org.junit.Test;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.git.OpalGitException;

import static org.fest.assertions.api.Assertions.assertThat;

public class OpalGitCommitLogCommandTest extends AbstractOpalGitCommandTest {

  @Test
  public void testCommitInfoRetrievalWithValidCommitId() {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME), "TestView", COMMIT_ID).build();
    CommitInfo commitInfo = command.execute();
    assertThat(commitInfo).isNotNull();
    assertThat(commitInfo.getCommitId()).isEqualTo(COMMIT_ID);
    assertThat(commitInfo.getAuthor()).isEqualTo("administrator");
    assertThat(commitInfo.getComment()).isEqualTo("Update TestView");
  }

  @Test(expected = OpalGitException.class)
  public void testCommitInfoRetrievalWithInvalidCommitId() {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME), "TestView", BAD_COMMIT_ID).build();
    command.execute();
  }

  @Test
  public void testCommitInfoRetrievalWithValidVariable() {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME), "TestView/TOTO_VAR.js", COMMIT_ID).build();
    CommitInfo commitInfo = command.execute();
    assertThat(commitInfo).isNotNull();
    assertThat(commitInfo.getCommitId()).isEqualTo(COMMIT_ID);
    assertThat(commitInfo.getAuthor()).isEqualTo("administrator");
    assertThat(commitInfo.getComment()).isEqualTo("Update TestView");
  }

  @Test(expected = OpalGitException.class)
  public void testCommitInfoRetrievalWithInvalidVariable() {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME), "TestView/BAD_VAR.js", COMMIT_ID).build();
    command.execute();
  }

}

