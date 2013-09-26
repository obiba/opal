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
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.git.commands.OpalGitFetchBlobCommand;
import org.obiba.opal.core.vcs.git.support.TestOpalGitVersionControlSystem;

public class OpalGitFetchBlobCommandTest {

  private static final String COMMIT_ID = "448b81ed146cc76751c3b10b89e80cc99da63427";

  private static final String BAD_COMMIT_ID = "DeadBeefDeadBeefDeadBeefDeadBeefDeadBeef";

  private static final String DATASOURCE_NAME = "opal-data2";

  private AtomicReference<TestOpalGitVersionControlSystem> vcs = new AtomicReference<TestOpalGitVersionControlSystem>();

  private AtomicReference<OpalGitFetchBlobCommand> command = new AtomicReference<OpalGitFetchBlobCommand>();

  @Before
  public void setup() {
    vcs.set(new TestOpalGitVersionControlSystem());
    command.set(new OpalGitFetchBlobCommand(vcs.get().getRepository(DATASOURCE_NAME), DATASOURCE_NAME));
  }

  @Test
  public void testCommitsInfoRetrievalWithValidViewPath() {
    try {
      command.get().addCommitId(COMMIT_ID).addPath("TestView/TOTO_VAR.js").execute();
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWithViewPath() {
    command.get().addCommitId(COMMIT_ID).addPath("TestView").execute();
  }

  @Test(expected = OpalGitException.class)
  public void testCommitsInfoRetrievalWithVariabnleOnlyPath() {
    command.get().addCommitId(COMMIT_ID).addPath("TOTO_VAR.js").execute();
  }


}

