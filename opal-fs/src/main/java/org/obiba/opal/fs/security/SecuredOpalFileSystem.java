package org.obiba.opal.fs.security;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.fs.OpalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class SecuredOpalFileSystem implements OpalFileSystem {

  private static final Logger log = LoggerFactory.getLogger(OpalFileSystem.class);

  private final SubjectAclService aclService;

  private final OpalFileSystem delegate;

  public SecuredOpalFileSystem(SubjectAclService aclService, OpalFileSystem delegate) {
    Preconditions.checkArgument(aclService != null);
    Preconditions.checkArgument(delegate != null);

    this.aclService = aclService;
    this.delegate = delegate;
  }

  public FileObject getRoot() {
    return new SecuredFileObject(delegate.getRoot());
  }

  public File getLocalFile(FileObject virtualFile) {
    return delegate.getLocalFile(virtualFile);
  }

  public File convertVirtualFileToLocal(FileObject virtualFile) {
    return delegate.convertVirtualFileToLocal(virtualFile);
  }

  public boolean isLocalFile(FileObject virtualFile) {
    return delegate.isLocalFile(virtualFile);
  }

  @Override
  public String getObfuscatedPath(FileObject virtualFile) {
    return delegate.getObfuscatedPath(virtualFile);
  }

  @Override
  public FileObject resolveFileFromObfuscatedPath(FileObject baseFolder, String obfuscatedPath) {
    return delegate.resolveFileFromObfuscatedPath(baseFolder, obfuscatedPath);
  }
}
