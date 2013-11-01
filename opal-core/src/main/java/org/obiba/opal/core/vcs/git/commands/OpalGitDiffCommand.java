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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.support.OpalGitUtils;

import com.google.common.base.Strings;

/**
 * Opal GIT command used to extract the diff between two commits. By default, the diff is between the given commit and
 * its parent. By providing a valid 'nthCommit' value, the command will extract the appropriate diff from the repo.
 */
public class OpalGitDiffCommand extends OpalGitCommand<List<String>> {

  private final String path;

  private final String commitId;

  private String previousCommitId;

  private int nthCommit = 1;

  private OpalGitDiffCommand(Builder builder) {
    super(builder.repository);
    commitId = builder.commitId;
    path = builder.path;
    nthCommit = builder.nthCommit;
    previousCommitId = builder.previousCommitId;
  }

  @Override
  public List<String> execute() {
    ObjectReader reader = repository.newObjectReader();

    try {
      DiffCurrentPreviousTreeParsersFactory test = new DiffCurrentPreviousTreeParsersFactory(reader).create();
      CanonicalTreeParser currentCommitParser = test.getCurrentCommitParser();
      AbstractTreeIterator previousCommitParser = test.getPreviousCommitParser();
      return CompareDiffTrees(currentCommitParser, previousCommitParser);

    } catch(IOException e) {
      throw new OpalGitException(e.getMessage(), e);
    } finally {
      reader.release();
    }
  }

  private List<String> CompareDiffTrees(CanonicalTreeParser currentCommitParser,
      AbstractTreeIterator previousCommitParser) throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    List<String> diffEntries = new ArrayList<String>();
    DiffFormatter df = new DiffFormatter(out);
    df.setRepository(repository);
    df.setDiffComparator(RawTextComparator.DEFAULT);
    df.setDetectRenames(true);

    if(!Strings.isNullOrEmpty(path)) {
      df.setPathFilter(PathFilter.create(path));
    }

    List<DiffEntry> diffs = df.scan(previousCommitParser, currentCommitParser);

    for(DiffEntry diffEntry : diffs) {
      df.format(diffEntry);
      diffEntry.getOldId();
      diffEntries.add(out.toString("UTF-8"));
      out.reset();
    }

    return diffEntries;
  }

  /**
   * Builder class for OpalGitDiffCommand
   */
  public static class Builder extends OpalGitCommand.Builder<Builder> {

    private final String commitId;

    private int nthCommit = 1;

    private String previousCommitId;

    public Builder(@NotNull Repository repository, @NotNull String commitId) {
      super(repository);
      this.commitId = commitId;
    }

    public Builder addNthCommit(int value) {
      nthCommit = value;
      return this;
    }

    public Builder addPreviousCommitId(String value) {
      previousCommitId = value;
      return this;
    }

    public OpalGitDiffCommand build() {
      if(Strings.isNullOrEmpty(commitId)) throw new OpalGitException("Commit id cannot be null");
      return new OpalGitDiffCommand(this);
    }

  }

  private class DiffCurrentPreviousTreeParsersFactory {
    private ObjectReader reader;

    private CanonicalTreeParser currentCommitParser;

    private AbstractTreeIterator previousCommitParser;

    private DiffCurrentPreviousTreeParsersFactory(ObjectReader reader) {this.reader = reader;}

    public CanonicalTreeParser getCurrentCommitParser() {
      return currentCommitParser;
    }

    public AbstractTreeIterator getPreviousCommitParser() {
      return previousCommitParser;
    }

    public DiffCurrentPreviousTreeParsersFactory create() throws IOException {
      RevCommit currentCommit = getCommitById(commitId);

      if(currentCommit == null) {
        throw new OpalGitException(String.format("There are no commit for commit id '%s'", commitId));
      }

      currentCommitParser = new CanonicalTreeParser();
      currentCommitParser.reset(reader, currentCommit.getTree());

      previousCommitParser = null;
      RevCommit previousCommit = !Strings.isNullOrEmpty(previousCommitId)
          ? getCommitById(previousCommitId)
          : getCommitById(OpalGitUtils.getNthCommitId(commitId, nthCommit));

      if(previousCommit == null) {
        // currentCommit is the first commit in the tree
        previousCommitParser = new EmptyTreeIterator();
      } else {
        CanonicalTreeParser parser = new CanonicalTreeParser();
        parser.reset(reader, previousCommit.getTree());
        previousCommitParser = parser;
      }

      return this;
    }
  }
}
