/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.vcs.command;

import java.io.File;
import java.nio.file.Files;

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.git.command.GitCommandHandler;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueView;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.View;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.vcs.OpalGitUtils;

import static org.fest.assertions.api.Assertions.assertThat;

public class OpalWriteViewsCommandTest {

  private File root;

  private boolean opalHomeSet;

  @Before
  public void setUp() throws Exception {
    root = Files.createTempDirectory("opal-git-views").toFile();
    opalHomeSet = System.getProperty("OPAL_HOME") == null;
    if (opalHomeSet) System.setProperty("OPAL_HOME", root.getAbsolutePath());
    new MagmaEngine().extend(new MagmaXStreamExtension());
    // the commit author is read from the shiro subject, anonymous here
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
  }

  @After
  public void tearDown() throws Exception {
    SecurityUtils.setSecurityManager(null);
    MagmaEngine.get().shutdown();
    if (opalHomeSet) System.clearProperty("OPAL_HOME");
    FileUtils.delete(root, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING);
  }

  @Test
  public void test_where_script_kept_after_second_write() throws Exception {
    StaticValueTable table = new StaticValueTable(new StaticDatasource("ds"), "T", ImmutableSet.of("1"));
    table.addVariables(TextType.get(), "A");
    View view = new View("v", table);
    view.setWhereClause(new JavascriptClause("$this('A').eq('x')"));
    ImmutableSet<ValueView> views = ImmutableSet.of(view);

    // unique repository name, because the work folder is shared within the JVM
    File repo = new File(root, root.getName() + ".git");
    GitCommandHandler handler = new GitCommandHandler();
    // the second write is the one that used to remove __where.js
    handler.execute(new OpalWriteViewsCommand.Builder(repo, views, "first", null).build());
    handler.execute(new OpalWriteViewsCommand.Builder(repo, views, "second", null).build());

    assertThat(existsInHead(repo, "v/" + OpalGitUtils.VIEW_WHERE_FILE_NAME)).isTrue();
    assertThat(existsInHead(repo, "v/A" + OpalGitUtils.VARIABLE_FILE_EXTENSION)).isTrue();
  }

  private boolean existsInHead(File repo, String path) throws Exception {
    try (Git git = Git.open(repo); RevWalk walk = new RevWalk(git.getRepository())) {
      Repository repository = git.getRepository();
      try (TreeWalk tw = TreeWalk.forPath(repository, path,
          walk.parseCommit(repository.resolve("HEAD")).getTree())) {
        return tw != null;
      }
    }
  }
}
