package org.obiba.opal.core.vcs.support;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.FS;

public final class OpalGitUtils {

  public static final String HEAD_COMMIT_ID = "HEAD";

  public static final Object VIEW_FILE_NAME = "View.xml";

  public static final Object VARIABLE_FILE_EXTENSION = ".js";

  public final static String OPAL_HOME_SYSTEM_PROPERTY_NAME = "OPAL_HOME";

  public static final String VIEWS_DIRECTORY_NAME = "views";

  public static final String GIT_DIRECTORY_NAME = "data" + File.separator + "git";

  public static final String GIT_EXTENSION = ".git";

  public static File buildOpalGitRootPath() {
    StringBuilder pathBuilder = new StringBuilder();
    pathBuilder.append(System.getProperty(OPAL_HOME_SYSTEM_PROPERTY_NAME)).append(File.separatorChar)
        .append(GIT_DIRECTORY_NAME).append(File.separatorChar).append(VIEWS_DIRECTORY_NAME);

    return new File(pathBuilder.toString());
  }

  public static File getGitDirectoryName(File root, String datasource) {
    return new File(root, datasource + GIT_EXTENSION);
  }

  public static String getViewFilePath(String view) {
    return new StringBuilder().append(view).append(File.separatorChar).append(VIEW_FILE_NAME).toString();
  }

  public static String getVariableFilePath(String view, String variable) {
    return new StringBuilder().append(view).append(File.separatorChar).append(variable).append(VARIABLE_FILE_EXTENSION)
        .toString();
  }

  public static String getNthCommitId(String commitId, int nth) {
    return commitId + "~" + String.valueOf(nth);
  }

  public static boolean isFilePath(String path) {
    // TODO find a safe check
    return path.contains(File.separator);
  }

  /**
   * Clone or Fetch a repository. If the local repository does not exist,
   * clone is called. If the repository does exist, fetch is called. By
   * default the clone/fetch retrieves the remote heads, tags, and notes.
   *
   * @param repositoriesFolder
   * @param name
   * @param fromUrl
   * @throws Exception
   */
  public static void cloneRepository(File repositoriesFolder, String name, String fromUrl) throws Exception {
    String repoName = name;
    // normal repository, strip .git suffix
    if(name.toLowerCase().endsWith(Constants.DOT_GIT_EXT)) {
      repoName = name.substring(0, name.indexOf(Constants.DOT_GIT_EXT));
    }

    File folder = new File(repositoriesFolder, repoName);
    if(folder.exists()) {
      File gitDir = RepositoryCache.FileKey.resolve(new File(repositoriesFolder, repoName), FS.DETECTED);
      Repository repository = new FileRepositoryBuilder().setGitDir(gitDir).build();
      fetchRepository(repository);
      repository.close();
    } else {
      CloneCommand clone = new CloneCommand();
      clone.setBare(false);
      clone.setCloneAllBranches(true);
      clone.setURI(fromUrl);
      clone.setDirectory(folder);
      Repository repository = clone.call().getRepository();

      // Now we have to fetch because CloneCommand doesn't fetch
      // refs/notes nor does it allow manual RefSpec.
      fetchRepository(repository);
      repository.close();
    }
  }

  /**
   * Fetch updates from the remote repository. If refSpecs is unspecifed,
   * remote heads, tags, and notes are retrieved.
   *
   * @param credentialsProvider
   * @param repository
   * @param refSpecs
   * @return FetchResult
   * @throws Exception
   */
  public static FetchResult fetchRepository(Repository repository, RefSpec... refSpecs) throws Exception {
    Git git = new Git(repository);
    FetchCommand fetch = git.fetch();
    List<RefSpec> specs = new ArrayList<>();
    if(refSpecs == null || refSpecs.length == 0) {
      specs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
      specs.add(new RefSpec("+refs/tags/*:refs/tags/*"));
      specs.add(new RefSpec("+refs/notes/*:refs/notes/*"));
    } else {
      specs.addAll(Arrays.asList(refSpecs));
    }
    fetch.setRefSpecs(specs);
    return fetch.call();
  }
}
