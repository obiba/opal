package org.obiba.opal.core.upgrade.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.obiba.opal.core.crypt.CacheablePasswordCallback;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.security.KeyStoreState;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

@SuppressWarnings({ "SpringJavaAutowiringInspection", "MethodOnlyUsedFromInnerClass" })
public class MoveUnitKeyStoreToOrientUpgradeStep extends AbstractUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private CallbackHandler callbackHandler;

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @Override
  public void execute(Version currentVersion) {

    orientDbService.createUniqueIndex(KeyStoreState.class);

    JdbcOperations dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    dataJdbcTemplate.query("select * from unit_key_store", new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        String unit = rs.getString("unit");
        byte[] keyStoreBytes = rs.getBytes("key_store");
        if("OpalInstance".equals(unit)) {
          importSystemKeyStore(keyStoreBytes);
        } else {
          try {
            OpalKeyStore keyStore = new OpalKeyStore(unit, getKeyStore(unit, keyStoreBytes));
            importCertificates(unit, keyStore);
            importKeyPairs(unit, keyStore);
          } catch(GeneralSecurityException | UnsupportedCallbackException | IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

    });
    dataJdbcTemplate.execute("drop table unit_key_store");
  }

  private KeyStore getKeyStore(String name, byte... keyStoreBytes)
      throws GeneralSecurityException, UnsupportedCallbackException, IOException {

    CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(name)
        .prompt(OpalKeyStore.PASSWORD_FOR + " '" + name + "':  ").build();

    callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });

    KeyStore keyStore = KeyStore.getInstance("JCEKS");
    keyStore.load(new ByteArrayInputStream(keyStoreBytes), passwordCallback.getPassword());
    return keyStore;
  }

  private void importSystemKeyStore(byte... keyStoreBytes) {
    KeyStoreState state = new KeyStoreState();
    state.setName("system");
    state.setKeyStore(keyStoreBytes);
    orientDbService.save(null, state);
  }

  private void importCertificates(String unit, OpalKeyStore keyStore) throws CertificateEncodingException {
    Map<String, Certificate> certificates = keyStore.getCertificates();
    for(String alias : keyStore.listCertificates()) {
      Certificate certificate = certificates.get(alias);
      SubjectCredentials.Builder builder = SubjectCredentials.Builder.create() //
          .name(unit + "-" + alias) //
          .certificate(certificate.getEncoded()) //
          .authenticationType(SubjectCredentials.AuthenticationType.CERTIFICATE) //
          .enabled(true) //
          .group(unit);
      subjectCredentialsService.save(builder.build());
      //TODO change unit permission to GROUP
    }
  }

  private void importKeyPairs(String unit, OpalKeyStore keyStore) throws KeyStoreException {
    for(String alias : keyStore.listKeyPairs()) {
      KeyPair keyPair = keyStore.getKeyPair(alias);
      byte[] privateKey = keyPair.getPrivate().getEncoded();
      byte[] publicKey = keyPair.getPublic().getEncoded();
      for(Project project : orientDbService.list(Project.class)) {
        projectsKeyStoreService.importKey(project, unit, new ByteInputStream(privateKey, privateKey.length),
            new ByteInputStream(publicKey, publicKey.length));
      }
    }
  }
}
