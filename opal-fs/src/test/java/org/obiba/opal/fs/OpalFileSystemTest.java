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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

import junit.framework.Assert;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

public class OpalFileSystemTest {

  private static final Logger log = LoggerFactory.getLogger(OpalFileSystemTest.class);

  private OpalFileSystem fsLocal;

  private FileObject fsLocalRoot;

  private OpalFileSystem fsFtp;

  private FileObject fsFtpRoot;

  FakeFtpServer mockFtpServer;

  @Before
  public void setUp() throws FileSystemException {

    fsLocal = new DefaultOpalFileSystem("res:opal-file-system");
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

      fsFtp = new DefaultOpalFileSystem("ftp://user:password@localhost:21");
      fsFtpRoot = fsFtp.getRoot();
    }

  }

  @Test
  public void testLocalFile() throws FileSystemException {
    log.info("file: {}", fsLocal.getRoot().resolveFile("temp.pem"));
    fsLocal.getLocalFile(fsLocalRoot.resolveFile("temp.pem"));
    Assert.assertTrue(fsLocal.isLocalFile(fsLocalRoot.resolveFile("temp.pem")));
  }

  @Test
  public void testFtpFileNotLocalFile() throws FileSystemException {
    assumeTrue(runningOsIsWindows());
    assertFalse(fsFtp.isLocalFile(fsFtpRoot.resolveFile("/temp/file2.txt")));
  }

  @Test
  public void getLocalFileFromFtp() throws FileNotFoundException, IOException {
    assumeTrue(runningOsIsWindows());
    assertFalse(fsFtp.isLocalFile(fsFtpRoot.resolveFile("/temp/file2.txt")));
    File localFile = fsFtp.getLocalFile(fsFtpRoot.resolveFile("/temp/file2.txt"));
    List<String> lines = CharStreams.readLines(new FileReader(localFile));
    assertEquals("this is the file content", lines.get(0));
  }

  private boolean runningOsIsWindows() {
    String osName = System.getProperty("os.name");
    return osName.toLowerCase().contains("windows");
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  @Test
  public void testGetObfuscatedPath() throws FileSystemException {
    FileObject file = fsLocalRoot.resolveFile("temp.pem");

    String obfuscatedFilePath = fsLocal.getObfuscatedPath(file);
    assertEquals("227379988e6f2c3e9eb87b1f7d7bd055", obfuscatedFilePath);

    file = fsLocalRoot.resolveFile("/test2/test21/temp2.pem");
    obfuscatedFilePath = fsLocal.getObfuscatedPath(file);
    assertEquals("269dd3644748e20182274c7a9de2ee6", obfuscatedFilePath);

    file = fsLocalRoot.resolveFile("/test2/test21/temp.pem");
    obfuscatedFilePath = fsLocal.getObfuscatedPath(file);
    assertEquals("30aa4ab41dbfeecb9e92d223bcaccb4", obfuscatedFilePath);

    file = fsLocalRoot.resolveFile("/test2/temp.pem");
    obfuscatedFilePath = fsLocal.getObfuscatedPath(file);
    assertEquals("508e41fae4e3de7a3045c2c9108f7fec", obfuscatedFilePath);

    file = fsLocalRoot.resolveFile("/reports/test2/test21/temp.pem");
    obfuscatedFilePath = fsLocal.getObfuscatedPath(file);
    assertEquals("f5f0f7be20e4b7eadca209fb071292b", obfuscatedFilePath);
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  @Test
  public void testResolveFileFromObfuscatedPath_PathIsResolved() throws FileSystemException {
    FileObject file = fsLocal.resolveFileFromObfuscatedPath(fsLocalRoot, "227379988e6f2c3e9eb87b1f7d7bd055");
    assertEquals("/temp.pem", file.getName().getPath());

    file = fsLocal.resolveFileFromObfuscatedPath(fsLocalRoot, "269dd3644748e20182274c7a9de2ee6");
    assertEquals("/test2/test21/temp2.pem", file.getName().getPath());

    file = fsLocal.resolveFileFromObfuscatedPath(fsLocalRoot, "30aa4ab41dbfeecb9e92d223bcaccb4");
    assertEquals("/test2/test21/temp.pem", file.getName().getPath());

    file = fsLocal.resolveFileFromObfuscatedPath(fsLocalRoot, "508e41fae4e3de7a3045c2c9108f7fec");
    assertEquals("/test2/temp.pem", file.getName().getPath());

  }

  @Test
  public void testResolveFileFromObfuscatedPath_PathIsNotResolved() throws FileSystemException {
    FileObject file = fsLocal.resolveFileFromObfuscatedPath(fsLocalRoot, "xxxx");
    Assert.assertNull(file);

  }

  @After
  public void cleanUp() {
    if(mockFtpServer != null) {
      mockFtpServer.stop();
    }
  }
}
