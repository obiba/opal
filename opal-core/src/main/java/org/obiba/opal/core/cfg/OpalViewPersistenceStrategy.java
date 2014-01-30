/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.obiba.core.util.FileUtil;
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.audit.OpalUserProvider;
import org.obiba.opal.core.vcs.git.support.GitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;

/**
 * This implementation of the {@link ViewPersistenceStrategy} serializes and de-serializes {@link View}s to XML files.
 * Each XML file is named after a {@link Datasource} and contains all the Views that are associated with that
 * Datasource.
 */
public class OpalViewPersistenceStrategy implements ViewPersistenceStrategy {

  private static final Logger log = LoggerFactory.getLogger(OpalViewPersistenceStrategy.class);

  public final static String OPAL_HOME_SYSTEM_PROPERTY_NAME = "OPAL_HOME";

  public static final String CONF_DIRECTORY_NAME = "conf";

  public static final String VIEWS_DIRECTORY_NAME = "views";

  public static final String GIT_DIRECTORY_NAME = "data" + File.separator + "git";

  public static final String VIEW_FILE_NAME = "View.xml";

  private final OpalUserProvider opalUserProvider;

  private final File viewsDirectory;

  private final File gitViewsDirectory;

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

  private final Lock readLock = readWriteLock.readLock();

  private final Lock writeLock = readWriteLock.writeLock();

  public OpalViewPersistenceStrategy() {
    this(null);
  }

  public OpalViewPersistenceStrategy(OpalUserProvider opalUserProvider) {
    this.opalUserProvider = opalUserProvider;
    String viewsDirectoryName = System.getProperty(OPAL_HOME_SYSTEM_PROPERTY_NAME) + File.separator //
        + CONF_DIRECTORY_NAME + File.separator + VIEWS_DIRECTORY_NAME;
    viewsDirectory = new File(viewsDirectoryName);
    gitViewsDirectory = new File(System.getProperty(OPAL_HOME_SYSTEM_PROPERTY_NAME),
        GIT_DIRECTORY_NAME + File.separator + VIEWS_DIRECTORY_NAME);
  }

  @Override
  public void initialise() {
  }

  @Override
  public void dispose() {
  }

  @Override
  public void writeViews(@NotNull final String datasourceName, @NotNull final Set<View> views,
      @Nullable final String comment) {
    executeWithWriteLock(new Action() {
      @Override
      public void execute() {
        doWriteGitViews(datasourceName, views, comment);
      }
    });
  }

  @Override
  public void writeView(@NotNull final String datasourceName, @NotNull final View view,
      @Nullable final String comment) {
    executeWithWriteLock(new Action() {
      @Override
      public void execute() {
        doWriteGitViews(datasourceName, ImmutableSet.of(view), comment);
      }
    });
  }

  @Override
  public void removeView(@NotNull final String datasourceName, @NotNull final String viewName) {
    executeWithWriteLock(new Action() {
      @Override
      public void execute() {
        doRemoveGitView(datasourceName, viewName);
      }
    });
  }

  @Override
  public void removeViews(final String datasourceName) {
    executeWithWriteLock(new Action() {
      @Override
      public void execute() {
        try {
          FileUtil.delete(getDatasourceViewsGit(datasourceName));
        } catch(IOException e) {
          throw new RuntimeException("Failed deleting views in git for datasource: " + datasourceName, e);
        }
      }
    });
  }

  @Override
  public Set<View> readViews(@NotNull final String datasourceName) {
    return executeWithReadLock(new ActionWithReturn<Set<View>>() {
      @Override
      public Set<View> execute() {
        File gitDir = getDatasourceViewsGit(datasourceName);
        return gitDir.exists() ? doReadGitViews(datasourceName) : doReadViews(datasourceName);
      }
    });
  }

  private void doCommitPush(Git git, String message) throws GitAPIException {
    git.commit().setAuthor(getAuthorName(), "opal@obiba.org").setCommitter("opal", "opal@obiba.org").setMessage(message)
        .call();
    git.push().setPushAll().setRemote("origin").call();
  }

  private void doWriteGitViews(@NotNull String datasourceName, @NotNull Iterable<View> views,
      @Nullable String comment) {
    File localRepo = null;

    try {
      // Fetch or clone a tmp working directory
      localRepo = cloneDatasourceViewsGit(datasourceName);

      // Serialize all view files in the repo
      List<String> varFilesToRemove = Lists.newArrayList();
      StringBuilder message = new StringBuilder();
      serializeAllViewFiles(views, localRepo, varFilesToRemove, message);

      // Push changes
      Git git = new Git(new FileRepository(new File(localRepo, ".git")));
      for(String toRemove : varFilesToRemove) {
        git.rm().addFilepattern(toRemove).call();
      }
      git.add().addFilepattern(".").call();
      doCommitPush(git, Strings.isNullOrEmpty(comment) ? "Update " + message : comment);

    } catch(Exception e) {
      throw new RuntimeException("Failed writing views in git for datasource: " + datasourceName, e);
    } finally {
      deleteLocalRepo(localRepo);
    }
  }

  private void serializeAllViewFiles(Iterable<View> views, File localRepo, Collection<String> varFilesToRemove,
      StringBuilder message) throws IOException {
    for(View view : views) {
      doWriteGitView(localRepo, view, varFilesToRemove);
      if(message.length() > 0) {
        message.append(", ");
      }
      message.append(view.getName());
    }
  }

  private void doRemoveGitView(@NotNull String datasourceName, @NotNull String viewName) {
    File localRepo = null;
    try {
      localRepo = cloneDatasourceViewsGit(datasourceName);
      Git git = new Git(new FileRepository(new File(localRepo, ".git")));
      git.rm().addFilepattern(viewName).call();
      doCommitPush(git, "Remove " + viewName);
    } catch(Exception e) {
      throw new RuntimeException("Failed removing view '" + viewName + "' from git for datasource: " + datasourceName,
          e);
    } finally {
      deleteLocalRepo(localRepo);
    }
  }

  private void deleteLocalRepo(File localRepo) {
    if(localRepo == null) return;

    try {
      FileUtil.delete(localRepo);
    } catch(IOException e) {
      log.warn("Failed removing local git repository: {}", localRepo.getAbsolutePath(), e);
    }
  }

  private File[] listViewDirectories(File localRepo) {
    return localRepo.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory() && !".git".equals(pathname.getName()) &&
            new File(pathname, VIEW_FILE_NAME).exists();
      }
    });
  }

  private File cloneDatasourceViewsGit(String datasourceName) throws Exception {
    File targetDir = getDatasourceViewsGit(datasourceName);

    // Create datasource views bare git repo
    Repository newRepo = new FileRepository(targetDir);
    if(!targetDir.exists()) {
      newRepo.create(true);
    }

    String remoteUrl = "file://" + targetDir.getAbsolutePath();
    File tmp = getTmpDirectory();
    File localRepo = new File(tmp, "opal-" + Long.toString(System.nanoTime()));
    GitUtils.cloneRepository(localRepo.getParentFile(), localRepo.getName(), remoteUrl);
    return localRepo;
  }

  private void doWriteGitView(File localRepo, ValueTable view, Collection<String> varFilesToRemove) throws IOException {
    File viewRepo = new File(localRepo, view.getName());
    viewRepo.mkdirs();

    // Write serialiazed view
    File viewFile = new File(viewRepo, VIEW_FILE_NAME);
    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(viewFile), Charsets.UTF_8);
    try {
      getXStream().toXML(view, writer);
    } finally {
      StreamUtil.silentSafeClose(writer);
    }

    // Write variable script files
    for(Variable variable : view.getVariables()) {
      doWriteGitViewVariable(viewRepo, variable);
    }
    // Detect removed variables

    for(File f : viewRepo.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".js");
      }
    })) {
      String varName = f.getName().substring(0, f.getName().length() - 3);
      if(!view.hasVariable(varName)) {
        varFilesToRemove.add(f.getParentFile().getName() + "/" + f.getName());
      }
    }
  }

  private void doWriteGitViewVariable(File viewRepo, Variable variable) throws IOException {
    String script = variable.hasAttribute("script") ? variable.getAttributeStringValue("script") : "null";
    File variableFile = new File(viewRepo, variable.getName() + ".js");

    FileWriter fileWriter = new FileWriter(variableFile);
    try {
      fileWriter.append(script);
      fileWriter.flush();
    } finally {
      StreamUtil.silentSafeClose(fileWriter);
    }
  }

  private File getDatasourceViewsGit(String datasourceName) {
    return new File(gitViewsDirectory, datasourceName + ".git");
  }

  private String getAuthorName() {
    return opalUserProvider == null ? OpalUserProvider.UNKNOWN_USERNAME : opalUserProvider.getUsername();
  }

  private File getTmpDirectory() throws IOException {
    // for java 6 compatibility
    File tmp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    tmp.delete();
    return tmp.getParentFile();
  }

  @SuppressWarnings("unchecked")
  @NotNull
  private Set<View> doReadViews(@NotNull String datasourceName) {
    Set<View> result = ImmutableSet.of();
    if(!viewsDirectory.isDirectory()) {
      log.info("The views directory '{}' does not exist.", viewsDirectory.getAbsolutePath());
      return result;
    }
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(getDatasourceViewsFile(datasourceName)), Charsets.UTF_8);
      result = (Set<View>) getXStream().fromXML(reader);
    } catch(FileNotFoundException e) {
      return ImmutableSet.of();
    } finally {
      StreamUtil.silentSafeClose(reader);
    }
    return result;
  }

  @NotNull
  private Set<View> doReadGitViews(@NotNull String datasourceName) {
    ImmutableSet.Builder<View> builder = ImmutableSet.builder();

    File localRepo = null;
    try {
      localRepo = cloneDatasourceViewsGit(datasourceName);

      for(File f : listViewDirectories(localRepo)) {
        View view = doReadGitView(f);
        if(view != null) {
          builder.add(view);
        }
      }
    } catch(Exception e) {
      throw new RuntimeException("Failed reading views from git for datasource: " + datasourceName, e);
    } finally {
      deleteLocalRepo(localRepo);
    }

    return builder.build();
  }

  private View doReadGitView(File viewRepo) {
    View result = null;
    File viewFile = new File(viewRepo, VIEW_FILE_NAME);
    if(viewFile.exists()) {
      InputStreamReader reader = null;
      try {
        reader = new InputStreamReader(new FileInputStream(viewFile), Charsets.UTF_8);
        result = (View) getXStream().fromXML(reader);
      } catch(FileNotFoundException e) {
        // not supposed to happen
      } finally {
        StreamUtil.silentSafeClose(reader);
      }
    }
    return result;
  }

  protected XStream getXStream() {
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

  private String normalizeDatasourceName(@SuppressWarnings("TypeMayBeWeakened") String datasourceName) {
    return Pattern.compile("([^a-zA-Z0-9-_. ])").matcher(datasourceName).replaceAll("");
  }

  private File getDatasourceViewsFile(String datasourceName) {
    return new File(viewsDirectory, normalizeDatasourceName(datasourceName) + ".xml");
  }

  private void executeWithWriteLock(Action action) {
    writeLock.lock();
    try {
      action.execute();
    } finally {
      writeLock.unlock();
    }
  }

  private <T> T executeWithReadLock(ActionWithReturn<T> action) {
    readLock.lock();
    try {
      return action.execute();
    } finally {
      readLock.unlock();
    }
  }

  private interface Action {
    void execute();
  }

  private interface ActionWithReturn<T> {
    T execute();
  }

}
