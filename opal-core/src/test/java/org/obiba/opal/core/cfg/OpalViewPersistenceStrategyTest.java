/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.obiba.core.util.FileUtil;
import org.obiba.git.command.GitCommandHandler;
import org.obiba.magma.Variable;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.vcs.OpalGitUtils;
import org.obiba.opal.core.vcs.OpalGitVersionControlSystem;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueView;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.magma.xstream.MagmaXStreamExtension;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.io.StreamException;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;

public class OpalViewPersistenceStrategyTest {

  private static final String EMPTY_DIRECTORY = "empty";

  private static final String OPAL_HOME = "OPAL_HOME";

  private ViewPersistenceStrategy viewPersistenceStrategy;

  @Before
  public void setUp() throws Exception {
    System.setProperty(OPAL_HOME, getTestFilesRoot());
    System.out.println(System.getProperty(OPAL_HOME));
    viewPersistenceStrategy = new OpalViewPersistenceStrategy();
    new MagmaEngine().extend(new MagmaXStreamExtension());
  }

  @After
  public void tearDown() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testReadWithNonExistentDirectory() throws Exception {
    System.setProperty(OPAL_HOME, getTestFilesRoot() + File.separator + EMPTY_DIRECTORY);
    // Re-initialise to pick up the the OPAL_HOME specified for this test.
    viewPersistenceStrategy = new OpalViewPersistenceStrategy();
    Set<ValueView> result = viewPersistenceStrategy.readViews("datasourceName");
    assertThat(result).isEmpty();
  }

  @Test
  public void testReadWithNoViewsFileReturnsEmptySet() throws Exception {
    Set<ValueView> result = viewPersistenceStrategy.readViews("datasourceName");
    assertThat(result).isEmpty();
  }

  @Test
  public void testReadOfSimpleView() throws Exception {
    Set<ValueView> result = viewPersistenceStrategy.readViews("simple-views");
    assertThat(result).hasSize(1);
  }

  @Test(expected = StreamException.class)
  public void testReadofEmptyViewFileThrowsStreamException() throws Exception {
    Set<ValueView> result = viewPersistenceStrategy.readViews("empty-views");
    assertThat(result).isEmpty();
  }

  @Ignore
  @Test
  public void testWriteEmptySetRemovesFile() throws Exception {
    Datasource datasourceMock = createMock(Datasource.class);
    expect(datasourceMock.getName()).andReturn("opal-data").atLeastOnce();

    ValueTable valueTableMock = createMock(ValueTable.class);
    expect(valueTableMock.getName()).andReturn("Participants").atLeastOnce();
    expect(valueTableMock.getDatasource()).andReturn(datasourceMock).atLeastOnce();

    replay(datasourceMock, valueTableMock);

    // Write a temporary views file with a single view.
    Set<ValueView> views = Sets.newHashSet();
    View view = new View("aView", valueTableMock);
    views.add(view);
    viewPersistenceStrategy.writeViews("temporary-views", views, null, null);
    // Verify the temporary views file exists.
    Set<ValueView> singleViewResult = viewPersistenceStrategy.readViews("temporary-views");
    assertThat(singleViewResult).hasSize(1);
    // Write the temporary views file with an empty views set. This will remove the file.
    viewPersistenceStrategy.writeViews("temporary-views", ImmutableSet.<ValueView>of(), null, null);
    // Verify that the temporary file has been removed, by ensuring that an empty set has been returned.
    Set<ValueView> noViewsResult = viewPersistenceStrategy.readViews("temporary-views");
    assertThat(noViewsResult).isEmpty();
  }

  @Ignore
  @Test
  public void testWriteSingleView() throws Exception {
    Datasource datasourceMock = createMock(Datasource.class);
    expect(datasourceMock.getName()).andReturn("opal-data").atLeastOnce();

    ValueTable valueTableMock = createMock(ValueTable.class);
    expect(valueTableMock.getName()).andReturn("Participants").atLeastOnce();
    expect(valueTableMock.getDatasource()).andReturn(datasourceMock).atLeastOnce();

    replay(datasourceMock, valueTableMock);

    Set<ValueView> views = Sets.newHashSet();
    View view = new View("aView", valueTableMock);
    views.add(view);
    viewPersistenceStrategy.writeViews("single-views", views, null, null);
  }

  @Test
  public void testReadViewAtCommit() throws Exception {
    // the commit and tag author is read from the shiro subject, anonymous here
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    MagmaEngine.get().extend(new MagmaJsExtension());
    // unique datasource name, because the git folders are shared within the JVM
    String datasource = "read-view-" + System.nanoTime();
    GitCommandHandler handler = new GitCommandHandler();
    OpalViewPersistenceStrategy strategy = new OpalViewPersistenceStrategy();
    strategy.setGitCommandHandler(handler);
    OpalGitVersionControlSystem vcs = new OpalGitVersionControlSystem();
    ReflectionTestUtils.setField(vcs, "gitCommandHandler", handler);
    try {
      StaticValueTable table = new StaticValueTable(new StaticDatasource("ds"), "T", ImmutableSet.of("1"));
      table.addVariables(TextType.get(), "A");

      strategy.writeView(datasource, newView(table, "$('A').eq('a')", "X", "Y"), "first", null);
      vcs.createTag(datasource, "v", "t1", null);
      String commitId = vcs.getTags(datasource, "v").iterator().next().getCommitId();
      strategy.writeView(datasource, newView(table, "$('A').eq('b')", "X"), "second", null);

      View restored = (View) strategy.readView(datasource, "v", commitId);
      assertThat(((JavascriptClause) restored.getWhereClause()).getScript()).isEqualTo("$('A').eq('a')");
      assertThat(variableNames(restored)).containsOnly("X", "Y");
      View latest = (View) strategy.readView(datasource, "v", OpalGitUtils.HEAD_COMMIT_ID);
      assertThat(variableNames(latest)).containsOnly("X");
    } finally {
      SecurityUtils.setSecurityManager(null);
      FileUtil.delete(OpalGitUtils.getGitViewsRepoFolder(datasource));
      FileUtil.delete(new File(OpalGitUtils.getGitViewsWorkFolder(), datasource));
    }
  }

  private View newView(ValueTable table, String where, String... variables) {
    View view = new View("v", table);
    view.setWhereClause(new JavascriptClause(where));
    VariablesClause clause = new VariablesClause();
    List<Variable> vars = new ArrayList<>();
    for (String name : variables) {
      vars.add(Variable.Builder.newVariable(name, TextType.get(), "Participant").addAttribute("script", "'" + name + "'").build());
    }
    clause.setVariables(vars);
    view.setListClause(clause);
    Initialisables.initialise(view);
    return view;
  }

  private List<String> variableNames(View view) {
    VariablesClause clause = (VariablesClause) view.getListClause();
    clause.setValueTable(view);
    clause.initialise();
    List<String> names = new ArrayList<>();
    clause.getVariableValueSources().forEach(source -> names.add(source.getVariable().getName()));
    return names;
  }

  private String getTestFilesRoot() {
    return getClass().getResource("/" + getClass().getSimpleName()).getFile();
  }
}
