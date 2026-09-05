/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.fs.security;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.util.Map;

/**
 * The content of a {@link SecuredFileObject}: every method that reads the bytes of the file first checks that the
 * current subject may read it. Metadata and writes are left to the callers' own checks, so that listing a folder
 * whose entries are not all readable keeps working.
 */
class SecuredFileContent implements FileContent {

  private final SecuredFileObject file;

  private final FileContent content;

  SecuredFileContent(SecuredFileObject file, FileContent content) {
    this.file = file;
    this.content = content;
  }

  @Override
  public InputStream getInputStream() throws FileSystemException {
    file.checkReadable();
    return content.getInputStream();
  }

  @Override
  public InputStream getInputStream(int bufferSize) throws FileSystemException {
    file.checkReadable();
    return content.getInputStream(bufferSize);
  }

  @Override
  public RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
    file.checkReadable();
    return content.getRandomAccessContent(mode);
  }

  @Override
  public byte[] getByteArray() throws IOException {
    file.checkReadable();
    return content.getByteArray();
  }

  @Override
  public String getString(Charset charset) throws IOException {
    file.checkReadable();
    return content.getString(charset);
  }

  @Override
  public String getString(String charset) throws IOException {
    file.checkReadable();
    return content.getString(charset);
  }

  @Override
  public long write(FileContent output) throws IOException {
    file.checkReadable();
    return content.write(output);
  }

  @Override
  public long write(FileObject output) throws IOException {
    file.checkReadable();
    return content.write(output);
  }

  @Override
  public long write(OutputStream output) throws IOException {
    file.checkReadable();
    return content.write(output);
  }

  @Override
  public long write(OutputStream output, int bufferSize) throws IOException {
    file.checkReadable();
    return content.write(output, bufferSize);
  }

  @Override
  public FileObject getFile() {
    return file;
  }

  @Override
  public long getSize() throws FileSystemException {
    return content.getSize();
  }

  @Override
  public long getLastModifiedTime() throws FileSystemException {
    return content.getLastModifiedTime();
  }

  @Override
  public void setLastModifiedTime(long modTime) throws FileSystemException {
    content.setLastModifiedTime(modTime);
  }

  @Override
  public boolean hasAttribute(String attrName) throws FileSystemException {
    return content.hasAttribute(attrName);
  }

  @Override
  public Map<String, Object> getAttributes() throws FileSystemException {
    return content.getAttributes();
  }

  @Override
  public String[] getAttributeNames() throws FileSystemException {
    return content.getAttributeNames();
  }

  @Override
  public Object getAttribute(String attrName) throws FileSystemException {
    return content.getAttribute(attrName);
  }

  @Override
  public void setAttribute(String attrName, Object value) throws FileSystemException {
    content.setAttribute(attrName, value);
  }

  @Override
  public void removeAttribute(String attrName) throws FileSystemException {
    content.removeAttribute(attrName);
  }

  @Override
  public Certificate[] getCertificates() throws FileSystemException {
    return content.getCertificates();
  }

  @Override
  public OutputStream getOutputStream() throws FileSystemException {
    return content.getOutputStream();
  }

  @Override
  public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
    return content.getOutputStream(bAppend);
  }

  @Override
  public OutputStream getOutputStream(int bufferSize) throws FileSystemException {
    return content.getOutputStream(bufferSize);
  }

  @Override
  public OutputStream getOutputStream(boolean bAppend, int bufferSize) throws FileSystemException {
    return content.getOutputStream(bAppend, bufferSize);
  }

  @Override
  public void close() throws FileSystemException {
    content.close();
  }

  @Override
  public boolean isOpen() {
    return content.isOpen();
  }

  @Override
  public FileContentInfo getContentInfo() throws FileSystemException {
    return content.getContentInfo();
  }
}
