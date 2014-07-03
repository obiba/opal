package org.obiba.opal.core.upgrade.v2_0_x.database;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.validation.ConstraintViolationException;
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

import org.obiba.crypt.CacheablePasswordCallback;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.KeyStoreState;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.DuplicateSubjectProfileException;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.security.CredentialsKeyStoreService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.core.service.security.realm.ApplicationRealm;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;

@SuppressWarnings({ "SpringJavaAutowiringInspection", "MethodOnlyUsedFromInnerClass" })
public class MoveUnitKeyStoreToOrientUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(MoveUnitKeyStoreToOrientUpgradeStep.class);

  private File configFile;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private CredentialsKeyStoreService credentialsKeyStoreService;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @Autowired
  private CallbackHandler upgradePasswordCallbackHandler;

  @Override
  public void execute(Version currentVersion) {

    orientDbService.createUniqueIndex(SubjectCredentials.class);
    orientDbService.createUniqueIndex(Group.class);

    JdbcOperations dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    dataJdbcTemplate.query("select * from unit_key_store", new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        String unit = rs.getString("unit");
        byte[] keyStoreBytes = rs.getBytes("key_store");
        log.debug("Import '{}' key store", unit);
        if("OpalInstance".equals(unit)) {
          importSystemKeyStore(keyStoreBytes);
        } else {
          try {
            OpalKeyStore opalKeyStore = new OpalKeyStore(unit, getKeyStore(unit, keyStoreBytes));
            opalKeyStore.setCallbackHandler(upgradePasswordCallbackHandler);
            importCertificates(unit, opalKeyStore);
            importKeyPairs(unit, opalKeyStore);
          } catch(Exception e) {
            // do not break the upgrade
            log.error("Unable to import certificate/key pair for unit: {}", unit, e);
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

  private void importCertificates(String unit, OpalKeyStore opalKeyStore) throws KeyStoreException {
    Map<String, Certificate> certificates = opalKeyStore.getCertificates();
    for(String alias : opalKeyStore.listCertificates()) {
      log.info("Import certificate for '{}' alias within '{}' unit", alias, unit);

      Certificate certificate = certificates.get(alias);
      SubjectCredentials.Builder builder = SubjectCredentials.Builder.create() //
          .name(unit + "-" + alias) //
          .authenticationType(SubjectCredentials.AuthenticationType.CERTIFICATE) //
          .enabled(true) //
          .group(unit);
      try {
        saveSubjectCredentials(builder.build(), certificate);
        changeUnitSubjectTypeToGroup(unit);
      } catch(Exception e) {
        // do not break the upgrade
        log.error("Unable to save user credentials: {}-{}", unit, alias, e);
      }
    }
  }

  private void saveSubjectCredentials(SubjectCredentials subjectCredentials, Certificate certificate)
      throws ConstraintViolationException, DuplicateSubjectProfileException, KeyStoreException {

    subjectCredentials.setCertificateAlias(subjectCredentials.generateCertificateAlias());

    OpalKeyStore opalKeyStore = credentialsKeyStoreService.getKeyStore();
    opalKeyStore.getKeyStore().setCertificateEntry(subjectCredentials.getCertificateAlias(), certificate);
    credentialsKeyStoreService.saveKeyStore(opalKeyStore);

    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    toSave.put(subjectCredentials, subjectCredentials);
    for(String groupName : subjectCredentials.getGroups()) {
      Group group = new Group(groupName);
      group.addSubjectCredential(subjectCredentials.getName());
      toSave.put(group, group);
    }
    orientDbService.save(toSave);

    subjectProfileService.ensureProfile(subjectCredentials.getName(), ApplicationRealm.APPLICATION_REALM);
  }

  private void changeUnitSubjectTypeToGroup(final String unit) {
    orientDbService.execute(new OrientDbService.WithinDocumentTxCallbackWithoutResult() {
      @Override
      protected void withinDocumentTxWithoutResult(ODatabaseDocumentTx db) {
        try {
          db.command(
              new OCommandSQL("update " + SubjectAcl.class.getSimpleName() + " set type = ? where principal = ?"))
              .execute(SubjectAcl.SubjectType.GROUP, unit);
        } catch (ORecordDuplicatedException e1) {
          // ignore
        } catch(Exception e) {
          // do not break the upgrade
          log.error("Unable to change permission's subject type for principal: {}", unit, e);
        }
      }
    });
  }

  private void importKeyPairs(String unit, OpalKeyStore opalKeyStore) throws KeyStoreException {
    for(String alias : opalKeyStore.listKeyPairs()) {
      log.info("Import key pair for '{}' alias within '{}' unit", alias, unit);
      KeyPair keyPair = opalKeyStore.getKeyPair(alias);
      byte[] privateKey = keyPair.getPrivate().getEncoded();
      Certificate certificate = opalKeyStore.getKeyStore().getCertificate(alias);
      for(Project project : orientDbService.list(Project.class)) {
        OpalKeyStore projectKeyStore = projectsKeyStoreService.getKeyStore(project);
        projectKeyStore.getKeyStore().setKeyEntry(alias, privateKey, new Certificate[] { certificate });
        projectsKeyStoreService.saveKeyStore(projectKeyStore);
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
