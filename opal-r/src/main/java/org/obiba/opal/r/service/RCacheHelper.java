package org.obiba.opal.r.service;

import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.service.security.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class RCacheHelper {

  private static final Logger log = LoggerFactory.getLogger(RCacheHelper.class);

  private static final String R_CACHE_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "R" + File.separatorChar + "cache";

  private final File cacheDir = new File(R_CACHE_DIR);

  private final CryptoService cryptoService;

  @Autowired
  public RCacheHelper(CryptoService cryptoService) {
    this.cryptoService = cryptoService;
  }

  public boolean hasCache(String cacheKey) {
    File cache = new File(cacheDir, cacheKey + ".enc");
    ensureCacheDir();
    return cache.exists();
  }

  public void evictAll() {
    try {
      FileUtil.delete(cacheDir);
    } catch (Exception e) {
      log.warn("Failure when evicting caches", e);
    }
  }

  public void evictCache(String prefix) {
    try {
      ensureCacheDir();
      File[] files = cacheDir.listFiles(file -> file.getName().startsWith(prefix));
      if (files != null) {
        for (File file : files) {
          file.delete();
        }
      }
    } catch (Exception e) {
      log.warn("Failure when evicting cache files: {}*", prefix, e);
    }
  }

  public InputStream newRDSInputStream(String cacheKey) throws IOException {
    File cache = new File(cacheDir, cacheKey + ".enc");
    ensureCacheDir();
    return cryptoService.newCipherInputStream(new FileInputStream(cache));
  }

  public OutputStream newRDSOutputStream(String cacheKey) throws IOException {
    File cache = new File(cacheDir, cacheKey + ".enc");
    ensureCacheDir();
    return cryptoService.newCipherOutputStream(new FileOutputStream(cache));
  }

  private void ensureCacheDir() {
    cacheDir.mkdirs();
  }
}
