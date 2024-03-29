/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.fs.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.cache.DefaultFilesCache;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.DelegateFileObject;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.obiba.opal.fs.OpalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

public class DefaultOpalFileSystem implements OpalFileSystem {

  private static final Logger log = LoggerFactory.getLogger(OpalFileSystem.class);

  private DefaultFileSystemManager fsm;

  @NotNull
  private final FileObject root;

  @NotNull
  private final String nativeRootURL;

  public static void deleteDirectoriesAndFilesInPath(Path aPath) throws IOException {
    Files.walkFileTree(aPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        Files.delete(path);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        Files.delete(path);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public DefaultOpalFileSystem(String fsRoot) {
    Assert.hasText(fsRoot, "You must specify a root directory for the Opal File System.");

    try {
      String rootWithPlaceholdersReplaced = Placeholders.replaceAll(fsRoot);
      log.info("Setting up Opal filesystem rooted at '{}'", rootWithPlaceholdersReplaced);
      fsm = new DefaultFileSystemManager();
      fsm.addProvider("file", new DefaultLocalFileProvider());
      fsm.addProvider("res", new ResourceFileProvider());
      fsm.setCacheStrategy(CacheStrategy.ON_RESOLVE);
      fsm.setFilesCache(new DefaultFilesCache());
      fsm.init();
      FileObject vfsRoot = fsm.resolveFile(rootWithPlaceholdersReplaced);

      if(!vfsRoot.exists()) {
        log.info("Opal File System does not exist. Trying to create it.");
        vfsRoot.createFolder();
      }
      nativeRootURL = vfsRoot.getURL() + "/";

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

  @Override
  public void close() {
    fsm.close();
  }

  @NotNull
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

  @Override
  public File resolveLocalFile(String virtualPath) {
    try {
      // note: does not ensure that file exists
      return getLocalFile(getRoot().resolveFile(virtualPath));
    } catch(FileSystemException e) {
      throw new IllegalArgumentException(e);
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

  private File convertVirtualFileToLocal(FileObject virtualFile) {
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
      try {
        if(virtualFileInputStream != null) virtualFileInputStream.close();
      } catch(IOException ignored) {
      }
      try {
        if(localFileOutputStream != null) localFileOutputStream.close();
      } catch(IOException ignored) {
      }
    }

  }

  private File getLocalTempFile(FileObject virtualFile) throws IOException {
    FileName virtualFileName = virtualFile.getName();
    return File.createTempFile("temp_local_vfs_", "." + virtualFileName.getExtension());
  }

  private boolean isLocalFile(@NotNull FileObject virtualFile) {
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
    MessageDigest digester;
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
