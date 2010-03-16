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
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;
import org.obiba.core.util.StreamUtil;

public class OpalFileSystemTest {

  private FileObject fsLocalRoot;

  private FileObject fsFtpRoot;

  FakeFtpServer mockFtpServer;

  @Before
  public void setUp() throws FileSystemException {

    FileSystemManager fsm = VFS.getManager();
    FileObject vfsRoot = fsm.resolveFile("res:opal-file-system");
    fsLocalRoot = fsm.createVirtualFileSystem(vfsRoot);

    mockFtpServer = new FakeFtpServer();
    mockFtpServer.addUserAccount(new UserAccount("user", "password", "c:/"));

    FileSystem fileSystem = new WindowsFakeFileSystem();
    fileSystem.add(new DirectoryEntry("c:/temp"));
    fileSystem.add(new FileEntry("c:/temp/file1.txt"));
    fileSystem.add(new FileEntry("c:/temp/file2.txt", "this is the file content"));
    mockFtpServer.setFileSystem(fileSystem);

    mockFtpServer.start();

    vfsRoot = fsm.resolveFile("ftp://user:password@localhost:21/temp");
    fsFtpRoot = fsm.createVirtualFileSystem(vfsRoot);

  }

  @Test
  public void testLocalFile() throws FileSystemException {
    System.out.println(fsLocalRoot.resolveFile("temp.pem"));
    OpalFileSystem.getLocaleFile(fsLocalRoot.resolveFile("temp.pem"));
    Assert.assertTrue(OpalFileSystem.isLocalFile(fsLocalRoot.resolveFile("temp.pem")));
  }

  @Test
  public void testFtpFileNotLocalFile() throws FileSystemException {
    Assert.assertFalse(OpalFileSystem.isLocalFile(fsFtpRoot.resolveFile("file1.txt")));
  }

  @Test
  public void getLocalFileFromFtp() throws FileNotFoundException, IOException {
    Assert.assertFalse(OpalFileSystem.isLocalFile(fsFtpRoot.resolveFile("file2.txt")));
    File localFile = OpalFileSystem.getLocaleFile(fsFtpRoot.resolveFile("file2.txt"));
    List<String> lines = StreamUtil.readLines(new FileInputStream(localFile));
    Assert.assertEquals("this is the file content", lines.get(0));
  }

  @After
  public void cleanUp() {
    mockFtpServer.stop();
  }
}
