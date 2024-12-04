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

import com.google.common.collect.Lists;
import jakarta.annotation.Nullable;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.obiba.magma.security.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class SecuredFileObject extends DecoratedFileObject {

  private static final Logger log = LoggerFactory.getLogger(SecuredFileObject.class);

  private final Authorizer authorizer;

  SecuredFileObject(Authorizer authorizer, FileObject decoratedFileObject) {
    super(decoratedFileObject);
    this.authorizer = authorizer;
  }

  @Override
  public void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected)
      throws FileSystemException {
    super.findFiles(selector, depthwise,
        selected);

    List<FileObject> securedSelected = Lists.newArrayList();
    for (FileObject file : selected) {
      SecuredFileObject secured = new SecuredFileObject(authorizer, file);
      if (secured.getParent() == null || secured.getParent().isReadable())
        securedSelected.add(secured);
    }
    selected.clear();
    selected.addAll(securedSelected);
  }

  @Override
  public FileObject[] findFiles(FileSelector selector) throws FileSystemException {
    return toSecuredFileObjects(super.findFiles(selector));
  }

  @Nullable
  @Override
  public FileObject getChild(String name) throws FileSystemException {
    FileObject child = super.getChild(name);
    if (child == null) return null;
    return new SecuredFileObject(authorizer, child);
  }

  @Override
  public FileObject[] getChildren() throws FileSystemException {
    return toSecuredFileObjects(super.getChildren());
  }

  @Override
  public FileObject getParent() throws FileSystemException {
    FileObject parent = super.getParent();
    return parent == null ? null : new SecuredFileObject(authorizer, parent);
  }

  @Override
  public boolean isReadable() throws FileSystemException {
    return super.isReadable() && isPermitted(getDecoratedFileObject(), "GET");
  }

  @Override
  public boolean isWriteable() throws FileSystemException {
    return super.isWriteable() && isPermitted(getDecoratedFileObject(), "POST");
  }

  @Override
  public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
    return new SecuredFileObject(authorizer, super.resolveFile(name, scope));
  }

  @Override
  public FileObject resolveFile(String path) throws FileSystemException {
    return new SecuredFileObject(authorizer, super.resolveFile(path));
  }

  @Override
  public boolean delete() throws FileSystemException {
    if (isPermitted(getDecoratedFileObject(), "DELETE")) {
      return super.delete();
    }
    throw new FileSystemException("vfs.provider.local/delete-file.error", getName());
  }

  @Override
  public void moveTo(FileObject destFile) throws FileSystemException {
    FileObject sourceFile = getDecoratedFileObject();

    if (isPermitted(sourceFile, "DELETE")) {
      if (!(destFile instanceof SecuredFileObject)) {
        super.moveTo(destFile);
        return;
      }

      sourceFile.moveTo(((DecoratedFileObject) destFile).getDecoratedFileObject());
      return;
    }
    throw new FileSystemException("vfs.provider.local/delete-file.error", getName());
  }

  @Nullable
  private FileObject[] toSecuredFileObjects(FileObject... children) {
    if (children == null) return null;

    FileObject[] secured = new FileObject[children.length];
    for (int i = 0; i < children.length; i++) {
      secured[i] = new SecuredFileObject(authorizer, children[i]);
    }

    return secured;
  }

  private boolean isPermitted(FileObject file, String method) {
    String permission = "rest:/files" + file.getName().getPath() + ":" + method;
    log.trace("checking permission {}", permission);
    return authorizer.isPermitted(permission);
  }
}
