package org.obiba.opal.shell.commands;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.shell.commands.options.FileBundleCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

@CommandUsage(description = "Prepare a file bundle.",
    syntax = "Syntax: file-bundle --path PATH [--password PASSWORD]")
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
      String path = options.getPath();
      FileObject fileObject = getFile(path);

      if (!fileObject.exists()) {
        getShell().printf("Path does not exist: %s%n", path);
        return 1;
      }
      if (!fileObject.isReadable()) {
        getShell().printf("Path is not readable: %s%n", path);
        return 1;
      }

      File localFile = getLocalFile(fileObject);
      String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
      File outDir = new File(WORK_DIR, timestamp);
      outDir.mkdirs();

      String password = options.isPassword() ? options.getPassword() : null;
      String zipName = localFile.getName() + ".zip";
      File outputFile;

      if (fileObject.getType() == FileType.FILE) {
        outputFile = FileUtil.zip(localFile, new File(outDir, zipName), password);
      } else {
        // folder: filter entries by VFS read access (applies ACLs)
        FileObject root = getFileSystemRoot();
        File localRoot = getLocalFile(root);
        FileFilter filter = pathname -> {
          try {
            String relativePath = pathname.getAbsolutePath().replace(localRoot.getAbsolutePath(), "");
            FileObject fo = root.resolveFile(relativePath);
            return fo.isReadable();
          } catch (FileSystemException e) {
            return false;
          }
        };
        outputFile = FileUtil.zip(localFile, filter, new File(outDir, zipName), password);
      }

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
    sb.append(" --path ").append(options.getPath());
    if (options.isPassword()) {
      sb.append(" --password *****");
    }
    return sb.toString();
  }
}
