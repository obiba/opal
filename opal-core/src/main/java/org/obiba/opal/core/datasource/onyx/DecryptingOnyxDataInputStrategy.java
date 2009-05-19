/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.datasource.onyx;

import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

/**
 * <p>
 * DecryptingOnyxDataInputStrategy is an IOnyxDataInputStrategy used to acquire a decrypted Onyx data
 * <code>InputStream</code>. It depends on its delegate to provide the <i>encrypted</i> <code>InputStream</code>.
 * </p>
 * 
 * <p>
 * DecryptingOnyxDataInputStrategy depends on certain encryption metadata (wrapped encryption/decryption key, IV, etc.)
 * contained in encryption.xml (this is an entry in the zip file). The prepare method initializes the strategy according
 * to these metadata. Once initialized, the getEntry method may be used to obtain a decrypted <code>InputStream</code>
 * for the specified entry.
 * </p>
 */
public class DecryptingOnyxDataInputStrategy implements IChainingOnyxDataInputStrategy {
  //
  // Constants
  //

  private static final String METADATA_ENTRY = "encryption.xml";

  private static final String DIGEST_ENTRY_SUFFIX = ".sha512";

  private static final String PKCS8_KEYSPEC_FORMAT = "PKCS#8";

  private static final String X509_KEYSPEC_FORMAT = "X.509";

  //
  // Instance Variables
  //

  private IOnyxDataInputStrategy delegate;

  private IKeyProvider keyProvider;

  private EncryptionData metadata;

  //
  // IChainingOnyxDataInputStrategy Methods
  //

  public void setDelegate(IOnyxDataInputStrategy delegate) {
    this.delegate = delegate;
  }

  /**
   * Calls the delegate's <code>listEntries</code> method to obtain all available entries. The list returned should
   * include the metadata entry (named <code>encryption.xml</code>). If it does, this method then gets an
   * <code>InputStream</code> for this entry, reads the metadata, and initializes the strategy.
   * 
   * @param context the strategy's context
   * @throws RuntimeException if the metadata entry could not be found
   */
  public void prepare(OnyxDataInputContext context) {
    // Prepare delegate.
    delegate.prepare(context);

    // Initialize the key provider.
    keyProvider.init(context.getKeyProviderArgs());

    List<String> delegateEntries = delegate.listEntries();
    if(delegateEntries.contains(METADATA_ENTRY)) {
      // Get the metadata and initialize the strategy accordingly.
      metadata = EncryptionData.fromXml(delegate.getEntry(METADATA_ENTRY));
    } else {
      throw new RuntimeException("Metadata entry (encryption.xml) not found");
    }
  }

  /**
   * Returns a list of encrypted entries. This should be a <i>subset</i> of the delegate's entries. Only encrypted
   * entries should be returned. Other types of entries (e.g., the metadata entry "encryption.xml") should be filtered.
   * 
   * Calling <code>getEntry</code> with the name of one of these entries returns a <i>decrypted</i>
   * <code>InputStream</code> for that entry.
   * 
   * @return list of encrypted entries (i.e., their names)
   */
  public List<String> listEntries() {
    if(metadata == null) {
      throw new IllegalStateException("Null metadata (prepare method must be called prior to calling listEntries method)");
    }

    List<String> entries = new ArrayList<String>();

    for(String entry : delegate.listEntries()) {
      if(isEncryptedEntry(entry)) {
        entries.add(entry);
      }
    }

    return entries;
  }

  /**
   * Returns a <i>decrypted</i> <code>InputStream</code> for the specified <i>encrypted</i> entry.
   * 
   * @param name name of encrypted entry
   * @return decrypted <code>InputStream</code> for the specified entry
   */
  public InputStream getEntry(String name) {
    if(metadata == null) {
      throw new IllegalStateException("Null metadata (prepare method must be called prior to calling getEntry method)");
    }

    // Don't try to decrypt non-encrypted entries! Return null since non-encrypted entries are not
    // supposed to be visible to clients (see listEntries()).
    if(!isEncryptedEntry(name)) {
      return null;
    }

    // Get the entry's encrypted InputStream from the delegate.
    InputStream encryptedEntryStream = delegate.getEntry(name);

    // Create a CipherInputStream for reading the entry's decrypted data.
    CipherInputStream decryptedEntryStream = null;

    if(encryptedEntryStream != null) { // do nothing if the entry does not exist
      try {
        decryptedEntryStream = new CipherInputStream(encryptedEntryStream, getCipher());
      } catch(KeyProviderSecurityException ex) {
        throw ex;
      } catch(CipherResolutionException ex) {
        throw new RuntimeException(ex);
      }
    }

    return decryptedEntryStream;
  }

  public void terminate(OnyxDataInputContext context) {
    metadata = null;

    // Terminate the delegate.
    delegate.terminate(context);
  }

  //
  // Methods
  //

  public void setKeyProvider(IKeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  public EncryptionData getMetadata() {
    return metadata;
  }

  /**
   * <p>
   * Indicates whether the specified entry is an encrypted entry.
   * </p>
   * 
   * <p>
   * This method is used by the <code>listEntries</code> method to filter the list returned by the delegate. It is
   * also used by the <code>getEntry</code> method to block attempts to get an <code>InputStream</code> for an entry
   * that is not an encrypted entry.
   * </p>
   * 
   * @param entryName the name of the entry
   * @return <code>true</code> if the entry is an encrypted entry
   */
  public boolean isEncryptedEntry(String entryName) {
    return (entryName != null && !entryName.equals(METADATA_ENTRY) && !entryName.endsWith(DIGEST_ENTRY_SUFFIX));
  }

  /**
   * Returns a <code>Cipher</code> instance initialized for decrypting entries. The
   * <code>Cipher<code> is initialized based on the current metadata.
   * 
   * @return <code>Cipher</code> for decrypting entries
   * @throws CipherResolutionException if for any reason the requested <code>Cipher</code>
   * could not be obtained or initialized as required
   */
  private Cipher getCipher() throws CipherResolutionException {
    Cipher cipher = null;

    String transformation = metadata.getEntry("transformation");
    String[] transformationElements = transformation.split("/");
    String algorithm = transformationElements[0];

    try {
      AlgorithmParameters algorithmParameters = getAlgorithmParameters(algorithm);
      Key unwrappedKey = getUnwrappedKey(algorithm);

      cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.DECRYPT_MODE, unwrappedKey, algorithmParameters);
    } catch(KeyProviderSecurityException ex) {
      throw ex;
    } catch(Exception ex) {
      throw new CipherResolutionException(ex);
    }

    return cipher;
  }

  private AlgorithmParameters getAlgorithmParameters(String algorithm) throws IOException, NoSuchAlgorithmException {
    AlgorithmParameters algorithmParameters = null;

    // It is assumed here that there may be no algorithmParameters entry in the metadata
    // (since some algorithms may not require any).
    byte[] encodedParameters = metadata.getEntry("algorithmParameters");
    if(encodedParameters != null) {
      algorithmParameters = AlgorithmParameters.getInstance(algorithm);
      algorithmParameters.init(encodedParameters);
    }

    return algorithmParameters;
  }

  private Key getUnwrappedKey(String wrappedKeyAlgorithm) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
    byte[] wrappedKey = metadata.getEntry("key");
    Key privateKey = getPrivateKey();

    Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
    cipher.init(Cipher.UNWRAP_MODE, privateKey);
    return cipher.unwrap(wrappedKey, wrappedKeyAlgorithm, Cipher.SECRET_KEY);
  }

  private Key getPrivateKey() {
    String publicKeyAlgorithm = metadata.getEntry("publicKeyAlgorithm");
    String publicKeyFormat = metadata.getEntry("publicKeyFormat");
    byte[] encodedPublicKey = metadata.getEntry("publicKey");
    PublicKey publicKey = getPublicKey(publicKeyAlgorithm, publicKeyFormat, encodedPublicKey);

    KeyPair keyPair = keyProvider.getKeyPair(publicKey);

    return keyPair.getPrivate();
  }

  private PublicKey getPublicKey(String algorithm, String format, byte[] encodedKey) {
    PublicKey publicKey = null;

    EncodedKeySpec keySpec = null;

    if(format.equals(X509_KEYSPEC_FORMAT)) {
      keySpec = new X509EncodedKeySpec(encodedKey);
    } else if(format.equals(PKCS8_KEYSPEC_FORMAT)) {
      keySpec = new PKCS8EncodedKeySpec(encodedKey);
    } else {
      // TODO: Support other formats.
      throw new RuntimeException("Unsupported KeySpec format (" + format + ")");
    }

    try {
      KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
      publicKey = keyFactory.generatePublic(keySpec);
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }

    return publicKey;
  }
}