package org.obiba.opal.core.runtime.upgrade.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class ExtractOpalJdbcConfigToDatabaseUpgradeStep extends AbstractUpgradeStep {

  private File configFile;

  private File propertiesFile;

  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    addOpalConfigProperties();
    extractOpalDatasource();
    importExtraDatasources();
    deleteJdbcDataSourcesFromConfig();
  }

  private void addOpalConfigProperties() {
    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream(propertiesFile));
      prop.setProperty("org.obiba.opal.datasource.driver", "org.hsqldb.jdbcDriver");
      prop.setProperty("org.obiba.opal.datasource.url", "jdbc:hsqldb:file:opal_config;shutdown=true;hsqldb.tx=mvcc");
      prop.setProperty("org.obiba.opal.datasource.username", "sa");
      prop.setProperty("org.obiba.opal.datasource.password", null);
      prop.setProperty("org.obiba.opal.datasource.dialect", "org.hibernate.dialect.HSQLDialect");
      prop.setProperty("org.obiba.opal.datasource.validationQuery", "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
      prop.store(new FileOutputStream(propertiesFile), null);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void extractOpalDatasource() {
    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream(propertiesFile));

      SqlDatabase opalData = new SqlDatabase.Builder() //
          .name("opal-data") //
          .url(prop.getProperty("org.obiba.opal.datasource.opal.url")) //
          .driverClass(prop.getProperty("org.obiba.opal.datasource.opal.driver")) //
          .username(prop.getProperty("org.obiba.opal.datasource.opal.username")) //
          .password(prop.getProperty("org.obiba.opal.datasource.opal.password")) //
          .editable(false) //
          .build();
      databaseRegistry.addOrReplaceDatabase(opalData);

      SqlDatabase opalKey = new SqlDatabase.Builder() //
          .name("opal-key") //
          .url(prop.getProperty("org.obiba.opal.datasource.key.url")) //
          .driverClass(prop.getProperty("org.obiba.opal.datasource.key.driver")) //
          .username(prop.getProperty("org.obiba.opal.datasource.key.username")) //
          .password(prop.getProperty("org.obiba.opal.datasource.key.password")) //
          .editable(false) //
          .build();
      databaseRegistry.addOrReplaceDatabase(opalKey);

      deleteDeprecatedProperties(prop);

      prop.store(new FileOutputStream(propertiesFile), null);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void deleteDeprecatedProperties(Properties prop) {
    prop.remove("org.obiba.opal.datasource.opal.url");
    prop.remove("org.obiba.opal.datasource.opal.driver");
    prop.remove("org.obiba.opal.datasource.opal.username");
    prop.remove("org.obiba.opal.datasource.opal.password");
    prop.remove("org.obiba.opal.datasource.opal.dialect");
    prop.remove("org.obiba.opal.datasource.opal.validationQuery");

    prop.remove("org.obiba.opal.datasource.key.url");
    prop.remove("org.obiba.opal.datasource.key.driver");
    prop.remove("org.obiba.opal.datasource.key.username");
    prop.remove("org.obiba.opal.datasource.key.password");
    prop.remove("org.obiba.opal.datasource.key.dialect");
    prop.remove("org.obiba.opal.datasource.key.validationQuery");
  }

  private void importExtraDatasources() {
    try {
      for(JdbcDataSource dataSource : getJdbcDataSourcesConfig()) {
        SqlDatabase sqlDatabase = new SqlDatabase.Builder() //
            .name(dataSource.name) //
            .url(dataSource.url) //
            .driverClass(dataSource.driverClass) //
            .username(dataSource.username) //
            .password(dataSource.password) //
            .properties(dataSource.properties) //
            .editable(dataSource.editable) //
            .build();
        databaseRegistry.addOrReplaceDatabase(sqlDatabase);
      }
    } catch(ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch(SAXException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Iterable<JdbcDataSource> getJdbcDataSourcesConfig()
      throws ParserConfigurationException, SAXException, IOException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    JdbcDataSourceHandler handler = new JdbcDataSourceHandler();
    saxParser.parse(configFile, handler);
    return handler.getDataSources();
  }

  private void deleteJdbcDataSourcesFromConfig() {
    try {
      SAXReader reader = new SAXReader();
      reader.setEncoding(Charsets.UTF_8.name());
      Document document = reader.read(configFile);

      Node node = document
          .selectSingleNode("org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry_-JdbcDataSourcesConfig");
      document.remove(node);

      FileWriter writer = new FileWriter(configFile);
      document.write(writer);
      writer.close();

    } catch(DocumentException e) {
      throw new RuntimeException(e);
    } catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
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

  //  <org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry_-JdbcDataSourcesConfig>
  //    <datasources>
  //      <org.obiba.opal.core.runtime.jdbc.JdbcDataSource>
  //        <name>LimeSurvey</name>
  //        <url>jdbc:mysql://localhost:3306/lime</url>
  //        <driverClass>com.mysql.jdbc.Driver</driverClass>
  //        <username>root</username>
  //        <password>1234</password>
  //        <properties/>
  //        <editable>true</editable>
  //      </org.obiba.opal.core.runtime.jdbc.JdbcDataSource>
  //    </datasources>
  //  </org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry_-JdbcDataSourcesConfig>
  private static class JdbcDataSourceHandler extends DefaultHandler {

    private final List<JdbcDataSource> dataSources = Lists.newArrayList();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if("org.obiba.opal.core.runtime.jdbc.JdbcDataSource".equals(qName)) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.name = attributes.getValue("name");
        ds.url = attributes.getValue("url");
        ds.driverClass = attributes.getValue("driverClass");
        ds.username = attributes.getValue("username");
        ds.password = attributes.getValue("password");
        ds.properties = attributes.getValue("properties");
        ds.editable = Boolean.valueOf(attributes.getValue("editable"));
        dataSources.add(ds);
      }
    }

    private Iterable<JdbcDataSource> getDataSources() {
      return dataSources;
    }
  }

  private static class JdbcDataSource {
    String name;

    String url;

    String driverClass;

    String username;

    String password;

    String properties;

    boolean editable;
  }

}
