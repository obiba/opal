/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.fs.security;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.authz.UnauthorizedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obiba.magma.security.Authorizer;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * The file ACL is enforced on every way of reading a file's bytes, not only on the /files web service: the native
 * handle and the VFS content of a file the subject cannot read are refused, while metadata and write destinations
 * stay available.
 */
public class SecuredOpalFileSystemTest {

  private static final String READABLE = "/home/alice/a.csv";

  private static final String UNREADABLE = "/home/bob/b.csv";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private SecuredOpalFileSystem fs;

  @Before
  public void setUp() throws IOException {
    Path root = tmp.getRoot().toPath();
    Files.createDirectories(root.resolve("home/alice"));
    Files.createDirectories(root.resolve("home/bob"));
    Files.writeString(root.resolve("home/alice/a.csv"), "alice");
    Files.writeString(root.resolve("home/bob/b.csv"), "bob");
    // alice's subject: readable is anything under her own home
    fs = new SecuredOpalFileSystem(new DefaultOpalFileSystem(tmp.getRoot().getAbsolutePath()), new PrefixAuthorizer("rest:/files/home/alice"));
  }

  @Test
  public void localFileOfAReadableFileIsHandedOut() throws IOException {
    File file = fs.getLocalFile(resolve(READABLE));

    assertThat(Files.readString(file.toPath())).isEqualTo("alice");
  }

  @Test(expected = UnauthorizedException.class)
  public void localFileOfAnUnreadableFileIsRefused() throws FileSystemException {
    fs.getLocalFile(resolve(UNREADABLE));
  }

  @Test(expected = UnauthorizedException.class)
  public void localFileResolvedByPathIsCheckedToo() {
    fs.resolveLocalFile(UNREADABLE);
  }

  @Test
  public void localFileOfAFileThatDoesNotExistYetIsHandedOut() throws FileSystemException {
    // a write destination: nothing to read, writability is the caller's check
    File file = fs.getLocalFile(resolve("/home/bob/new.csv"));

    assertThat(file).doesNotExist();
    assertThat(file.getName()).isEqualTo("new.csv");
  }

  @Test
  public void localFileOfAnUnreadableFolderIsHandedOut() throws FileSystemException {
    assertThat(fs.getLocalFile(resolve("/home/bob"))).isDirectory();
  }

  @Test
  public void contentOfAReadableFileCanBeRead() throws IOException {
    FileContent content = resolve(READABLE).getContent();

    assertThat(content.getString(StandardCharsets.UTF_8)).isEqualTo("alice");
    assertThat(new String(content.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("alice");
  }

  @Test(expected = FileSystemException.class)
  public void inputStreamOfAnUnreadableFileIsRefused() throws FileSystemException {
    resolve(UNREADABLE).getContent().getInputStream();
  }

  @Test(expected = FileSystemException.class)
  public void bytesOfAnUnreadableFileAreRefused() throws IOException {
    resolve(UNREADABLE).getContent().getByteArray();
  }

  @Test(expected = FileSystemException.class)
  public void copyOfAnUnreadableFileIsRefused() throws IOException {
    resolve(UNREADABLE).getContent().write(new ByteArrayOutputStream());
  }

  @Test
  public void metadataOfAnUnreadableFileStaysAvailable() throws FileSystemException {
    // folder listings show the entries of a readable folder whatever their own permissions
    FileContent content = resolve(UNREADABLE).getContent();

    assertThat(content.getSize()).isEqualTo(3);
    assertThat(content.getLastModifiedTime()).isGreaterThan(0);
  }

  private FileObject resolve(String path) throws FileSystemException {
    return fs.getRoot().resolveFile(path);
  }

  /**
   * Grants every permission whose string starts with the given prefix.
   */
  private static class PrefixAuthorizer implements Authorizer {

    private final String prefix;

    private PrefixAuthorizer(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public boolean isPermitted(String permission) {
      return permission.startsWith(prefix);
    }

    @Override
    public <V> V silentSudo(Callable<V> callable) {
      try {
        return callable.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public <V> V sudo(Callable<V> callable) throws Exception {
      return callable.call();
    }
  }
}
