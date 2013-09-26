package org.obiba.opal.core.vcs;

import java.util.Date;
import java.util.List;

public class CommitInfo {
  private String author;
  private Date date;
  private String comment;
  private String commitId;
  private List<String> diffEntries;

  public String getAuthor() {
    return author;
  }

  public Date getDate() {
    return date;
  }

  public String getComment() {
    return comment;
  }

  public String getCommitId() {
    return commitId;
  }

  public List<String> getDiffEntries() {
    return diffEntries;
  }

  public String toString() {
    return String.format("CommitInfo Id: %s, Author: %s, Date: %s\n%s", commitId, author, date, comment);
  }

  public static class Builder {
    private String author;
    private Date date;
    private String comment;
    private String commitId;
    private List<String> diffEntries;

    public static Builder createFromObject(CommitInfo commitInfo) {
        return new Builder().setAuthor(commitInfo.author).setComment(commitInfo.comment).setCommitId(commitInfo.commitId).setDate(commitInfo.date)
            .setDiffEntries(commitInfo.diffEntries);
    }

    public Builder setAuthor(String value) {
      author = value;
      return this;
    }

    public Builder setDate(Date value) {
      date = value;
      return this;
    }

    public Builder setComment(String value) {
      comment = value;
      return this;
    }

    public Builder setCommitId(String value) {
      commitId = value;
      return this;
    }

    public Builder setDiffEntries(List<String> value) {
      diffEntries = value;
      return this;
    }

    public CommitInfo build() {
      CommitInfo commitInfo = new CommitInfo();
      commitInfo.author = author;
      commitInfo.date = date;
      commitInfo.comment = comment;
      commitInfo.commitId = commitId;
      commitInfo.diffEntries = diffEntries;

      return commitInfo;
    }
  }
}
