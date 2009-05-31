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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.crypt.OpalKeyStore;
import org.springframework.core.io.FileSystemResource;

public class DecryptingOnyxDataInputStrategyTest {
  //
  // Constants
  //

  private static final String SUB_DIR = "OnyxDataInputStrategyTests";

  private static final String RESOURCE_DIR = //
  "src" + File.separator + //
  "test" + File.separator + //
  "resources" + File.separator + //
  SUB_DIR;

  private static final String TMP_DIR = //
  "target" + File.separator + //
  SUB_DIR;

  private static final String TEST_ARCHIVE_NO_META = //
  RESOURCE_DIR + File.separator + //
  "testArchive.zip";

  private static final String TEST_ARCHIVE_WITH_META = //
  RESOURCE_DIR + File.separator + //
  "testArchiveWithEncryptedEntries.zip";

  private static final String TEST_ARCHIVE_ONYX = //
  RESOURCE_DIR + File.separator + //
  "onyxDataExport.zip";

  private static final String TEST_ARCHIVE_ONYX_WITH_ENTRY_DIGEST_MISMATCH = //
  RESOURCE_DIR + File.separator + //
  "onyxDataExportWithEntryDigestMismatch.zip";

  private static final String TEST_KEYSTORE = //
  RESOURCE_DIR + File.separator + //
  "opal.jks";

  //
  // Instance Variables
  //

  private DecryptingOnyxDataInputStrategy decryptingStrategy;

  private OnyxDataInputContext context;

  //
  // Fixture Methods (setUp, tearDown)
  //

  @BeforeClass
  public static void setUpOnce() {
    File tmpDir = new File("target", "OnyxDataInputStrategyTests");
    if(tmpDir.exists()) {
      try {
        FileUtil.delete(tmpDir);
      } catch(IOException ex) {
        fail("Failed to delete pre-existing tmpDir (" + ex.getMessage() + ")");
      }
    }

    boolean tmpDirCreated = tmpDir.mkdirs();

    if(!tmpDirCreated) {
      fail("Failed to create tmpDir (" + tmpDir.getAbsolutePath() + ")");
    }
  }

  @AfterClass
  public static void tearDownOnce() {
    File tmpDir = new File("target", "OnyxDataInputStrategyTests");
    if(tmpDir.exists()) {
      try {
        FileUtil.delete(tmpDir);
      } catch(IOException ex) {
        fail("Failed to delete tmpDir (" + ex.getMessage() + ")");
      }
    }
  }

  @Before
  public void setUp() {
    context = new OnyxDataInputContext();
    context.setSource(TEST_ARCHIVE_WITH_META);
    context.setKeyProviderArg(OpalKeyStore.KEYSTORE_PASSWORD_ARGKEY, "password");
    context.setKeyProviderArg(OpalKeyStore.KEY_PASSWORD_ARGKEY, "password");

    OpalKeyStore keyStore = new OpalKeyStore();
    keyStore.setKeyStoreResource(new FileSystemResource(TEST_KEYSTORE));
    keyStore.init(context.getKeyProviderArgs());

    ZipOnyxDataInputStrategy zipStrategy = new ZipOnyxDataInputStrategy();
    zipStrategy.setDelegate(new FileOnyxDataInputStrategy());

    decryptingStrategy = new DecryptingOnyxDataInputStrategy();
    decryptingStrategy.setKeyProvider(keyStore);
    decryptingStrategy.setDelegate(zipStrategy);
  }

  @After
  public void tearDown() {
    decryptingStrategy.terminate(context);
  }

  //
  // Test Methods
  //

  @Test
  public void testPrepare() {
    decryptingStrategy.prepare(context);

    EncryptionData metadata = decryptingStrategy.getMetadata();
    assertNotNull(metadata);

    String transformation = metadata.getEntry("transformation");
    assertEquals("AES/CFB/NoPadding", transformation);

    byte[] key = metadata.getEntry("key");
    assertNotNull(key);
    assertArrayEquals(decodeBase64("M0j5qW6FaWgylytzhP7J3KTsrS+AY38abkO7U7Iplk4p0mwNQjelSOw+Y7CivxruFBQ50Q1Oktl46kWX3lyu2w=="), key);
  }

  @Test
  public void testPrepareWithNoMetadata() {
    context.setSource(TEST_ARCHIVE_NO_META);

    try {
      decryptingStrategy.prepare(context);
      fail("Expected RuntimeException");
    } catch(RuntimeException ex) {
      String exMsg = ex.getMessage();
      assertNotNull(exMsg);
      assertEquals("Metadata entry (encryption.xml) not found", exMsg);
    } catch(Exception ex) {
      fail("Unexpected exception type (" + ex.getClass().getSimpleName() + ")");
    }
  }

  /**
   * Tests that only the encrypted entries are included the list (non-encrypted entries such as
   * <code>encryption.xml</code> should be omitted).
   */
  @Test
  public void testListEntries() {
    decryptingStrategy.prepare(context);

    List<String> entries = decryptingStrategy.listEntries();
    assertEquals(3, entries.size());
    assertTrue(entries.contains("entry1.txt"));
    assertTrue(entries.contains("entry2.txt"));
    assertTrue(entries.contains("entry3.txt"));
  }

  @Test
  public void testListEntriesBeforePrepare() {
    try {
      decryptingStrategy.listEntries();
      fail("Expected IllegalStateException");
    } catch(IllegalStateException ex) {
      String exMsg = ex.getMessage();
      assertNotNull(exMsg);
      assertEquals("Null metadata (prepare method must be called prior to calling listEntries method)", exMsg);
    } catch(Exception ex) {
      fail("Unexpected exception type (" + ex.getClass().getSimpleName() + ")");
    }
  }

  @Test(timeout = 60000)
  public void testGetEntryWithOnyxDataExport() throws IOException {
    context.setSource(TEST_ARCHIVE_ONYX);

    decryptingStrategy.prepare(context);

    for(String entryName : decryptingStrategy.listEntries()) {
      InputStream entryStream = null;

      try {
        entryStream = decryptingStrategy.getEntry(entryName);
        assertNotNull(entryStream);

        String decryptedEntryName = entryName + ".decrypted";
        persistDecryptedEntry(entryStream, decryptedEntryName);
        assertTrue(compareFiles(new File(RESOURCE_DIR, decryptedEntryName), new File(TMP_DIR, decryptedEntryName)));
      } finally {
        IOUtils.closeQuietly(entryStream);
      }
    }
  }

  @Test(timeout = 60000)
  public void testGetEntryWithDigestMismatch() throws IOException {
    context.setSource(TEST_ARCHIVE_ONYX_WITH_ENTRY_DIGEST_MISMATCH);

    decryptingStrategy.prepare(context);

    InputStream entryStream = null;

    try {
      entryStream = decryptingStrategy.getEntry("entryWithDigestMismatch.xml");
      fail("Expected DigestMismatchException");
    } catch(DigestMismatchException ex) {
      assertEquals("Digest check failed", ex.getMessage());
    } finally {
      IOUtils.closeQuietly(entryStream);
    }
  }

  //
  // Methods
  //

  private byte[] decodeBase64(String encodedData) {
    return Base64.decodeBase64(encodedData.getBytes());
  }

  private void persistDecryptedEntry(InputStream entryStream, String fileName) throws IOException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(TMP_DIR + File.separator + fileName);

      while(true) {
        int entryByte = entryStream.read();
        if(entryByte == -1) {
          break;
        }

        fos.write(entryByte);
      }
    } finally {
      if(fos != null) {
        try {
          fos.close();
        } catch(IOException ex) {
        }
      }
    }
  }

  /**
   * Performs a byte by byte comparison of two files.
   * 
   * @param file a file
   * @param anotherFile another file
   * @return <code>true</code> if the files match
   */
  private boolean compareFiles(File file, File anotherFile) throws IOException {
    FileInputStream fileStream = null;
    FileInputStream anotherFileStream = null;

    try {
      fileStream = new FileInputStream(file);
      anotherFileStream = new FileInputStream(file);

      while(true) {
        int aByte = fileStream.read();
        int anotherByte = anotherFileStream.read();

        if(aByte != anotherByte) {
          return false;
        }

        if(aByte == -1) {
          break;
        }
      }
    } finally {
      if(fileStream != null) {
        try {
          fileStream.close();
        } catch(IOException ex) {
        }
      }

      if(anotherFileStream != null) {
        try {
          anotherFileStream.close();
        } catch(IOException ex) {
        }
      }
    }

    return true;
  }
}
