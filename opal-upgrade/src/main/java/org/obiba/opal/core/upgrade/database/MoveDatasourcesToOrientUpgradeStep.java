package org.obiba.opal.core.upgrade.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.project.domain.Project;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

public class MoveDatasourcesToOrientUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(MoveDatasourcesToOrientUpgradeStep.class);

  private static final String OPAL_URL = "org.obiba.opal.datasource.opal.url";

  private static final String OPAL_DRIVER = "org.obiba.opal.datasource.opal.driver";

  private static final String OPAL_USERNAME = "org.obiba.opal.datasource.opal.username";

  private static final String OPAL_PASSWORD = "org.obiba.opal.datasource.opal.password";

  private static final String OPAL_DIALECT = "org.obiba.opal.datasource.opal.dialect";

  private static final String OPAL_VALIDATION_QUERY = "org.obiba.opal.datasource.opal.validationQuery";

  private static final String KEY_URL = "org.obiba.opal.datasource.key.url";

  private static final String KEY_DRIVER = "org.obiba.opal.datasource.key.driver";

  private static final String KEY_USERNAME = "org.obiba.opal.datasource.key.username";

  private static final String KEY_PASSWORD = "org.obiba.opal.datasource.key.password";

  private static final String KEY_DIALECT = "org.obiba.opal.datasource.key.dialect";

  private static final String KEY_VALIDATION_QUERY = "org.obiba.opal.datasource.key.validationQuery";

  private File configFile;

  private File propertiesFile;

  private DatabaseRegistry databaseRegistry;

  private OrientDbService orientDbService;

  @Override
  public void execute(Version currentVersion) {
    initOrientProjectTable();
    extractOpalDatasource();
    importDatasourceFactories();
    commentDeprecatedProperties();
  }

  /**
   * Init OrientDB project table here as we cannot inject ProjectService that depends on OpalRuntime...
   */
  private void initOrientProjectTable() {
    orientDbService.registerEntityClass(Project.class);
    orientDbService.createUniqueStringIndex(Project.class, "name");
  }

  private void extractOpalDatasource() {
    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream(propertiesFile));

      Database opalData = Database.Builder.create() //
          .name("opal-data") //
          .sqlSettings(SqlSettings.Builder.create() //
              .url(prop.getProperty(OPAL_URL)) //
              .driverClass(prop.getProperty(OPAL_DRIVER)) //
              .username(prop.getProperty(OPAL_USERNAME)) //
              .password(prop.getProperty(OPAL_PASSWORD)) //
              .sqlSchema(SqlSettings.SqlSchema.HIBERNATE)) //
          .editable(false) //
          .usage(Database.Usage.STORAGE) //

          .build();
      log.debug("Import opalData: {}", opalData);
      databaseRegistry.saveDatabase(opalData);
      orientDbService.save(Project.Builder.create().name("opal-data").title("opal-data").database("opal-data").build());

      Database opalKey = Database.Builder.create() //
          .name("_identifiers") //
          .sqlSettings(SqlSettings.Builder.create() //
              .url(prop.getProperty(KEY_URL)) //
              .driverClass(prop.getProperty(KEY_DRIVER)) //
              .username(prop.getProperty(KEY_USERNAME)) //
              .password(prop.getProperty(KEY_PASSWORD)) //
              .sqlSchema(SqlSettings.SqlSchema.HIBERNATE)) //
          .editable(false) //
          .usedForIdentifiers(true) //
          .usage(Database.Usage.STORAGE) //
          .build();
      log.debug("Import opalKey: {}", opalKey);

      databaseRegistry.saveDatabase(opalKey);

    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void commentDeprecatedProperties() {
    log.debug("Comment deprecated config");

    try {
      List<String> comments = Lists.newArrayList();
      comments.add("\nDeprecated datasources configuration moved to Opal configuration database");

      PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);

      removeAndAddComment(OPAL_URL, comments, config);
      removeAndAddComment(OPAL_DRIVER, comments, config);
      removeAndAddComment(OPAL_USERNAME, comments, config);
      removeAndAddComment(OPAL_PASSWORD, comments, config);
      removeAndAddComment(OPAL_DIALECT, comments, config);
      removeAndAddComment(OPAL_VALIDATION_QUERY, comments, config);

      removeAndAddComment(KEY_URL, comments, config);
      removeAndAddComment(KEY_DRIVER, comments, config);
      removeAndAddComment(KEY_USERNAME, comments, config);
      removeAndAddComment(KEY_PASSWORD, comments, config);
      removeAndAddComment(KEY_DIALECT, comments, config);
      removeAndAddComment(KEY_VALIDATION_QUERY, comments, config);

      config.setHeader(config.getHeader() + "\n" + StringUtils.collectionToDelimitedString(comments, "\n"));
      config.save(propertiesFile);
    } catch(ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  private void removeAndAddComment(String key, Collection<String> comments, Configuration config) {
    comments.add(key + " = " + config.getProperty(key));
    config.clearProperty(key);
  }

  /*
    <factories>
      <org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory>
        <name>opal-data</name>
        <sessionFactoryProvider class="org.obiba.magma.datasource.hibernate.support.SpringBeanSessionFactoryProvider">
          <beanName>opalSessionFactory</beanName>
        </sessionFactoryProvider>
      </org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory>
      <org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory>
        <name>data2</name>
        <sessionFactoryProvider class="org.obiba.opal.web.magma.support.DatabaseSessionFactoryProvider">
          <datasourceName>data2</datasourceName>
          <databaseName>database-mysql1</databaseName>
        </sessionFactoryProvider>
      </org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory>
      <org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory>
        <name>opal-data-2</name>
        <sessionFactoryProvider class="org.obiba.magma.datasource.hibernate.support.SpringBeanSessionFactoryProvider">
          <beanName>opalSessionFactory</beanName>
        </sessionFactoryProvider>
      </org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory>
      <org.obiba.opal.web.magma.support.DatabaseJdbcDatasourceFactory>
        <name>opal-custom</name>
        <databaseName>opal-custom</databaseName>
        <settings>
        <defaultEntityType>Participant</defaultEntityType>
        <mappedTables/>
        <tableSettings/>
        <useMetadataTables>false</useMetadataTables>
      </settings>
      </org.obiba.opal.web.magma.support.DatabaseJdbcDatasourceFactory>
        <org.obiba.magma.datasource.nil.support.NullDatasourceFactory>
        <name>no-storage</name>
      </org.obiba.magma.datasource.nil.support.NullDatasourceFactory>
    </factories>
  */
  private void importDatasourceFactories() {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
      XPath xPath = XPathFactory.newInstance().newXPath();
      importJdbcDataSources(doc, xPath);
      importHibernateDatasourceFactories(doc, xPath);
      importJdbcDatasourceFactories(doc, xPath);
      importNullDatasourceFactories(doc, xPath);
      deleteDeprecatedNodes(doc, xPath);
    } catch(SAXException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    } catch(ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch(XPathExpressionException e) {
      throw new RuntimeException(e);
    } catch(TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  /*
  <org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry_-JdbcDataSourcesConfig>
    <datasources>
      <org.obiba.opal.core.runtime.jdbc.JdbcDataSource>
        <name>LimeSurvey</name>
        <url>jdbc:mysql://localhost:3306/lime</url>
        <driverClass>com.mysql.jdbc.Driver</driverClass>
        <username>root</username>
        <password>1234</password>
        <properties/>
        <editable>true</editable>
      </org.obiba.opal.core.runtime.jdbc.JdbcDataSource>
    </datasources>
  </org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry_-JdbcDataSourcesConfig>
*/
  private void importJdbcDataSources(Document doc, XPath xPath) throws XPathExpressionException {
    NodeList nodeList = (NodeList) xPath.compile("//org.obiba.opal.core.runtime.jdbc.JdbcDataSource")
        .evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
    for(int i = 0; i < nodeList.getLength(); i++) {
      Element element = (Element) nodeList.item(i);
      Database sqlDatabase = Database.Builder.create() //
          .name(getChildTextContent(element, "name")) //
          .sqlSettings(SqlSettings.Builder.create() //
              .url(getChildTextContent(element, "url")) //
              .driverClass(getChildTextContent(element, "driverClass")) //
              .username(getChildTextContent(element, "username")) //
              .password(getChildTextContent(element, "password")) //
              .sqlSchema(SqlSettings.SqlSchema.HIBERNATE)) //
          .editable(Boolean.valueOf(getChildTextContent(element, "editable"))) //
          .usage(Database.Usage.STORAGE) //
          .build();
      log.debug("Import database: {}", sqlDatabase);
      databaseRegistry.saveDatabase(sqlDatabase);
    }
  }

  private String getChildTextContent(Element element, String child) {
    NodeList children = element.getElementsByTagName(child);
    return children.getLength() == 1 ? children.item(0).getTextContent() : null;
  }

  private void importNullDatasourceFactories(Document doc, XPath xPath) throws XPathExpressionException {
    NodeList nodeList = (NodeList) xPath.compile("//org.obiba.magma.datasource.nil.support.NullDatasourceFactory/name")
        .evaluate(doc, XPathConstants.NODESET);
    for(int i = 0; i < nodeList.getLength(); i++) {
      String name = nodeList.item(i).getTextContent();
      orientDbService.save(Project.Builder.create().name(name).title(name).build());
    }
  }

  private void importHibernateDatasourceFactories(Document doc, XPath xPath) throws XPathExpressionException {
    NodeList nodeList = (NodeList) xPath
        .compile("//org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory")
        .evaluate(doc, XPathConstants.NODESET);
    for(int i = 0; i < nodeList.getLength(); i++) {
      parseHibernateDatasourceFactory((Element) nodeList.item(i));
    }
  }

  private void parseHibernateDatasourceFactory(Element factoryElement) {
    String name = getChildTextContent(factoryElement, "name");
    if(!"opal-data".equals(name)) { // skip opal-data as it was already imported when reading opal-config.properties
      Project.Builder projectBuilder = Project.Builder.create().name(name).title(name);
      Element sessionFactoryElement = (Element) factoryElement.getElementsByTagName("sessionFactoryProvider").item(0);
      String clazz = sessionFactoryElement.getAttribute("class");
      if("org.obiba.magma.datasource.hibernate.support.SpringBeanSessionFactoryProvider".equals(clazz)) {
        // based on main database opal-data
        projectBuilder.database("opal-data");
      } else if("org.obiba.opal.web.magma.support.DatabaseSessionFactoryProvider".equals(clazz)) {
        String databaseName = getChildTextContent(sessionFactoryElement, "databaseName");
        projectBuilder.database(databaseName);
      } else {
        throw new IllegalArgumentException("Unknown sessionFactoryProviderClass: " + clazz);
      }
      orientDbService.save(projectBuilder.build());
    }
  }

  private void importJdbcDatasourceFactories(Document doc, XPath xPath) throws XPathExpressionException {
    NodeList nodeList = (NodeList) xPath.compile("//org.obiba.opal.web.magma.support.DatabaseJdbcDatasourceFactory")
        .evaluate(doc, XPathConstants.NODESET);
    for(int i = 0; i < nodeList.getLength(); i++) {
      Element element = (Element) nodeList.item(i);

      String name = getChildTextContent(element, "name");
      String databaseName = getChildTextContent(element, "databaseName");
      Database database = databaseRegistry.getDatabase(databaseName);
      if(database.getSqlSettings() == null) {
        throw new IllegalArgumentException("Cannot find SqlSettings for database " + databaseName);
      }
      database.getSqlSettings().setSqlSchema(SqlSettings.SqlSchema.JDBC);
      database.setUsage(Database.Usage.IMPORT);

      Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);

      JdbcDatasourceSettings settings = new JdbcDatasourceSettings();
      settings.setDefaultEntityType(getChildTextContent(settingsElement, "defaultEntityType"));
      settings.setUseMetadataTables(Boolean.valueOf(getChildTextContent(settingsElement, "useMetadataTables")));
      database.getSqlSettings().setJdbcDatasourceSettings(settings);
      databaseRegistry.saveDatabase(database);

      if(database.getUsage() == Database.Usage.STORAGE) {
        orientDbService.save(Project.Builder.create().name(name).title(name).database(databaseName).build());
      }
    }
  }

  private void deleteDeprecatedNodes(Document doc, XPath xPath) throws XPathExpressionException, TransformerException {
    deleteNode(doc, xPath, "//magmaEngineFactory/factories");
    deleteNode(doc, xPath, "//magmaEngineFactory/datasources");
    deleteNode(doc, xPath, "//org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry_-JdbcDataSourcesConfig");
    deleteNode(doc, xPath, "//binariesMigrated");
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(new DOMSource(doc), new StreamResult(configFile));
  }

  private void deleteNode(Document doc, XPath xPath, String expression) throws XPathExpressionException {
    Node node = (Node) xPath.compile(expression).evaluate(doc.getDocumentElement(), XPathConstants.NODE);
    if(node != null) node.getParentNode().removeChild(node);
  }

  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }

  public void setPropertiesFile(File propertiesFile) {
    this.propertiesFile = propertiesFile;
  }

  public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  public void setOrientDbService(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

}
