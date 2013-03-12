/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.fs.security;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.obiba.magma.security.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

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
        Arrays.asList(toSecuredFileObjects(selected.toArray(new FileObject[selected.size()]))));
  }

  @Override
  public FileObject[] findFiles(FileSelector selector) throws FileSystemException {
    return toSecuredFileObjects(super.findFiles(selector));
  }

  @Override
  public FileObject getChild(String name) throws FileSystemException {
    FileObject child = super.getChild(name);
    if(child == null) return null;
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
    if(isPermitted(getDecoratedFileObject(), "DELETE")) {
      return super.delete();
    }
    throw new FileSystemException("vfs.provider.local/delete-file.error", getName());
  }

  @Override
  public void moveTo(FileObject destFile) throws FileSystemException {
    FileObject sourceFile = getDecoratedFileObject();

    if(isPermitted(sourceFile, "DELETE")) {
      if(!(destFile instanceof SecuredFileObject)) {
        super.moveTo(destFile);
        return;
      }

      sourceFile.moveTo(((SecuredFileObject) destFile).getDecoratedFileObject());
      return;
    }
    throw new FileSystemException("vfs.provider.local/delete-file.error", getName());
  }

  private FileObject[] toSecuredFileObjects(FileObject... children) {
    if(children == null) return null;

    return Iterables.toArray(Iterables.transform(Arrays.asList(children), new Function<FileObject, FileObject>() {
      @Override
      public FileObject apply(@Nullable FileObject input) {
        return new SecuredFileObject(authorizer, input);
      }
    }), FileObject.class);
  }

  private boolean isPermitted(FileObject file, String method) {
    String permission = "magma:/files" + file.getName().getPath() + ":" + method;
    log.trace("checking permission {}", permission);
    return authorizer.isPermitted(permission);
  }
}
