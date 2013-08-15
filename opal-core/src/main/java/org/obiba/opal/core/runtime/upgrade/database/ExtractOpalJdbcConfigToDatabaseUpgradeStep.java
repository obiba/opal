package org.obiba.opal.core.runtime.upgrade.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;
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
    commentDeprecatedProperties();
  }

  private void addOpalConfigProperties() {
    try {
      PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
      PropertiesConfigurationLayout layout = config.getLayout();
      config.setProperty("org.obiba.opal.datasource.driver", "org.hsqldb.jdbcDriver");
      config
          .setProperty("org.obiba.opal.datasource.url", "jdbc:hsqldb:file:opal_config.db;shutdown=true;hsqldb.tx=mvcc");
      config.setProperty("org.obiba.opal.datasource.username", "sa");
      config.setProperty("org.obiba.opal.datasource.password", "");
      config.setProperty("org.obiba.opal.datasource.dialect", "org.hibernate.dialect.HSQLDialect");
      config.setProperty("org.obiba.opal.datasource.validationQuery", "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
      layout.setComment("org.obiba.opal.datasource.driver", "\nOpal internal database settings");
      config.save(propertiesFile);
    } catch(ConfigurationException e) {
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

    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void commentDeprecatedProperties() {
    try {
      List<String> comments = Lists.newArrayList();
      comments.add("\nDeprecated datasources configuration moved to Opal configuration database");

      PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);

      removeAndAddComment("org.obiba.opal.datasource.opal.url", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.opal.driver", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.opal.username", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.opal.password", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.opal.dialect", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.opal.validationQuery", comments, config);

      removeAndAddComment("org.obiba.opal.datasource.key.url", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.key.driver", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.key.username", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.key.password", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.key.dialect", comments, config);
      removeAndAddComment("org.obiba.opal.datasource.key.validationQuery", comments, config);

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
