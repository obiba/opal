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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
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
    this.opalfs = runtime.getFileSystem();
    this.user = user;
  }

  @Override
  public SshFile getFile(String file) {
    FileObject fo = resolve(file);
    return new OpalFsSshFile(fo.getName().getPath(), opalfs.getLocalFile(fo), user);
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
   * It may be preferable to implement SshFile on top of FileObject instead of the native file system file.
   */
  private class OpalFsSshFile extends NativeSshFile {

    private final boolean isRoot;

    protected OpalFsSshFile(String fileName, File file, String userName) {
      super(fileName, file, userName);
      this.isRoot = opalfs.getLocalFile(opalfs.getRoot()).equals(file);
    }

    @Override
    public SshFile getParentFile() {
      return isRoot() ? this : super.getParentFile();
    }

    private boolean isRoot() {
      return isRoot;
    }
  }

  /**
   * Implementation on top of FileObject. Not used because some methods are not implemented correctly (getOutputStream,
   * truncate)
   */
  private class FileObjectSshFile implements SshFile {

    private final FileObject file;

    private FileObjectSshFile(FileObject fo) {
      this.file = fo;
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
      InputStream is = this.file.getContent().getInputStream();
      is.skip(offset);
      return is;
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
      // TODO implement offset writing
      return this.file.getContent().getOutputStream();
    }

    @Override
    public boolean delete() {
      try {
        return this.file.delete();
      } catch(FileSystemException e) {
        return false;
      }
    }

    @Override
    public boolean doesExist() {
      try {
        return this.file.exists();
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getAbsolutePath() {
      return this.file.getName().getPath();
    }

    @Override
    public long getLastModified() {
      try {
        return this.file.getContent().getLastModifiedTime();
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getName() {
      return this.file.getName().getBaseName();
    }

    @Override
    public SshFile getParentFile() {
      try {
        return new FileObjectSshFile(this.file.getParent() == null ? opalfs.getRoot() : this.file.getParent());
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public long getSize() {
      try {
        return this.file.getType() == FileType.FILE ? this.file.getContent().getSize() : 0;
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void handleClose() throws IOException {

    }

    @Override
    public boolean isDirectory() {
      try {
        return this.file.getType() == FileType.FOLDER;
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isFile() {
      try {
        return this.file.getType() == FileType.FILE;
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isReadable() {
      try {
        return this.file.isReadable();
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isRemovable() {
      try {
        return this.file.getParent().isWriteable();
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isWritable() {
      try {
        return this.file.isWriteable();
      } catch(FileSystemException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public List<SshFile> listSshFiles() {
      try {
        if(this.file.getType() == FileType.FOLDER) {
          List<FileObject> children = Arrays.asList(this.file.getChildren());
          Collections.sort(children, new Comparator<FileObject>() {

            @Override
            public int compare(FileObject lhs, FileObject rhs) {
              return lhs.getName().compareTo(rhs.getName());
            }
          });

          return ImmutableList.copyOf(Iterables.transform(children, new Function<FileObject, SshFile>() {

            @Override
            public SshFile apply(FileObject from) {
              return new FileObjectSshFile(from);
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
        this.file.createFolder();
        return true;
      } catch(FileSystemException e) {
        return false;
      }
    }

    @Override
    public boolean move(SshFile destination) {
      try {
        this.file.moveTo(((FileObjectSshFile) destination).file);
        return true;
      } catch(FileSystemException e) {
        return false;
      }
    }

    @Override
    public boolean setLastModified(long time) {
      try {
        this.file.getContent().setLastModifiedTime(time);
        return true;
      } catch(FileSystemException e) {
        return false;
      }
    }

    @Override
    public void truncate() throws IOException {
      this.file.getContent().getOutputStream().close();
    }
  }

}
