package org.obiba.opal.core.upgrade.v2_0_x.database;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.obiba.opal.core.crypt.CacheablePasswordCallback;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.security.KeyStoreState;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.domain.security.SubjectProfile;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.sql.OCommandSQL;

@SuppressWarnings({ "SpringJavaAutowiringInspection", "MethodOnlyUsedFromInnerClass" })
public class MoveUnitKeyStoreToOrientUpgradeStep extends AbstractUpgradeStep {

  private File configFile;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @Autowired
  private CallbackHandler upgradePasswordCallbackHandler;

  @Override
  public void execute(Version currentVersion) {

    orientDbService.createUniqueIndex(KeyStoreState.class);
    orientDbService.createUniqueIndex(SubjectProfile.class);

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
    deleteFunctionalUnitsFromXmlConfig();
  }

  private KeyStore getKeyStore(String name, byte... keyStoreBytes)
      throws GeneralSecurityException, UnsupportedCallbackException, IOException {

    CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(name)
        .prompt(OpalKeyStore.PASSWORD_FOR + " '" + name + "':  ").build();

    upgradePasswordCallbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });

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
      changeUnitSubjectTypeToGroup(unit);
    }
  }

  private void changeUnitSubjectTypeToGroup(final String unit) {
    orientDbService.execute(new OrientDbService.WithinDocumentTxCallbackWithoutResult() {
      @Override
      protected void withinDocumentTxWithoutResult(ODatabaseDocumentTx db) {
        db.command(new OCommandSQL("update " + SubjectAcl.class.getSimpleName() + " set type = ? where principal = ?"))
            .execute(SubjectAcl.SubjectType.GROUP, unit);
      }
    });
  }

  private void importKeyPairs(String unit, OpalKeyStore keyStore) throws KeyStoreException {
    for(String alias : keyStore.listKeyPairs()) {
      KeyPair keyPair = keyStore.getKeyPair(alias);
      byte[] privateKey = keyPair.getPrivate().getEncoded();
      byte[] publicKey = keyPair.getPublic().getEncoded();
      for(Project project : orientDbService.list(Project.class)) {
        projectsKeyStoreService
            .importKey(project, unit, new ByteArrayInputStream(privateKey), new ByteArrayInputStream(publicKey));
      }
    }
  }

  private void deleteFunctionalUnitsFromXmlConfig() {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
      XPath xPath = XPathFactory.newInstance().newXPath();
      Node node = (Node) xPath.compile("//functionalUnits").evaluate(doc.getDocumentElement(), XPathConstants.NODE);
      if(node != null) node.getParentNode().removeChild(node);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(doc), new StreamResult(configFile));
    } catch(SAXException | TransformerException | XPathExpressionException | ParserConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }

}
