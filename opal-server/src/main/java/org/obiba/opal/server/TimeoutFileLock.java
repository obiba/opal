package org.obiba.opal.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class TimeoutFileLock {

  private static final Logger log = LoggerFactory.getLogger(TimeoutFileLock.class);

  private static final String LOCK_PATH = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "opal.lock";

  private static final int TIMEOUT_SECONDS = 30;

  private static FileLock lock;
  private static FileChannel channel;
  private static File lockFile;

  public static boolean setupLock() {
    log.info("Setting up lock file at {}", LOCK_PATH);
    lockFile = new File(LOCK_PATH);

    try {
      RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
      channel = raf.getChannel();

      long startTime = System.currentTimeMillis();
      while (true) {
        try {
          lock = channel.tryLock();
          if (lock != null) {
            break; // Got the lock
          }
        } catch (IOException e) {
          // Lock already held — retry after short sleep
        }

        if ((System.currentTimeMillis() - startTime) > TIMEOUT_SECONDS * 1000) {
          log.error("Failed to acquire lock within timeout: {}s", TIMEOUT_SECONDS);
          channel.close();
          System.exit(1);
        }

        Thread.sleep(200); // Small delay before retrying
      }

      // Optional: Write PID or timestamp
      raf.writeBytes("Locked by PID: " + ProcessHandle.current().pid() + "\n");

      // Register shutdown hook for cleanup
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          log.info("Releasing lock and deleting lock file...");
          if (lock != null && lock.isValid()) lock.release();
          if (channel != null && channel.isOpen()) channel.close();
          if (lockFile.exists()) lockFile.delete();
        } catch (IOException e) {
          log.error("Error while releasing lock", e);
        }
      }));

      return true;
    } catch (Exception e) {
      log.error("Error while setting up lock", e);
      return false;
    }
  }
}
