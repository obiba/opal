/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;

/**
 * <code>MessageDigest</code> utilities.
 */
public class DigestUtil {

  /**
   * Checks a file against its digest.
   * 
   * @param digestAlgorithm digest algorithm (e.g., <code>SHA-512</code>)
   * @param digestFile digest file (the digest)
   * @param dataFile data file to be checked
   * @throws DigestMismatchException if the check fails (or if it could not be performed)
   */
  public static void checkDigest(String digestAlgorithm, File digestFile, File dataFile) throws DigestMismatchException {
    FileInputStream digestFileStream = null;
    FileInputStream dataFileStream = null;

    try {
      digestFileStream = new FileInputStream(digestFile);
      dataFileStream = new FileInputStream(dataFile);
      checkDigest(digestAlgorithm, IOUtils.toByteArray(digestFileStream), IOUtils.toByteArray(dataFileStream));
    } catch(DigestMismatchException ex) {
      throw ex;
    } catch(IOException ex) {
      throw new DigestMismatchException("Digest check could not be performed (" + ex.getMessage() + ")");
    } finally {
      IOUtils.closeQuietly(digestFileStream);
      IOUtils.closeQuietly(dataFileStream);
    }
  }

  /**
   * Checks a data stream against its digest.
   * 
   * @param digestAlgorithm digest algorithm (e.g., <code>SHA-512</code>)
   * @param digest the digest
   * @param data the data to be checked
   * @throws DigestMismatchException if the check fails (or if it could not be performed)
   */
  public static void checkDigest(String digestAlgorithm, byte[] digest, byte[] data) throws DigestMismatchException {
    byte[] expectedDigest = digest;
    byte[] actualDigest = null;

    try {
      MessageDigest digester = MessageDigest.getInstance(digestAlgorithm);
      actualDigest = digester.digest(data);
    } catch(Exception ex) {
      throw new DigestMismatchException("Digest check could not be performed (" + ex.getMessage() + ")");
    }

    // Compare the digests and throw an exception if they are not equal.
    if(expectedDigest != null && actualDigest != null) {
      if(!MessageDigest.isEqual(expectedDigest, actualDigest)) {
        throw new DigestMismatchException("Digest check failed");
      }
    }
  }
}
