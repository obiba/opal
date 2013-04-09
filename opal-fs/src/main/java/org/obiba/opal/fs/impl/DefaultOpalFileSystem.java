package org.obiba.opal.fs.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.apache.commons.vfs2.provider.DelegateFileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.obiba.opal.fs.OpalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

public class DefaultOpalFileSystem implements OpalFileSystem {

  private static final Logger log = LoggerFactory.getLogger(OpalFileSystem.class);

  @Nonnull
  private final FileObject root;

  @Nonnull
  private final String nativeRootURL;

  public DefaultOpalFileSystem(String fsRoot) {
    Assert.hasText(fsRoot, "You must specify a root directory for the Opal File System.");

    try {
      String rootWithPlaceholdersReplaced = Placeholders.replaceAll(fsRoot);
      log.info("Setting up Opal filesystem rooted at '{}'", rootWithPlaceholdersReplaced);
      FileSystemManager fsm = VFS.getManager();
      FileObject vfsRoot = fsm.resolveFile(rootWithPlaceholdersReplaced);

      if(!vfsRoot.exists()) {
        log.info("Opal File System does not exist. Trying to create it.");
        vfsRoot.createFolder();
      }
      nativeRootURL = vfsRoot.getURL().toString() + "/";

      if(!vfsRoot.isReadable()) {
        log.error("Opal File System is not readable.  Please check your Opal File System configuration.");
        throw new RuntimeException(
            "Opal File System is not readable.  Please check your Opal File System configuration.");
      }
      if(!vfsRoot.isWriteable()) {
        log.error(
            "The root of the Opal File System is not writable.  Please reconfigure the Opal File System with a writable root.");
        throw new RuntimeException(
            "The root of the Opal File System is not writable.  Please reconfigure the Opal File System with a writable root.");
      }

      // This is similar to what chroot does. We obtain a "filesystem" that appears to be rooted at vfsRoot
      root = fsm.createVirtualFileSystem(vfsRoot);
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  @Override
  public FileObject getRoot() {
    return root;
  }

  @Override
  public File getLocalFile(FileObject virtualFile) {
    Assert.notNull(virtualFile, "A virtualFile is required.");
    try {
      if(isLocalFile(virtualFile)) {
        String virtualFileURL = virtualFile.getURL().toString();
        String nativeFileURL = virtualFileURL.replace(root.getURL().toString(), nativeRootURL);
        return new File(nativeFileURL.replaceFirst("[a-zA-Z]*[0-9]?://", ""));
      }
      return convertVirtualFileToLocal(virtualFile);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void makeSureThatFileCanBeConverted(FileObject virtualFile) {
    FileType virtualFileType;
    try {
      virtualFileType = virtualFile.getType();

      if(virtualFileType == FileType.FOLDER) {
        throw new RuntimeException(
            "This FileObject cannot be converted to a local File because it represents a folder in the VFS.");
      } else if(virtualFileType == FileType.IMAGINARY) {
        virtualFile.createFile();
      }

    } catch(FileSystemException e) {
      throw new RuntimeException("Unsuspected error : ", e);
    }
  }

  @Override
  public File convertVirtualFileToLocal(FileObject virtualFile) {
    Assert.notNull(virtualFile, "A virtualFile is required.");
    makeSureThatFileCanBeConverted(virtualFile);

    InputStream virtualFileInputStream = null;
    FileOutputStream localFileOutputStream = null;
    try {
      File localFile = getLocalTempFile(virtualFile);
      localFileOutputStream = new FileOutputStream(localFile);
      virtualFileInputStream = virtualFile.getContent().getInputStream();
      ByteStreams.copy(virtualFileInputStream, localFileOutputStream);
      return localFile;
    } catch(Exception couldNotConvertFileToLocal) {
      throw new RuntimeException("Failed to convert FileObject (VFS) to a local File", couldNotConvertFileToLocal);
    } finally {
      Closeables.closeQuietly(virtualFileInputStream);
      Closeables.closeQuietly(localFileOutputStream);
    }

  }

  private File getLocalTempFile(FileObject virtualFile) throws IOException {
    FileName virtualFileName = virtualFile.getName();
    return File.createTempFile("temp_local_vfs_", "." + virtualFileName.getExtension());
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  @Override
  public boolean isLocalFile(@Nonnull FileObject virtualFile) {
    Preconditions.checkNotNull(virtualFile);
    FileObject currentFile = virtualFile;
    while(true) {
      if(currentFile instanceof DelegateFileObject) {
        currentFile = ((DelegateFileObject) currentFile).getDelegateFile();
      } else if(currentFile instanceof DecoratedFileObject) {
        currentFile = ((DecoratedFileObject) currentFile).getDecoratedFileObject();
      } else {
        return currentFile instanceof LocalFile;
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

  private String convertBytesToString(byte... digestedBytes) {
    StringBuilder hexString = new StringBuilder();
    if(digestedBytes != null) {
      for(byte digestedByte : digestedBytes) {
        hexString.append(Integer.toHexString(0xFF & digestedByte));
      }
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

  @Nullable
  private FileObject searchFolder(FileObject folder, String obfuscatedPath) throws FileSystemException {
    FileObject matchingFile = null;
    for(FileObject file : folder.getChildren()) {
      if(file.getType() == FileType.FOLDER && file.getChildren().length > 0) {
        matchingFile = searchFolder(file, obfuscatedPath);
        if(matchingFile != null) break;
      } else {
        if(getObfuscatedPath(file).equals(obfuscatedPath)) {
          matchingFile = file;
          break;
        }
      }
    }
    return matchingFile;
  }
}
