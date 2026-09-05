/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.fs.security;

import java.io.File;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.shiro.authz.UnauthorizedException;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.fs.OpalFileSystem;

import com.google.common.base.Preconditions;

public class SecuredOpalFileSystem implements OpalFileSystem {

//  private static final Logger log = LoggerFactory.getLogger(OpalFileSystem.class);

  @NotNull
  private final OpalFileSystem delegate;

  private final Authorizer authorizer;

  public SecuredOpalFileSystem(@NotNull OpalFileSystem delegate) {
    this(delegate, new ShiroAuthorizer());
  }

  public SecuredOpalFileSystem(@NotNull OpalFileSystem delegate, @NotNull Authorizer authorizer) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(delegate != null, "delegate must not be null");
    Preconditions.checkArgument(authorizer != null, "authorizer must not be null");
    this.delegate = delegate;
    this.authorizer = authorizer;
  }

  @Override
  public void close() {
    delegate.close();
  }

  @NotNull
  @Override
  public FileObject getRoot() {
    return new SecuredFileObject(authorizer, delegate.getRoot());
  }

  @Override
  public File getLocalFile(FileObject virtualFile) {
    checkReadable(virtualFile);
    return delegate.getLocalFile(virtualFile);
  }

  @Override
  public File resolveLocalFile(String virtualPath) {
    try {
      return getLocalFile(getRoot().resolveFile(virtualPath));
    } catch (FileSystemException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * A native handle escapes every later check, so an existing regular file is only handed out when the current
   * subject may read it. Folders and files that do not exist yet are export and upload destinations: whether they
   * may be written is for their callers to check, and there is nothing to read from them.
   */
  private void checkReadable(FileObject virtualFile) {
    FileObject secured = virtualFile instanceof SecuredFileObject ? virtualFile : new SecuredFileObject(authorizer, virtualFile);
    try {
      if (secured.getType() == FileType.FILE && !secured.isReadable()) {
        throw new UnauthorizedException("File cannot be read: " + secured.getName().getPath());
      }
    } catch (FileSystemException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String getObfuscatedPath(FileObject virtualFile) {
    return delegate.getObfuscatedPath(virtualFile);
  }

  @Override
  public FileObject resolveFileFromObfuscatedPath(FileObject baseFolder, String obfuscatedPath) {
    return new SecuredFileObject(authorizer, delegate.resolveFileFromObfuscatedPath(baseFolder, obfuscatedPath));
  }
}
