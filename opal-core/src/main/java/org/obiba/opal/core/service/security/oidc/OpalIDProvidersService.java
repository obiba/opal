/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.shiro.realm.OIDCRealm;
import org.obiba.opal.core.service.security.DuplicateIDProviderException;
import org.obiba.opal.core.service.security.IDProvidersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class OpalIDProvidersService implements IDProvidersService, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(OpalIDProvidersService.class);

  private static final String OIDC_CONF_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "conf" + File.separatorChar + "oidc";

  private static final ObjectMapper mapper = new ObjectMapper();

  private final Map<String, OIDCConfiguration> configurations = Maps.newConcurrentMap();

  public void init() {
    File confDir = getConfigurationDirectory();
    File[] confFiles = confDir.listFiles(file -> file.isFile() && file.getName().endsWith(".json"));
    if (confFiles != null) {
      for (File confFile : confFiles) {
        try {
          OIDCConfiguration conf = mapper.readValue(confFile, OIDCConfiguration.class);
          if (!Strings.isNullOrEmpty(conf.getName())) {
            log.info("Registering ID provider: {}", conf.getName());
            configurations.put(conf.getName(), conf);
          }
        } catch (Exception e) {
          log.error("Cannot read OIDConfiguration: {}", confFile.getName());
        }
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    init();
  }

  @Override
  public void saveConfiguration(OIDCConfiguration configuration) throws IOException {
    boolean update = configurations.containsKey(configuration.getName());
    configurations.put(configuration.getName(), configuration);
    mapper.writeValue(getConfigurationFile(configuration.getName()), configuration);
    if (update) {
      removeRealm(configuration.getName());
    }
    if ("true".equals(configuration.getCustomParam("enabled"))) {
      RealmSecurityManager securityManager = ((RealmSecurityManager) SecurityUtils.getSecurityManager());
      Collection<Realm> realms = securityManager.getRealms();
      realms.add(new OIDCRealm(configuration));
      securityManager.setRealms(realms);
    }
  }

  @Override
  public void deleteConfiguration(String name) {
    if (Strings.isNullOrEmpty(name)) return;
    if (configurations.containsKey(name)) {
      File confFile = getConfigurationFile(configurations.get(name).getName());
      if (confFile.exists()) confFile.delete();
      configurations.remove(name);
      removeRealm(name);
    }
  }

  @Override
  public void enableConfiguration(String name, boolean enable) throws IOException {
    OIDCConfiguration configuration = getConfiguration(name);
    configuration.getCustomParams().put("enabled", enable + "");
    saveConfiguration(configuration);
  }

  @Override
  public Collection<OIDCConfiguration> getConfigurations() {
    return configurations.keySet().stream().sorted().map(configurations::get).collect(Collectors.toList());
  }

  @Override
  public void ensureUniqueConfiguration(String name) throws DuplicateIDProviderException {
    if (configurations.containsKey(Strings.nullToEmpty(name))) {
      throw new DuplicateIDProviderException(configurations.get(name));
    }
  }

  @Override
  public OIDCConfiguration getConfiguration(String name) {
    if (Strings.isNullOrEmpty(name) || !configurations.containsKey(name))
      throw new NoSuchElementException("No OIDC configuration with name: " + name);
    return configurations.get(name);
  }

  private void removeRealm(String name) {
    RealmSecurityManager securityManager = ((RealmSecurityManager) SecurityUtils.getSecurityManager());
    Collection<Realm> realms = securityManager.getRealms();
    realms.stream()
        .filter(realm -> realm.getName().equals(name))
        .findFirst().ifPresent(realms::remove);
    securityManager.setRealms(realms);
  }

  private File getConfigurationDirectory() {
    File confDir = new File(OIDC_CONF_DIR);
    if (!confDir.exists()) confDir.mkdirs();
    return confDir;
  }

  private File getConfigurationFile(String name) {
    return new File(getConfigurationDirectory(), name + ".json");
  }


}
