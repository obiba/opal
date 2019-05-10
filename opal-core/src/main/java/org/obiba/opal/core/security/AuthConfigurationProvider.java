/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

@Component
public class AuthConfigurationProvider implements OIDCConfigurationProvider {

  private static final Logger log = LoggerFactory.getLogger(AuthConfigurationProvider.class);

  private static final String OIDC_CONF_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "conf" + File.separatorChar + "oidc";

  List<OIDCConfiguration> configurations = Lists.newArrayList();

  @PostConstruct
  public void init() {
    File confDir = new File(OIDC_CONF_DIR);
    if (confDir.exists() && confDir.isDirectory()) {
      File[] confFiles = confDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File file) {
          return file.isFile() && file.getName().endsWith(".json");
        }
      });
      if (confFiles != null) {
        ObjectMapper mapper = new ObjectMapper();
        for (File confFile : confFiles) {
          try {
            OIDCConfiguration conf = mapper.readValue(confFile, OIDCConfiguration.class);
            log.info("Registering ID provider: {}", conf.getName());
            configurations.add(conf);
          } catch (Exception e) {
            log.error("Cannot read OIDConfiguration: {}", confFile.getName());
          }
        }
      }
    }
  }

  @Override
  public List<OIDCConfiguration> getConfigurations() {
    return configurations;
  }

  @Override
  public OIDCConfiguration getConfiguration(String name) {
    if (Strings.isNullOrEmpty(name)) return null;
    return configurations.stream().filter(conf -> name.equals(conf.getName())).findFirst().orElse(null);
  }
}
