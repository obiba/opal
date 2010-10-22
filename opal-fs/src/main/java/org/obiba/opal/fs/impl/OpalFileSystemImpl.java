package org.obiba.opal.fs.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.vfs.FileName;
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

      Assert.isTrue(vfsRoot.isReadable(), "Opal File System is not readable.  Please check your Opal File System configuration.");
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
    try {
      if(isLocalFile(virtualFile)) {
        String virtualFileURL = virtualFile.getURL().toString();
        String nativeFileURL = virtualFileURL.replace(root.getURL().toString(), nativeRootURL);

        log.info("nativeRootURL: {}", nativeRootURL);
        log.info("nativeFileURL: {}", nativeFileURL);

        File file = new File(nativeFileURL.replaceFirst("[a-zA-Z]*[0-9]?://", ""));
        return file;
      } else {
        return convertVirtualFileToLocal(virtualFile);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void makeSureThatFileCanBeConverted(FileObject virtualFile) {
    FileType virtualFileType;
    try {
      virtualFileType = virtualFile.getType();

      if(virtualFileType == FileType.FOLDER) {
        throw new RuntimeException("This FileObject cannot be converted to a local File because it represents a folder in the VFS.");
      } else if(virtualFileType == FileType.IMAGINARY) {
        virtualFile.createFile();
      }

    } catch(FileSystemException e) {
      throw new RuntimeException("Unsuspected error : ", e);
    }
  }

  public File convertVirtualFileToLocal(FileObject virtualFile) {
    Assert.notNull(virtualFile, "A virtualFile is required.");
    makeSureThatFileCanBeConverted(virtualFile);

    InputStream virtualFileInputStream = null;
    FileOutputStream localFileOutputStream = null;
    try {
      File localFile = getLocalTempFile(virtualFile);
      localFileOutputStream = new FileOutputStream(localFile);
      virtualFileInputStream = virtualFile.getContent().getInputStream();
      StreamUtil.copy(virtualFileInputStream, localFileOutputStream);
      return localFile;
    } catch(Exception couldNotConvertFileToLocal) {
      throw new RuntimeException("Failed to convert FileObject (VFS) to a local File", couldNotConvertFileToLocal);
    } finally {
      StreamUtil.silentSafeClose(virtualFileInputStream);
      StreamUtil.silentSafeClose(localFileOutputStream);
    }

  }

  private File getLocalTempFile(FileObject virtualFile) throws IOException {
    FileName virtualFileName = virtualFile.getName();
    return File.createTempFile("temp_local_vfs_", "." + virtualFileName.getExtension());
  }

  public boolean isLocalFile(FileObject virtualFile) {

    Assert.notNull(virtualFile, "A virtualFile is required.");

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

  @Override
  public String getObfuscatedPath(FileObject virtualFile) {
    return obfuscate(virtualFile.getName().getPath());
  }

  private String obfuscate(String stringToObfuscate) {
    return convertBytesToString(getDigester().digest(stringToObfuscate.getBytes()));
  }

  private String convertBytesToString(byte[] digestedBytes) {
    StringBuilder hexString = new StringBuilder();
    for(int i = 0; i < digestedBytes.length; i++) {
      hexString.append(Integer.toHexString(0xFF & digestedBytes[i]));
    }
    return hexString.toString();
  }

  private MessageDigest getDigester() {
    MessageDigest digester = null;
    try {
      digester = MessageDigest.getInstance("MD5");
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException("Cannot find the specified digesting algorithm", e);
    }
    return digester;
  }

  @Override
  public FileObject resolveFileFromObfuscatedPath(FileObject baseFolder, String obfuscatedPath) {
    try {
      return searchFolder(baseFolder, obfuscatedPath);
    } catch(FileSystemException e) {
      throw new RuntimeException("Unsuspected error : ", e);
    }

  }

  private FileObject searchFolder(FileObject folder, String obfuscatedPath) throws FileSystemException {

    FileObject matchingFile = null;
    for(FileObject file : folder.getChildren()) {

      if(file.getType().equals(FileType.FOLDER) && file.getChildren().length > 0) {
        matchingFile = searchFolder(file, obfuscatedPath);
      }

      if(obfuscate(file.getName().getPath()).equals(obfuscatedPath)) {
        matchingFile = file;
        break;
      }

    }

    return matchingFile;
  }
}
