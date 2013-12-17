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

import java.io.File;
import java.io.FileNotFoundException;

import org.obiba.opal.core.vcs.git.OpalGitVersionControlSystem;
import org.springframework.util.ResourceUtils;

public abstract class AbstractOpalGitCommandTest {

  static final String COMMIT_ID = "448b81ed146cc76751c3b10b89e80cc99da63427";

  static final String BAD_COMMIT_ID = "DeadBeefDeadBeefDeadBeefDeadBeefDeadBeef";

  static final String DATASOURCE_NAME = "opal-data2";

  final OpalGitVersionControlSystem versionControlSystem;

  public AbstractOpalGitCommandTest() {
    File repoPath = null;
    try {
      repoPath = ResourceUtils.getFile("classpath:org/obiba/opal/core/vcs/git/commands/views");
    } catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    versionControlSystem = new OpalGitVersionControlSystem(repoPath);
  }

}
