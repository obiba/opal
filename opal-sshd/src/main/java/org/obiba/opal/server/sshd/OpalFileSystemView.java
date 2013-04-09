/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.sshd;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.filesystem.NativeSshFile;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class OpalFileSystemView implements FileSystemView {

  private final OpalFileSystem opalfs;

  private final String user;

  public OpalFileSystemView(OpalRuntime runtime, String user) {
    opalfs = runtime.getFileSystem();
    this.user = user;
  }

  @Override
  public SshFile getFile(String file) {
    FileObject fo = resolve(file);
    return new FileObjectSshFile(fo, user);
  }

  @Override
  public SshFile getFile(SshFile baseDir, String file) {
    return getFile(new File(baseDir.getAbsolutePath(), file).getAbsolutePath());
  }

  private FileObject resolve(String file) {
    FileObject resolved = null;
    try {
      resolved = opalfs.getRoot().resolveFile(file, NameScope.DESCENDENT_OR_SELF);
    } catch(FileSystemException e) {
      resolved = opalfs.getRoot();
    }
    return resolved;
  }

  /**
   * Implementation on top of FileObject.
   */
  @SuppressWarnings("unused")
  private class FileObjectSshFile extends NativeSshFile {

    private final FileObject file;

    private FileObjectSshFile(FileObject fo, String userName) {
      super(fo.getName().getPath(), opalfs.getLocalFile(fo), userName);
      file = fo;
    }

    @Override
    public SshFile getParentFile() {
      try {
        return new FileObjectSshFile(file.getParent() == null ? opalfs.getRoot() : file.getParent(), getOwner());
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isReadable() {
      try {
        return file.isReadable();
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isRemovable() {
      try {
        // TODO not enough: check DELETE permission on corresponding resource
        return file.getParent().isWriteable();
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isWritable() {
      try {
        return file.isWriteable();
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public List<SshFile> listSshFiles() {
      try {
        if(file.getType() == FileType.FOLDER) {
          List<FileObject> children = Arrays.asList(file.getChildren());
          Collections.sort(children, new Comparator<FileObject>() {

            @Override
            @SuppressWarnings("unchecked")
            public int compare(FileObject lhs, FileObject rhs) {
              return lhs.getName().compareTo(rhs.getName());
            }
          });

          return ImmutableList.copyOf(Iterables.transform(children, new Function<FileObject, SshFile>() {

            @Override
            public SshFile apply(FileObject from) {
              return new FileObjectSshFile(from, getOwner());
            }
          }));
        }
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
      return null;
    }

    @Override
    public boolean mkdir() {
      try {
        file.createFolder();
        return true;
      } catch(FileSystemException e) {
        return false;
      }
    }

    @Override
    public boolean move(SshFile destination) {
      try {
        file.moveTo(((FileObjectSshFile) destination).file);
        return true;
      } catch(FileSystemException e) {
        return false;
      }
    }

    @Override
    public boolean delete() {
      try {
        return file.delete();
      } catch(FileSystemException e) {
        return false;
      }
    }

  }

}
