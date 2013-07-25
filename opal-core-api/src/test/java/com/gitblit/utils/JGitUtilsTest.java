package com.gitblit.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;

//import org.gitective.core.BlobUtils;

public class JGitUtilsTest {

  private File targetDir;

  private String remoteUrl;

  private File localRepo;

  @Before
  public void init() throws IOException {
    File tmp = getTmpDirectory();
    targetDir = new File(tmp, "mytestrepo.git");
    remoteUrl = "file://" + targetDir.getAbsolutePath();
    localRepo = new File(tmp, "working-copy");
    FileUtil.delete(targetDir);
    FileUtil.delete(localRepo);
  }

  @After
  public void tearDown() {

  }

  @Test
  public void testCreate() throws Exception {
    Repository newRepo = new FileRepository(targetDir);
    newRepo.create(true);

    // Fetch or clone
    JGitUtils.cloneRepository(localRepo.getParentFile(), localRepo.getName(), remoteUrl, false,
        null);

    for(int i = 1; i < 10; i++) {
      pushChanges(i);
    }

    System.out.println("--- log ---");
    printLog();
//    System.out.println("--- diff ---");
//    printDiff(1);
//    System.out.println("--- content ---");
//    printContent();
    // TODO Get commits, diffs, restore etc.
  }

//  private void printContent() throws IOException {
//    Repository repo = new FileRepository(new File(localRepo, ".git"));
//
//    String content = BlobUtils.getHeadContent(repo, "myfile");
//    System.out.println(content);
//  }

//  private void printDiff(int level) throws IOException {
//    Repository repo = new FileRepository(new File(localRepo, ".git"));
//
//    ObjectId current = BlobUtils.getId(repo, "HEAD", "myfile");
//    ObjectId previous = BlobUtils.getId(repo, "HEAD~" + level, "myfile");
//
//    Collection<Edit> edit = BlobUtils.diff(repo, previous, current);
//    EditList editList = new EditList();
//    for (Edit e : edit) {
//      editList.add(e);
//      System.out.println(e);
//    }
//    DiffFormatter formatter = new DiffFormatter(System.out);
//    formatter.format(editList, new RawText(BlobUtils.getRawContent(repo, current, "myfile")), new RawText(BlobUtils.getRawContent(repo, previous, "myfile")));
//  }

  private void pushChanges(int n) throws IOException, GitAPIException {
    Git git = new Git(new FileRepository(new File(localRepo, ".git")));

    File myfile = new File(localRepo, "myfile");
    if(!myfile.exists()) myfile.createNewFile();
    FileWriter writer = new FileWriter(myfile);
    writer.append(n + ": hello world!");
    writer.flush();
    writer.close();
    //git.add().addFilepattern("myfile").call();
    git.add().addFilepattern(".").call();

    git.commit().setAuthor("toto","dummy@obiba.org").setMessage("some comment " + n).setCommitter("opal","opal@obiba.org").call();
    git.push().setPushAll().setRemote("origin").call();
  }

  private void printLog() throws IOException, GitAPIException {
    Git git = new Git(new FileRepository(new File(localRepo, ".git")));

    int count = 10;
    for (RevCommit revCommit : git.log().setMaxCount(count).call()) {
      System.out.println("name: " + revCommit.getName());
      System.out.println("  fullMessage: " + revCommit.getFullMessage());
      System.out.println("  author name: " + revCommit.getAuthorIdent().getName());
      System.out.println("  author email: " + revCommit.getAuthorIdent().getEmailAddress());
      System.out.println("  committer name: " + revCommit.getCommitterIdent().getName());
      System.out.println("  committer email: " + revCommit.getCommitterIdent().getEmailAddress());
      System.out.println("  commitTime: " + revCommit.getCommitTime());
      System.out.println("  authorDate: " + revCommit.getAuthorIdent().getWhen());
    }
  }

  private File getTmpDirectory() throws IOException {
    // for java 6 compatibility
    File tmp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    tmp.delete();
    return tmp.getParentFile();
  }

}