/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.unit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.obiba.magma.Datasource;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.MagmaCryptRuntimeException;
import org.obiba.magma.crypt.NoSuchKeyException;
import org.obiba.opal.core.crypt.CacheablePasswordCallback;
import org.obiba.opal.core.crypt.CachingCallbackHandler;
import org.obiba.opal.core.crypt.KeyPairNotFoundException;
import org.obiba.opal.core.crypt.KeyProviderException;
import org.obiba.opal.core.crypt.KeyProviderSecurityException;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.springframework.util.Assert;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

/**
 * A {@link FunctionalUnit}'s keystore.
 */
public class UnitKeyStore implements KeyProvider {

  private static final long serialVersionUID = 1L;

  private static final String PASSWORD_FOR = "Password for";

  public enum UnitKeyType {
    KEY_PAIR, CERTIFICATE
  }

  private final String unitName;

  private final KeyStore store;

  private CallbackHandler callbackHandler;

  public UnitKeyStore(String unitName, KeyStore store) {
    this.unitName = unitName;
    this.store = store;
  }

  public Set<String> listAliases() {
    try {
      return ImmutableSet.copyOf(Iterators.forEnumeration(store.aliases()));
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }

  public Entry getEntry(String alias) {
    try {
      if(store.isKeyEntry(alias)) {
        CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
            .prompt("Password for '" + alias + "':  ").build();
        return store.getEntry(alias, new PasswordProtection(getKeyPassword(passwordCallback)));
      } else if(store.isCertificateEntry(alias)) {
        return store.getEntry(alias, null);
      } else {
        throw new UnsupportedOperationException("unsupported key type");
      }
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(UnrecoverableEntryException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Set<String> listKeyPairs() {
    return ImmutableSet.copyOf(Iterables.filter(listAliases(), new Predicate<String>() {

      @Override
      public boolean apply(String input) {
        try {
          return store.isKeyEntry(input) && store.entryInstanceOf(input, PrivateKeyEntry.class);
        } catch(KeyStoreException e) {
          throw new RuntimeException(e);
        }
      }
    }));
  }

  public Set<String> listCertificates() {
    return ImmutableSet.copyOf(Iterables.filter(listAliases(), new Predicate<String>() {

      @Override
      public boolean apply(String input) {
        try {
          return store.isCertificateEntry(input);
        } catch(KeyStoreException e) {
          throw new RuntimeException(e);
        }
      }
    }));
  }

  public boolean hasKeyPair(String alias) {
    return listKeyPairs().contains(alias);
  }

  @Override
  public KeyPair getKeyPair(String alias)
      throws NoSuchKeyException, org.obiba.magma.crypt.KeyProviderSecurityException {
    KeyPair keyPair = null;
    try {
      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
          .prompt("Password for '" + alias + "':  ").build();
      keyPair = findKeyPairForPrivateKey(alias, store, keyPair, passwordCallback);
    } catch(KeyPairNotFoundException ex) {
      throw ex;
    } catch(UnrecoverableKeyException ex) {
      if(callbackHandler instanceof CachingCallbackHandler) {
        ((CachingCallbackHandler) callbackHandler).clearPasswordCache(unitName);
      }
      throw new KeyProviderSecurityException("Wrong key password");
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }

    return keyPair;
  }

  @Override
  public KeyPair getKeyPair(PublicKey publicKey)
      throws NoSuchKeyException, org.obiba.magma.crypt.KeyProviderSecurityException {
    Enumeration<String> aliases = null;
    try {
      aliases = store.aliases();
    } catch(KeyStoreException ex) {
      throw new RuntimeException(ex);
    }

    return findKeyPairForPublicKey(publicKey, aliases);
  }

  @Override
  public PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException {
    try {
      Certificate cert = store.getCertificate(datasource.getName());
      if(cert != null) {
        return cert.getPublicKey();
      }
    } catch(KeyStoreException e) {
      throw new MagmaCryptRuntimeException(e);
    }
    throw new NoSuchKeyException(datasource.getName(), "No PublicKey for Datasource '" + datasource.getName() + "'");
  }

  public X509Certificate importCertificate(String alias, FileObject certFile) {
    X509Certificate cert = getCertificateFromFile(certFile);
    try {
      store.setCertificateEntry(alias, cert);
    } catch(KeyStoreException e) {
      throw new MagmaCryptRuntimeException(e);
    }
    return cert;
  }

  public X509Certificate importCertificate(String alias, InputStream pem) {
    X509Certificate cert = getCertificate(pem);
    try {
      store.setCertificateEntry(alias, cert);
    } catch(KeyStoreException e) {
      throw new MagmaCryptRuntimeException(e);
    }
    return cert;
  }

  public List<Certificate> getCertificateEntries() {
    List<Certificate> certs = Lists.newArrayList();
    for(String alias : listAliases()) {
      Entry keyEntry = getEntry(alias);
      if(keyEntry instanceof TrustedCertificateEntry) {
        TrustedCertificateEntry tce = (TrustedCertificateEntry) keyEntry;
        certs.add(tce.getTrustedCertificate());
      }
    }
    return certs;
  }

  //
  // Methods
  //

  public void setCallbackHandler(CallbackHandler callbackHandler) {
    this.callbackHandler = callbackHandler;
  }

  public String getUnitName() {
    return unitName;
  }

  public KeyStore getKeyStore() {
    return store;
  }

  private char[] getKeyPassword(CacheablePasswordCallback passwordCallback)
      throws UnsupportedCallbackException, IOException {
    callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
    return passwordCallback.getPassword();
  }

  private KeyPair findKeyPairForPrivateKey(String alias, KeyStore ks, @Nullable KeyPair keyPair,
      CacheablePasswordCallback passwordCallback)
      throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedCallbackException,
      IOException {
    Key key = ks.getKey(alias, getKeyPassword(passwordCallback));
    if(key == null) {
      throw new KeyPairNotFoundException("KeyPair not found for specified alias (" + alias + ")");
    }

    if(key instanceof PrivateKey) {
      // Get certificate of public key
      Certificate cert = ks.getCertificate(alias);

      // Get public key
      PublicKey publicKey = cert.getPublicKey();

      // Return a key pair
      return new KeyPair(publicKey, (PrivateKey) key);
    } else {
      throw new KeyPairNotFoundException("KeyPair not found for specified alias (" + alias + ")");
    }
  }

  private KeyPair findKeyPairForPublicKey(Key publicKey, Enumeration<String> aliases) {
    KeyPair keyPair = null;

    while(aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      KeyPair currentKeyPair = getKeyPair(alias);

      if(Arrays.equals(currentKeyPair.getPublic().getEncoded(), publicKey.getEncoded())) {
        keyPair = currentKeyPair;
        break;
      }
    }

    if(keyPair == null) {
      throw new KeyPairNotFoundException("KeyPair not found for specified public key");
    }
    return keyPair;
  }

  public static X509Certificate makeCertificate(PrivateKey issuerPrivateKey, PublicKey subjectPublicKey,
      String certificateInfo, String signatureAlgorithm)
      throws SignatureException, InvalidKeyException, CertificateEncodingException, NoSuchAlgorithmException {
    X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
    X509Name issuerDN = new X509Name(certificateInfo);
    X509Name subjectDN = new X509Name(certificateInfo);
    int daysTillExpiry = 30 * 365;

    Calendar expiry = Calendar.getInstance();
    expiry.add(Calendar.DAY_OF_YEAR, daysTillExpiry);

    certificateGenerator.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
    certificateGenerator.setIssuerDN(issuerDN);
    certificateGenerator.setSubjectDN(subjectDN);
    certificateGenerator.setPublicKey(subjectPublicKey);
    certificateGenerator.setNotBefore(new Date());
    certificateGenerator.setNotAfter(expiry.getTime());
    certificateGenerator.setSignatureAlgorithm(signatureAlgorithm);

    return certificateGenerator.generate(issuerPrivateKey);
  }

  public void createOrUpdateKey(String alias, String algorithm, int size, String certificateInfo) {
    try {
      KeyPair keyPair = generateKeyPair(algorithm, size);
      X509Certificate cert = makeCertificate(algorithm, certificateInfo, keyPair);
      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
          .prompt(getPasswordFor(unitName)).build();
      store.setKeyEntry(alias, keyPair.getPrivate(), getKeyPassword(passwordCallback), new X509Certificate[] { cert });
    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Deletes the key associated with the provided alias.
   *
   * @param alias key to delete
   */
  public void deleteKey(String alias) {
    try {
      store.deleteEntry(alias);
    } catch(KeyStoreException e) {
      throw new KeyProviderException(e);
    }

  }

  /**
   * Returns true if the provided alias exists.
   *
   * @param alias check if this alias exists in the KeyStore.
   * @return true if the alias exists
   */
  public boolean aliasExists(String alias) {
    try {
      return store.containsAlias(alias);
    } catch(KeyStoreException e) {
      throw new KeyProviderException(e);
    }
  }

  public UnitKeyType getKeyType(String alias) {
    if(listKeyPairs().contains(alias)) {
      return UnitKeyType.KEY_PAIR;
    }
    if(listCertificates().contains(alias)) {
      return UnitKeyType.CERTIFICATE;
    }
    throw new IllegalArgumentException("unknown alias '" + alias + "'or key type");
  }

  public static void loadBouncyCastle() {
    if(Security.getProvider("BC") == null) Security.addProvider(new BouncyCastleProvider());
  }

  /**
   * Import a private key and it's associated certificate into the keystore at the given alias.
   *
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificate certificate in the PEM format
   */
  public void importKey(String alias, FileObject privateKey, FileObject certificate) {
    storeKeyEntry(alias, getPrivateKeyFromFile(privateKey), getCertificateFromFile(certificate));
  }

  public void importKey(String alias, InputStream privateKey, InputStream certificate)
      throws NoSuchFunctionalUnitException {
    storeKeyEntry(alias, getPrivateKey(privateKey), getCertificate(certificate));
  }

  private void storeKeyEntry(String alias, Key key, X509Certificate cert) {
    CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
        .prompt(getPasswordFor(alias)).build();
    try {
      store.setKeyEntry(alias, key, getKeyPassword(passwordCallback), new X509Certificate[] { cert });
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Import a private key into the keystore and generate an associated certificate at the given alias.
   *
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   */
  public void importKey(String alias, FileObject privateKey, String certificateInfo) {
    makeAndStoreKeyEntry(alias, getKeyPairFromFile(privateKey), certificateInfo);
  }

  public void importKey(String alias, InputStream privateKey, String certificateInfo)
      throws NoSuchFunctionalUnitException {
    makeAndStoreKeyEntry(alias, getKeyPair(privateKey), certificateInfo);
  }

  private void makeAndStoreKeyEntry(String alias, KeyPair keyPair, String certificateInfo) {
    X509Certificate cert;
    try {
      cert = makeCertificate(keyPair.getPrivate(), keyPair.getPublic(), certificateInfo,
          chooseSignatureAlgorithm(keyPair.getPrivate().getAlgorithm()));
      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unitName)
          .prompt(getPasswordFor(alias)).build();
      store.setKeyEntry(alias, keyPair.getPrivate(), getKeyPassword(passwordCallback), new X509Certificate[] { cert });
    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private X509Certificate makeCertificate(String algorithm, String certificateInfo, KeyPair keyPair)
      throws SignatureException, InvalidKeyException, CertificateEncodingException, NoSuchAlgorithmException {
    X509Certificate cert = makeCertificate(keyPair.getPrivate(), keyPair.getPublic(), certificateInfo,
        chooseSignatureAlgorithm(algorithm));
    return cert;
  }

  private KeyPair generateKeyPair(String algorithm, int size) throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator;
    keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
    keyPairGenerator.initialize(size);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    return keyPair;
  }

  private String chooseSignatureAlgorithm(String keyAlgorithm) {
    // TODO add more algorithms here.
    if("DSA".equals(keyAlgorithm)) {
      return "SHA1withDSA";
    }
    return "SHA1WithRSA";
  }

  private KeyPair getKeyPairFromFile(FileObject privateKey) {
    try {
      return getKeyPair(privateKey.getContent().getInputStream());
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    } catch(RuntimeException e) {
      throw new RuntimeException("Failed reading key pair from file: " + privateKey.getName(), e);
    }
  }

  private KeyPair getKeyPair(InputStream privateKey) {
    PEMReader pemReader = null;
    try {
      pemReader = new PEMReader(new InputStreamReader(privateKey), new PasswordFinder() {
        @Override
        public char[] getPassword() {
          return System.console().readPassword("%s:  ", "Password for imported private key");
        }
      });
      Object object = pemReader.readObject();
      if(object == null) {
        throw new RuntimeException("No PEM information.");
      }
      if(object instanceof KeyPair) {
        return (KeyPair) object;
      }
      throw new RuntimeException("Unexpected type [" + object + "]. Expected KeyPair.");
    } catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(pemReader);
    }
  }

  private Key getPrivateKeyFromFile(FileObject privateKey) {
    try {
      return getPrivateKey(privateKey.getContent().getInputStream());
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    } catch(RuntimeException e) {
      throw new RuntimeException("Failed reading private key from file: " + privateKey.getName(), e);
    }
  }

  private Key getPrivateKey(InputStream privateKey) {

    PEMReader pemReader = null;
    try {
      pemReader = new PEMReader(new InputStreamReader(privateKey), new PasswordFinder() {
        @Override
        public char[] getPassword() {
          return System.console().readPassword("%s:  ", "Password for imported private key");
        }
      });
      Object pemObject = pemReader.readObject();
      if(pemObject == null) {
        throw new RuntimeException("No PEM information.");
      }
      return toPrivateKey(pemObject);
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(pemReader);
    }
  }

  private Key toPrivateKey(Object pemObject) {
    if(pemObject instanceof KeyPair) {
      KeyPair keyPair = (KeyPair) pemObject;
      return keyPair.getPrivate();
    }
    if(pemObject instanceof Key) {
      return (Key) pemObject;
    }
    throw new RuntimeException("Unexpected type [" + pemObject + "]. Expected KeyPair or Key.");
  }

  private X509Certificate getCertificateFromFile(FileObject certificate) {
    try {
      return getCertificate(certificate.getContent().getInputStream());
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    } catch(RuntimeException e) {
      throw new RuntimeException("Failed reading certificate from file: " + certificate.getName(), e);
    }
  }

  private X509Certificate getCertificate(InputStream certificate) {
    PEMReader pemReader = null;
    try {
      pemReader = new PEMReader(new InputStreamReader(certificate), new PasswordFinder() {

        @Override
        public char[] getPassword() {
          return System.console().readPassword("%s:  ", "Password for imported certificate");
        }
      });
      Object object = pemReader.readObject();
      if(object == null) {
        throw new RuntimeException("No PEM information.");
      }
      if(object instanceof X509Certificate) {
        return (X509Certificate) object;
      }
      throw new RuntimeException("Unexpected type [" + object + "]. Expected X509Certificate.");
    } catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(pemReader);
    }
  }

  /**
   * Returns "Password for 'name':  ".
   */
  private String getPasswordFor(String name) {
    return PASSWORD_FOR + " '" + name + "':  ";
  }

  //
  // Inner Classes
  //

  @SuppressWarnings({ "StaticMethodOnlyUsedInOneClass", "ParameterHidesMemberVariable" })
  public static class Builder {
    private String unit;

    private CallbackHandler callbackHandler;

    public static Builder newStore() {
      return new Builder();
    }

    public Builder unit(String unit) {
      this.unit = unit;
      return this;
    }

    public Builder passwordPrompt(CallbackHandler callbackHandler) {
      this.callbackHandler = callbackHandler;
      return this;
    }

    private char[] getKeyPassword(CacheablePasswordCallback passwordCallback)
        throws UnsupportedCallbackException, IOException {
      callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
      return passwordCallback.getPassword();
    }

    public UnitKeyStore build() {
      Assert.hasText(unit, "unit must not be null or empty");
      Assert.notNull(callbackHandler, "callbackHandler must not be null");

      loadBouncyCastle();

      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(unit)
          .prompt("Enter '" + unit + "' keystore password:  ")
          .confirmation("Re-enter '" + unit + "' keystore password:  ").build();

      KeyStore keyStore = createEmptyKeyStore(passwordCallback);

      return createUnitKeyStore(keyStore);
    }

    private KeyStore createEmptyKeyStore(CacheablePasswordCallback passwordCallback) {
      KeyStore keyStore = null;
      try {
        keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, getKeyPassword(passwordCallback));
      } catch(KeyStoreException e) {
        clearPasswordCache(callbackHandler, unit);
        throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
      } catch(GeneralSecurityException e) {
        throw new RuntimeException(e);
      } catch(IOException ex) {
        clearPasswordCache(callbackHandler, unit);
        translateAndRethrowKeyStoreIOException(ex);
      } catch(UnsupportedCallbackException e) {
        throw new RuntimeException(e);
      }
      return keyStore;
    }

    private static void clearPasswordCache(CallbackHandler callbackHandler, String alias) {
      if(callbackHandler instanceof CachingCallbackHandler) {
        ((CachingCallbackHandler) callbackHandler).clearPasswordCache(alias);
      }
    }

    private static void translateAndRethrowKeyStoreIOException(IOException ex) {
      if(ex.getCause() != null && ex.getCause() instanceof UnrecoverableKeyException) {
        throw new KeyProviderSecurityException("Wrong keystore password");
      }
      throw new RuntimeException(ex);
    }

    private UnitKeyStore createUnitKeyStore(KeyStore keyStore) {
      UnitKeyStore unitKeyStore = new UnitKeyStore(unit, keyStore);
      unitKeyStore.setCallbackHandler(callbackHandler);
      return unitKeyStore;
    }
  }

}
