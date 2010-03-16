package org.obiba.opal.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileNotFoundException;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.DelegateFileObject;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.obiba.core.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class OpalFileSystem {

  private static final Logger log = LoggerFactory.getLogger(OpalFileSystem.class);

  private final FileObject root;

  public OpalFileSystem(String root) {

    Assert.hasText(root, "You must specify a root directory for the Opal File System.");

    try {
      FileSystemManager fsm = VFS.getManager();
      FileObject vfsRoot = fsm.resolveFile(root);

      Assert.isTrue(vfsRoot.isWriteable(), "The root of the Opal File System is not writable.  Please reconfigure the Opal File System with a writable root.");

      // This is similar to what chroot does. We obtain a "filesystem" that appears to be rooted at vfsRoot
      this.root = fsm.createVirtualFileSystem(vfsRoot);
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  public FileObject getRoot() {
    return root;
  }

  public static File getLocaleFile(FileObject virtualFile) {

    Assert.notNull(virtualFile, "A virtualFile is required.");

    boolean isLocalFile;
    try {
      isLocalFile = isLocalFile(virtualFile);
    } catch(FileNotFoundException e1) {
      return null;
    }

    try {
      if(isLocalFile) {
        return new File(virtualFile.getURL().toURI());
      } else {
        return convertVirtualFileToLocal(virtualFile);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

  }

  public static File convertVirtualFileToLocal(FileObject virtualFile) {

    Assert.notNull(virtualFile, "A virtualFile is required.");

    File localFile = null;
    InputStream virtualFileInputStream = null;
    FileOutputStream localFileOutputStream = null;
    try {

      if(virtualFile.getType() != FileType.FILE) {
        throw new RuntimeException("This FileObject (VFS) cannot be converted to a local File, because it is either a folder or represents a file that does not exist.");
      }

      FileName virtualFileName = virtualFile.getName();
      System.out.println(virtualFileName.getExtension());
      localFile = File.createTempFile("temp_local_vfs_", "." + virtualFileName.getExtension());

      localFileOutputStream = new FileOutputStream(localFile);
      virtualFileInputStream = virtualFile.getContent().getInputStream();
      StreamUtil.copy(virtualFileInputStream, localFileOutputStream);

    } catch(Exception couldNotConvertFileToLocal) {
      throw new RuntimeException("Failed to convert FileObject (VFS) to a local File", couldNotConvertFileToLocal);
    } finally {
      StreamUtil.silentSafeClose(virtualFileInputStream);
      StreamUtil.silentSafeClose(localFileOutputStream);
    }

    return localFile;
  }

  public static boolean isLocalFile(FileObject virtualFile) throws FileNotFoundException {

    Assert.notNull(virtualFile, "A virtualFile is required.");

    try {
      if(!virtualFile.exists()) {
        throw new FileNotFoundException("File not found : " + virtualFile);
      }
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }

    FileObject currentFile = virtualFile;
    while(true) {
      if(currentFile instanceof DelegateFileObject) {
        currentFile = ((DelegateFileObject) virtualFile).getDelegateFile();
      } else if(currentFile instanceof LocalFile) {
        return true;
      } else {
        return false;
      }
    }

  }

}
