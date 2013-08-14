package org.obiba.opal.core.runtime.upgrade.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
          .usedForIdentifiers(true) //
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
    } catch(XPathExpressionException e) {
      throw new RuntimeException(e);
    } catch(ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch(SAXException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
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
  private Iterable<JdbcDataSource> getJdbcDataSourcesConfig()
      throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

    List<JdbcDataSource> dataSources = Lists.newArrayList();

    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
    XPath xPath = XPathFactory.newInstance().newXPath();
    NodeList nodeList = (NodeList) xPath.compile("//org.obiba.opal.core.runtime.jdbc.JdbcDataSource")
        .evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
    for(int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      NodeList childNodes = node.getChildNodes();
      BeanWrapper wrapper = new BeanWrapperImpl(new JdbcDataSource());
      for(int j = 0; j < childNodes.getLength(); j++) {
        Node child = childNodes.item(j);
        if(child.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) child;
          wrapper.setPropertyValue(element.getTagName(), element.getTextContent());
        }
      }
      dataSources.add((JdbcDataSource) wrapper.getWrappedInstance());
    }
    return dataSources;
  }

  private void deleteJdbcDataSourcesFromConfig() {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
      XPath xPath = XPathFactory.newInstance().newXPath();
      Node node = (Node) xPath
          .compile("//org.obiba.opal.core.runtime.jdbc.DefaultJdbcDataSourceRegistry_-JdbcDataSourcesConfig")
          .evaluate(doc.getDocumentElement(), XPathConstants.NODE);
      node.getParentNode().removeChild(node);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(doc), new StreamResult(configFile));
    } catch(TransformerConfigurationException e) {
      throw new RuntimeException(e);
    } catch(SAXException e) {
      throw new RuntimeException(e);
    } catch(ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch(XPathExpressionException e) {
      throw new RuntimeException(e);
    } catch(TransformerException e) {
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

  @SuppressWarnings("UnusedDeclaration")
  private static class JdbcDataSource {

    private String name;

    private String url;

    private String driverClass;

    private String username;

    private String password;

    private String properties;

    private boolean editable;

    public String getDriverClass() {
      return driverClass;
    }

    public void setDriverClass(String driverClass) {
      this.driverClass = driverClass;
    }

    public boolean isEditable() {
      return editable;
    }

    public void setEditable(boolean editable) {
      this.editable = editable;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getProperties() {
      return properties;
    }

    public void setProperties(String properties) {
      this.properties = properties;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

  }

}
