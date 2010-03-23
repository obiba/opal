/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;
import org.obiba.core.util.StreamUtil;
import org.obiba.opal.fs.impl.OpalFileSystemImpl;

public class OpalFileSystemTest {

  private OpalFileSystem fsLocal;

  private FileObject fsLocalRoot;

  private OpalFileSystem fsFtp;

  private FileObject fsFtpRoot;

  FakeFtpServer mockFtpServer;

  @Before
  public void setUp() throws FileSystemException {

    fsLocal = new OpalFileSystemImpl("res:opal-file-system");
    fsLocalRoot = fsLocal.getRoot();

    // MockFtpServer blocks the execution of this test under the unix environment, so this test
    // will be run on Windows until we find a solution...
    if(runningOsIsWindows()) {

      mockFtpServer = new FakeFtpServer();
      mockFtpServer.addUserAccount(new UserAccount("user", "password", "c:/"));

      FileSystem fileSystem = new WindowsFakeFileSystem();
      fileSystem.add(new DirectoryEntry("c:/temp"));
      fileSystem.add(new FileEntry("c:/temp/file1.txt"));
      fileSystem.add(new FileEntry("c:/temp/file2.txt", "this is the file content"));
      mockFtpServer.setFileSystem(fileSystem);

      mockFtpServer.start();

      fsFtp = new OpalFileSystemImpl("ftp://user:password@localhost:21");
      fsFtpRoot = fsFtp.getRoot();
    }

  }

  @Test
  public void testLocalFile() throws FileSystemException {
    System.out.println(fsLocal.getRoot().resolveFile("temp.pem"));
    fsLocal.getLocalFile(fsLocalRoot.resolveFile("temp.pem"));
    Assert.assertTrue(fsLocal.isLocalFile(fsLocalRoot.resolveFile("temp.pem")));
  }

  @Test
  public void testFtpFileNotLocalFile() throws FileSystemException {
    Assume.assumeTrue(runningOsIsWindows());
    Assert.assertFalse(fsFtp.isLocalFile(fsFtpRoot.resolveFile("/temp/file2.txt")));
  }

  @Test
  public void getLocalFileFromFtp() throws FileNotFoundException, IOException {
    Assume.assumeTrue(runningOsIsWindows());
    Assert.assertFalse(fsFtp.isLocalFile(fsFtpRoot.resolveFile("/temp/file2.txt")));
    File localFile = fsFtp.getLocalFile(fsFtpRoot.resolveFile("/temp/file2.txt"));
    List<String> lines = StreamUtil.readLines(new FileInputStream(localFile));
    Assert.assertEquals("this is the file content", lines.get(0));
  }

  private boolean runningOsIsWindows() {
    String osName = System.getProperty("os.name");
    return osName.toLowerCase().contains("windows");
  }

  @After
  public void cleanUp() {
    if(mockFtpServer != null) {
      mockFtpServer.stop();
    }
  }
}
