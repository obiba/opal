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

import com.google.common.base.Strings;

public class OpalGitFetchBlobCommand extends OpalGitCommand<String> {

  private String path;
  private String commitId;
  private String encoding;


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
    // Resolve the revision specification
    final ObjectId id;
    String blob = "";

    try {
      id = repository.resolve(commitId);

      // Makes it simpler to release the allocated resources in one go
      ObjectReader reader = repository.newObjectReader();

      // Get the commit object for that revision
      RevWalk walk = new RevWalk(reader);
      RevCommit commit = walk.parseCommit(id);

      // Get the revision's file tree
      RevTree tree = commit.getTree();
      // .. and narrow it down to the single file's path
      TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);

      if(treewalk != null) {
        // use the blob id to read the file's data
        return new String(reader.open(treewalk.getObjectId(0)).getBytes(),
            Charset.forName(Strings.isNullOrEmpty(encoding) ? "UTF-8" : encoding));
      }

    } catch(Exception e) {
      throw new OpalGitException(e.getMessage(), e);
    }

    throw new OpalGitException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
  }
}