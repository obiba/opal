package org.obiba.opal.core.vcs.git.commands;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

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

/**
 * Opal GIT command used to extract the content of a file. Folders are not supported.
 */
public class OpalGitFetchBlobCommand extends OpalGitCommand<String> {

  private String path;
  private String commitId;
  private String encoding;

  private OpalGitFetchBlobCommand(Builder builder) {
    super(builder.repository, builder.datasourceName);
    path = builder.path;
    encoding = builder.encoding;
    commitId = builder.commitId;
  }

  @Override
  public String execute() {
    try {
      ObjectReader reader = repository.newObjectReader();
      TreeWalk treewalk = getPathTreeWalk(reader);

      if(treewalk != null) {
        return new String(reader.open(treewalk.getObjectId(0)).getBytes(),
            Charset.forName(Strings.isNullOrEmpty(encoding) ? "UTF-8" : encoding));
      }

    } catch(Exception e) {
      throw new OpalGitException(e.getMessage(), e);
    }

    throw new OpalGitException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
  }

  private TreeWalk getPathTreeWalk(ObjectReader reader) throws IOException {
    ObjectId id = repository.resolve(commitId);
    RevWalk walk = new RevWalk(reader);
    RevCommit commit = walk.parseCommit(id);
    RevTree tree = commit.getTree();
    return TreeWalk.forPath(reader, path, tree);
  }

  /**
   * Builder class for OpalGitFetchBlobCommand
   */
  public static class Builder extends OpalGitCommand.Builder<Builder> {

    private final String commitId;
    private String encoding;

    public Builder(@Nonnull Repository repository, @Nonnull String path, @Nonnull String commitId) {
      super(repository);
      addPath(path);
      this.commitId = commitId;
    }

    public Builder addEncoding(String value) {
      encoding = value;
      return this;
    }

    public OpalGitFetchBlobCommand build() {
      if (Strings.isNullOrEmpty(commitId)) throw new OpalGitException("Commit id cannot be empty nor null");
      if (Strings.isNullOrEmpty(path)) throw new OpalGitException("Commit path cannot be empty nor null");
      if (!OpalGitUtils.isFilePath(path)) throw new OpalGitException("Commit path must point to a file and not a folder");
      return new OpalGitFetchBlobCommand(this);
    }

  }
}