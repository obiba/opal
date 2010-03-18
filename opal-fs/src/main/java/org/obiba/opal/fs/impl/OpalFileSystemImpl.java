package org.obiba.opal.fs.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.obiba.opal.fs.OpalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class OpalFileSystemImpl implements OpalFileSystem {

  private static final Logger log = LoggerFactory.getLogger(OpalFileSystem.class);

  private final FileObject root;

  private final String nativeRootURL;

  public OpalFileSystemImpl(String root) {

    Assert.hasText(root, "You must specify a root directory for the Opal File System.");

    try {
      FileSystemManager fsm = VFS.getManager();
      FileObject vfsRoot = fsm.resolveFile(root);
      nativeRootURL = vfsRoot.getURL().toString() + "/";

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

  public File getLocalFile(FileObject virtualFile) {
    Assert.notNull(virtualFile, "A virtualFile is required.");

    boolean isLocalFile;
    try {
      isLocalFile = isLocalFile(virtualFile);
    } catch(FileNotFoundException e1) {
      return null;
    }

    try {
      if(isLocalFile) {
        String virtualFileURL = virtualFile.getURL().toString();
        String nativeFileURL = virtualFileURL.replace(root.getURL().toString(), nativeRootURL);
        log.info("nativeRootURL: {}", nativeRootURL);
        log.info("nativeFileURL: {}", nativeFileURL);

        File file = new File(nativeFileURL.substring("file:///".length()));
        log.info("nativeFile exists: {}", file.exists());
        return file;
      } else {
        return convertVirtualFileToLocal(virtualFile);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  public File convertVirtualFileToLocal(FileObject virtualFile) {
    Assert.notNull(virtualFile, "A virtualFile is required.");

    File localFile = null;
    InputStream virtualFileInputStream = null;
    FileOutputStream localFileOutputStream = null;
    try {
      makeSureThatFileCanBeConverted(virtualFile);
      localFileOutputStream = new FileOutputStream(getLocalTempFile(virtualFile));
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

  private void makeSureThatFileCanBeConverted(FileObject virtualFile) throws FileSystemException {
    if(virtualFile.getType() != FileType.FILE) {
      throw new RuntimeException("This FileObject (VFS) cannot be converted to a local File, because it is either a folder or represents a file that does not exist.");
    }
  }

  private File getLocalTempFile(FileObject virtualFile) throws IOException {
    FileName virtualFileName = virtualFile.getName();
    return File.createTempFile("temp_local_vfs_", "." + virtualFileName.getExtension());
  }

  public boolean isLocalFile(FileObject virtualFile) throws FileNotFoundException {

    Assert.notNull(virtualFile, "A virtualFile is required.");

    // checkThatFileExist(virtualFile);

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

  private void checkThatFileExist(FileObject virtualFile) {
    try {
      if(!virtualFile.exists()) {
        throw new FileNotFoundException("File not found : " + virtualFile);
      }
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

}
