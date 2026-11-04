/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.vcs;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.git.CommitInfo;
import org.obiba.git.GitException;
import org.obiba.git.TagInfo;
import org.obiba.git.command.GitCommandHandler;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueView;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.View;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.vcs.command.OpalWriteViewsCommand;
import org.springframework.test.util.ReflectionTestUtils;

import static org.fest.assertions.api.Assertions.assertThat;

public class OpalGitVersionControlSystemTest {

  private File root;

  private boolean opalHomeSet;

  // unique datasource name, because the git folders are shared within the JVM
  private String datasource;

  private GitCommandHandler handler;

  private OpalGitVersionControlSystem vcs;

  @Before
  public void setUp() throws Exception {
    root = Files.createTempDirectory("opal-git-vcs").toFile();
    opalHomeSet = System.getProperty("OPAL_HOME") == null;
    if (opalHomeSet) System.setProperty("OPAL_HOME", root.getAbsolutePath());
    new MagmaEngine().extend(new MagmaXStreamExtension());
    // the commit and tag author is read from the shiro subject, anonymous here
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    datasource = root.getName();
    handler = new GitCommandHandler();
    vcs = new OpalGitVersionControlSystem();
    ReflectionTestUtils.setField(vcs, "gitCommandHandler", handler);
  }

  @After
  public void tearDown() throws Exception {
    SecurityUtils.setSecurityManager(null);
    MagmaEngine.get().shutdown();
    FileUtils.delete(OpalGitUtils.getGitViewsRepoFolder(datasource), FileUtils.RECURSIVE | FileUtils.SKIP_MISSING);
    FileUtils.delete(new File(OpalGitUtils.getGitViewsWorkFolder(), datasource),
        FileUtils.RECURSIVE | FileUtils.SKIP_MISSING);
    if (opalHomeSet) System.clearProperty("OPAL_HOME");
    FileUtils.delete(root, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING);
  }

  @Test
  public void test_views_keep_separate_tags() {
    writeViews("v1", "v 2");

    vcs.createTag(datasource, "v1", "t1", "first tag");
    vcs.createTag(datasource, "v 2", "t2", null);

    assertThat(names(vcs.getTags(datasource, "v1"))).containsOnly("t1");
    assertThat(names(vcs.getTags(datasource, "v 2"))).containsOnly("t2");
    assertThat(vcs.getTags(datasource, "v1").iterator().next().getMessage()).contains("first tag");

    Iterable<CommitInfo> commits = vcs.getCommitsInfo(datasource, "v 2", "v 2");
    assertThat(commits.iterator().next().getTags()).containsOnly("t2");
  }

  @Test
  public void test_same_tag_name_in_two_views() {
    writeViews("v1", "v 2");

    vcs.createTag(datasource, "v1", "t", null);
    vcs.createTag(datasource, "v 2", "t", null);

    assertThat(names(vcs.getTags(datasource, "v1"))).containsOnly("t");
    assertThat(names(vcs.getTags(datasource, "v 2"))).containsOnly("t");
  }

  @Test
  public void test_tags_of_views_with_dots_and_stars_in_their_name() {
    String[] views = { "x.lock", ".hidden", "a..b", "end.", "a*" };
    writeViews(views);

    for (String view : views) {
      vcs.createTag(datasource, view, "t", null);
      assertThat(names(vcs.getTags(datasource, view))).containsOnly("t");
    }
  }

  @Test(expected = GitException.class)
  public void test_existing_tag_rejected() {
    writeViews("v1");
    vcs.createTag(datasource, "v1", "t1", null);
    vcs.createTag(datasource, "v1", "t1", null);
  }

  @Test(expected = GitException.class)
  public void test_tag_name_with_space_rejected() {
    writeViews("v1");
    vcs.createTag(datasource, "v1", "my tag", null);
  }

  private void writeViews(String... names) {
    StaticValueTable table = new StaticValueTable(new StaticDatasource("ds"), "T", ImmutableSet.of("1"));
    table.addVariables(TextType.get(), "A");
    ImmutableSet.Builder<ValueView> views = ImmutableSet.builder();
    for (String name : names) views.add(new View(name, table));
    handler.execute(
        new OpalWriteViewsCommand.Builder(OpalGitUtils.getGitViewsRepoFolder(datasource), views.build(), "write", null)
            .build());
  }

  private List<String> names(Iterable<TagInfo> tags) {
    List<String> names = new ArrayList<>();
    tags.forEach(tag -> names.add(tag.getName()));
    return names;
  }
}
