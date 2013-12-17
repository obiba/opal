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
import org.obiba.opal.core.vcs.git.OpalGitException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OpalGitFetchBlobCommandTest extends AbstractOpalGitCommandTest {

  @Test(expected = OpalGitException.class)
  public void testBlobRetrievalWithInvalidViewPath() {
    new OpalGitFetchBlobCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME), "Zozo/PLACE_NAME.js",
        COMMIT_ID).addDatasourceName(DATASOURCE_NAME).build().execute();
  }

  @Test(expected = OpalGitException.class)
  public void testBlobRetrievalWithInvalidCommit() {
    new OpalGitFetchBlobCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME), "TestView/PLACE_NAME.js",
        BAD_COMMIT_ID).addDatasourceName(DATASOURCE_NAME).build().execute();
  }

  @Test
  public void testBlobRetrievalWithValidVariablePath() {
    OpalGitFetchBlobCommand command = new OpalGitFetchBlobCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME), "TestView/PLACE_NAME.js", COMMIT_ID)
        .addDatasourceName(DATASOURCE_NAME).build();
    String blob = command.execute();
    assertThat(blob, equalTo("$('PLACE_NAME')"));
  }

  @Test(expected = OpalGitException.class)
  public void testBlobRetrievalWithFolderPath() {
    OpalGitFetchBlobCommand command = new OpalGitFetchBlobCommand.Builder(
        versionControlSystem.getRepository(DATASOURCE_NAME), "TestView", COMMIT_ID).addDatasourceName(DATASOURCE_NAME)
        .build();
    command.execute();
  }
}

