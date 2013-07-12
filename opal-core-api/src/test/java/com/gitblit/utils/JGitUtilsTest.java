package com.gitblit.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;

import com.gitblit.utils.JGitUtils;

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
    JGitUtils.cloneRepository(localRepo.getParentFile(), localRepo.getName(), remoteUrl, false, null);

    for(int i = 1; i < 10; i++) {
      pushChanges(i);
    }

    // TODO Get commits, diffs, restore etc.
  }

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

    git.commit().setAuthor("toto","dummy@obiba.org").setMessage("some comment " + n).call();
    git.push().setPushAll().setRemote("origin").call();
  }

  private File getTmpDirectory() throws IOException {
    // for java 6 compatibility
    File tmp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    tmp.delete();
    return tmp.getParentFile();
  }

}