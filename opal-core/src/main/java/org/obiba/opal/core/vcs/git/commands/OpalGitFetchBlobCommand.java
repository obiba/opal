package org.obiba.opal.core.vcs.git.commands;

import java.nio.charset.Charset;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.support.OpalGitUtils;

import com.google.common.base.Strings;

public class OpalGitFetchBlobCommand extends OpalGitCommand<String> {

  private String path;
  private String commitId;
  private String encoding;


  public OpalGitFetchBlobCommand(Repository repository, String datasourceName) {
    super(repository, datasourceName);
  }

  public OpalGitFetchBlobCommand(Repository repository) {
    super(repository);
  }

  public OpalGitFetchBlobCommand addPath(String value) {
    path = value;
    return this;
  }

  public OpalGitFetchBlobCommand addCommitId(String value) {
    commitId = value;
    return this;
  }

  public OpalGitFetchBlobCommand addEncoding(String value) {
    encoding = value;
    return this;
  }

  @Override
  public String execute() {
    try {
      validate();

      final ObjectId id = repository.resolve(commitId);
      ObjectReader reader = repository.newObjectReader();
      RevWalk walk = new RevWalk(reader);
      RevCommit commit = walk.parseCommit(id);
      RevTree tree = commit.getTree();
      TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);

      if(treewalk != null) {
        return new String(reader.open(treewalk.getObjectId(0)).getBytes(),
            Charset.forName(Strings.isNullOrEmpty(encoding) ? "UTF-8" : encoding));
      }

    } catch(Exception e) {
      throw new OpalGitException(e.getMessage(), e);
    }

    throw new OpalGitException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
  }

  protected void validate() {
    if (Strings.isNullOrEmpty(commitId)) {
      throw new OpalGitException("Commit id cannot be null");
    }

    if (Strings.isNullOrEmpty(path)) {
      throw new OpalGitException("Path cannot be null");
    }
    else if (!OpalGitUtils.isFilePath(path)) {
      throw new OpalGitException("Path must point to a file and not a folder");
    }
  }
}