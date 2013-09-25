package org.obiba.opal.core.vcs.git.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class OpalGitDiffCommand extends OpalGitCommand<List<String>> {

  private String path;

  private String commitId;

  private int nthCommit = 1;

  public OpalGitDiffCommand(Repository repository) {
    super(repository);
  }

  public OpalGitDiffCommand addPath(String value) {
    path = value;
    return this;
  }

  public OpalGitDiffCommand addCommitId(String value) {
    commitId = value;
    return this;
  }

  public OpalGitDiffCommand addNthCommit(int value) {
    nthCommit = value;
    return this;
  }

  @Override
  public List<String> execute() {
    ObjectReader reader = repository.newObjectReader();
    List<String> diffEntries = new ArrayList<String>();

    try {
      RevCommit currentCommit = getCommitById(commitId);

      if(currentCommit == null) {
        throw new OpalGitException(String.format("There are no commit for commit id '%s'", commitId));
      }

      CanonicalTreeParser currentCommitParser = new CanonicalTreeParser();
      currentCommitParser.reset(reader, currentCommit.getTree());

      AbstractTreeIterator previousCommitParser = null;
      RevCommit previousCommit = getCommitById(OpalGitUtils.getNthCommitId(commitId, nthCommit));

      if(previousCommit == null) {
        previousCommitParser = new EmptyTreeIterator();
      } else {
        CanonicalTreeParser parser = new CanonicalTreeParser();
        parser.reset(reader, previousCommit.getTree());
        previousCommitParser = parser;
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      DiffFormatter df = new DiffFormatter(out);
      df.setRepository(repository);
      df.setDiffComparator(RawTextComparator.DEFAULT);
      df.setDetectRenames(true);

      if(!Strings.isNullOrEmpty(path)) {
        df.setPathFilter(PathFilter.create(path));
      }

      List<DiffEntry> diffs = df.scan(currentCommitParser, previousCommitParser);

      for(DiffEntry diffEntry : diffs) {
        df.format(diffEntry);
        diffEntry.getOldId();
        diffEntries.add(out.toString("UTF-8"));
        out.reset();
      }
    } catch(IOException e) {
      throw new OpalGitException(e.getMessage(), e);
    } finally {
      reader.release();
    }

    return diffEntries;
  }

}
