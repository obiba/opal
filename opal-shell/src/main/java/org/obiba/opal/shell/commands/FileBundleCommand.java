package org.obiba.opal.shell.commands;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.core.util.ZipBuilder;
import org.obiba.opal.shell.commands.options.FileBundleCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CommandUsage(description = "Prepare a file bundle.",
    syntax = "Syntax: file-bundle --path PATH[,PATH,...] [--password PASSWORD]")
public class FileBundleCommand extends AbstractOpalRuntimeDependentCommand<FileBundleCommandOptions>
    implements ResultCapable<FileCommandResult> {

  private static final Logger log = LoggerFactory.getLogger(FileBundleCommand.class);

  private final String WORK_DIR = System.getProperty("OPAL_HOME") + File.separator + "work" + File.separator + "fs";

  /** Set after a successful execution; {@code null} until then. */
  private FileCommandResult result;

  @Override
  public boolean hasResult() {
    return result != null;
  }

  @Override
  public FileCommandResult getResult() {
    return result;
  }

  @Override
  public int execute() {
    try {
      List<String> paths = options.getPaths();
      if (paths == null || paths.isEmpty()) {
        getShell().printf("No paths specified%n");
        return 1;
      }

      // Build the VFS-ACL filter once; applies to every entry in every file/folder
      FileObject root = getFileSystemRoot();
      File localRoot = getLocalFile(root);
      FileFilter aclFilter = pathname -> {
        try {
          String relativePath = pathname.getAbsolutePath().replace(localRoot.getAbsolutePath(), "");
          FileObject fo = root.resolveFile(relativePath);
          return fo.isReadable();
        } catch (FileSystemException e) {
          return false;
        }
      };

      // Validate all paths and collect their local File references
      List<File> localFiles = new ArrayList<>();
      for (String path : paths) {
        FileObject fileObject = getFile(path);
        if (!fileObject.exists()) {
          getShell().printf("Path does not exist: %s%n", path);
          return 1;
        }
        if (!fileObject.isReadable()) {
          getShell().printf("Path is not readable: %s%n", path);
          return 1;
        }
        localFiles.add(getLocalFile(fileObject));
      }

      String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
      File outDir = new File(WORK_DIR, timestamp);
      outDir.mkdirs();

      String password = options.isPassword() ? options.getPassword() : null;

      // Single path: keep current naming and use the entry's parent as zip base.
      // Multiple paths: name the bundle and use the VFS root as base so full
      // paths are preserved inside the archive (avoiding name collisions).
      boolean singlePath = localFiles.size() == 1;
      String zipName = singlePath
          ? localFiles.get(0).getName() + ".zip"
          : "bundle_" + timestamp + ".zip";
      File outputFile = new File(outDir, zipName);

      File zipBase = singlePath ? localFiles.get(0).getParentFile() : localRoot;
      ZipBuilder builder = ZipBuilder.newBuilder(outputFile)
          .base(zipBase)
          .password(password)
          .compressed();
      for (File localFile : localFiles) {
        builder.put(localFile, aclFilter);
      }
      builder.build();

      log.info("File bundle created: {}", outputFile.getAbsolutePath());
      getShell().printf("File bundle created: %s%n", outputFile.getAbsolutePath());
      result = new FileCommandResult(outputFile, "application/zip");
      return 0;
    } catch (Exception e) {
      log.error("Failed to create file bundle", e);
      getShell().printf("Failed to create file bundle: %s%n", e.getMessage());
      return 1;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("file-bundle");
    if (options.isPaths()) {
      options.getPaths().forEach(p -> sb.append(" --path ").append(p));
    }
    if (options.isPassword()) {
      sb.append(" --password *****");
    }
    return sb.toString();
  }
}
