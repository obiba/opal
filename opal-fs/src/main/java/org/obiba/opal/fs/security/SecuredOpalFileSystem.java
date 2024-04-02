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

import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.fs.OpalFileSystem;

import com.google.common.base.Preconditions;

public class SecuredOpalFileSystem implements OpalFileSystem {

//  private static final Logger log = LoggerFactory.getLogger(OpalFileSystem.class);

  @NotNull
  private final OpalFileSystem delegate;

  private final Authorizer authorizer = new ShiroAuthorizer();

  public SecuredOpalFileSystem(@NotNull OpalFileSystem delegate) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(delegate != null, "delegate must not be null");
    this.delegate = delegate;
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
    return delegate.getLocalFile(virtualFile);
  }

  @Override
  public File resolveLocalFile(String virtualPath) {
    return delegate.resolveLocalFile(virtualPath);
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
