package org.obiba.opal.core.runtime.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.obiba.opal.core.domain.server.OpalGeneralConfig;
import org.obiba.opal.core.service.impl.DefaultGeneralConfigService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

public class ExtractOpalGeneralConfigToDatabaseUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(ExtractOpalGeneralConfigToDatabaseUpgradeStep.class);

  private static final String OPAL_CHARSET = "org.obiba.opal.charset.default";

  private static final String OPAL_LANGUAGES = "org.obiba.opal.languages";

  private File propertiesFile;

  private DefaultGeneralConfigService generalConfigService;

  public void setPropertiesFile(File propertiesFile) {
    this.propertiesFile = propertiesFile;
  }

  public void setGeneralConfigService(DefaultGeneralConfigService generalConfigService) {
    this.generalConfigService = generalConfigService;
  }

  @Override
  public void execute(Version currentVersion) {
    extractOpalGeneralConfig();
    commentDeprecatedProperties();
  }

  private void extractOpalGeneralConfig() {
    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream(propertiesFile));

      OpalGeneralConfig conf = new OpalGeneralConfig();
      conf.setName("Opal");
      conf.setDefaultCharacterSet(prop.getProperty(OPAL_CHARSET, "ISO-8859-1"));

      String[] languages = prop.getProperty(OPAL_LANGUAGES, "en").split(",");
      conf.setLocales(Arrays.asList(languages));

      generalConfigService.createServerConfig(conf);

      log.debug("Import general configuration: name:Opal, characterSet:{}, languages:{}", conf.getDefaultCharacterSet(),
          StringUtils.arrayToDelimitedString(languages, ","));

    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void commentDeprecatedProperties() {
    log.debug("Comment deprecated config");

    try {
      List<String> comments = Lists.newArrayList();
      comments.add("\nDeprecated server general configuration moved to Opal configuration database");

      PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);

      removeAndAddComment(OPAL_LANGUAGES, comments, config);
      removeAndAddComment(OPAL_CHARSET, comments, config);

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
}
