package org.obiba.opal.r.service.tasks;


import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.service.security.CryptoService;
import org.obiba.opal.r.service.NoSuchRSessionException;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.FileReadROperation;
import org.obiba.opal.spi.r.FileWriteROperation;
import org.obiba.opal.spi.r.RRuntimeException;
import org.obiba.opal.spi.r.RScriptROperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class SubjectRSessions implements Iterable<RServerSession> {

  private static final Logger log = LoggerFactory.getLogger(SubjectRSessions.class);

  private static final String R_IMAGE_FILE = ".RData";

  private static final String R_IMAGE_FILE_ENC = ".RData.enc";

  private final CryptoService cryptoService;

  private final List<RServerSession> rSessions = Collections.synchronizedList(new ArrayList<RServerSession>());

  public SubjectRSessions(CryptoService cryptoService) {
    this.cryptoService = cryptoService;
  }

  public void saveRSession(String rSessionId, String saveId) {
    RServerSession rSession = getRSession(rSessionId);
    // make sure the session storage folder is empty
    File store = rSession.getWorkspace(saveId);
    File[] files = store.listFiles();
    if (files != null) {
      Lists.newArrayList(files).forEach(file -> {
        try {
          FileUtil.delete(file);
        } catch (IOException e) {
          // ignore
        }
      });
    }
    saveRSessionFiles(rSession, saveId);
    saveRSessionImage(rSession, saveId);
  }

  public void restoreRSession(String rSessionId, String restoreId) {
    RServerSession rSession = getRSession(rSessionId);
    restoreSessionImage(rSession, restoreId);
    restoreSessionFiles(rSession, restoreId);
  }

  public void removeRSession(String rSessionId) {
    removeRSession(getRSession(rSessionId));
  }

  public void removeRSession(RServerSession rSession) {
    try {
      rSession.close();
      rSessions.remove(rSession);
    } catch (Exception e) {
      log.warn("Failed closing R session: {}", rSession.getId(), e);
    }
  }

  public void removeRSessions(String clusterName, String serverName) {
    List<RServerSession> sessionsToRemove = rSessions.stream()
        .filter(s -> clusterName.equals(s.getProfile().getCluster()) && serverName.equals(s.getRServerServiceName()))
        .toList();
    for (RServerSession rSession : sessionsToRemove) {
      try {
        removeRSession(rSession);
      } catch (Exception e) {
        log.warn("Failed closing R session: {}", rSession.getId(), e);
      }
    }
  }

  public void removeRSessions() {
    for (RServerSession rSession : rSessions) {
      try {
        rSession.close();
      } catch (Exception e) {
        log.warn("Failed closing R session: {}", rSession.getId(), e);
      }
    }
    rSessions.clear();
  }

  public void clean() {
    List<RServerSession> toRemove = Lists.newArrayList();
    for (RServerSession rSession : rSessions) {
      if (!rSession.isPending() && rSession.isClosed()) {
        log.info("R session {} has been closed, removing it from user R sessions", rSession.getId());
        toRemove.add(rSession);
      }
    }
    rSessions.removeAll(toRemove);
  }

  private void saveRSessionImage(RServerSession rSession, String saveId) {
    // then save the memory image
    String rscript = "base::save.image()";
    RScriptROperation rop = new RScriptROperation(rscript, false);
    rSession.execute(rop);
    // clean legacy
    File destination = new File(rSession.getWorkspace(saveId), R_IMAGE_FILE);
    if (destination.exists()) destination.delete();
    destination = new File(rSession.getWorkspace(saveId), R_IMAGE_FILE_ENC);
    if (destination.exists()) destination.delete();
    try {
      FileReadROperation readop = new FileReadROperation(R_IMAGE_FILE, cryptoService.newCipherOutputStream(new FileOutputStream(destination)));
      rSession.execute(readop);
    } catch (FileNotFoundException e) {
      throw new RRuntimeException(e);
    }
  }

  private void restoreSessionImage(RServerSession rSession, String restoreId) {
    File source = new File(rSession.getWorkspace(restoreId), R_IMAGE_FILE_ENC);
    if (!source.exists()) {
      // legacy
      source = new File(rSession.getWorkspace(restoreId), R_IMAGE_FILE);
    }
    if (!source.exists()) return;

    try {
      FileWriteROperation writeop = null;
      if (R_IMAGE_FILE_ENC.equals(source.getName())) {
        writeop = new FileWriteROperation(R_IMAGE_FILE, cryptoService.newCipherInputStream(new FileInputStream(source)));
      } else {
        // legacy
        writeop = new FileWriteROperation(R_IMAGE_FILE, source);
      }
      rSession.execute(writeop);
      String rscript = String.format("base::load('%s')", R_IMAGE_FILE);
      RScriptROperation rop = new RScriptROperation(rscript, false);
      rSession.execute(rop);
    } catch (FileNotFoundException e) {
      throw new RRuntimeException(e);
    }
  }

  private void saveRSessionFiles(RServerSession rSession, String saveId) {
    rSession.saveRSessionFiles(saveId);
  }

  private void restoreSessionFiles(RServerSession rSession, String restoreId) {
    File source = rSession.getWorkspace(restoreId);
    FileUtils.listFiles(source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).forEach(file -> {
      String destination = file.getAbsolutePath().replace(source.getAbsolutePath(), "");
      if (destination.startsWith("/")) destination = destination.substring(1);
      if (!R_IMAGE_FILE_ENC.equals(destination) && !R_IMAGE_FILE.equals(destination)) {
        if (destination.contains("/")) {
          // make sure destination directory exists
          String rscript = String.format("base::dir.create('%s', showWarnings=FALSE, recursive=TRUE)", destination.substring(0, destination.lastIndexOf("/")));
          RScriptROperation rop = new RScriptROperation(rscript, false);
          rSession.execute(rop);
        }
        FileWriteROperation writeop = new FileWriteROperation(destination, file);
        rSession.execute(writeop);
      }
    });
  }

  public boolean hasRSession(String rSessionId) {
    for (RServerSession rs : rSessions) {
      if (rs.getId().equals(rSessionId)) {
        return true;
      }
    }
    return false;
  }

  public void addRSession(RServerSession rSession) {
    rSessions.add(rSession);
  }

  public RServerSession getRSession(String rSessionId) {
    for (RServerSession rs : rSessions) {
      if (rs.getId().equals(rSessionId)) {
        return rs;
      }
    }
    throw new NoSuchRSessionException(rSessionId);
  }

  @Override
  public Iterator<RServerSession> iterator() {
    return rSessions.iterator();
  }

}
