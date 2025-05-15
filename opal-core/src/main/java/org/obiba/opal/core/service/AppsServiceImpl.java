/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.shiro.authz.UnauthorizedException;
import org.obiba.opal.core.cfg.AppsService;
import org.obiba.opal.core.domain.AppsConfig;
import org.obiba.opal.core.domain.RockAppConfig;
import org.obiba.opal.core.event.AppRegisteredEvent;
import org.obiba.opal.core.event.AppRejectedEvent;
import org.obiba.opal.core.event.AppUnregisteredEvent;
import org.obiba.opal.core.runtime.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AppsServiceImpl implements AppsService {

  private static final Logger log = LoggerFactory.getLogger(AppsServiceImpl.class);

  private final OrientDbService orientDbService;

  private final EventBus eventBus;

  @Value("${apps.registration.token}")
  private String defaultToken;

  @Value("${apps.registration.include}")
  private String registrationInclude;

  @Value("${apps.registration.exclude}")
  private String registrationExclude;

  @Value("${apps.discovery.rock.hosts}")
  private String[] defaultRockHosts;

  private final Lock registryLock = new ReentrantLock();

  private final Lock configLock = new ReentrantLock();

  @Autowired
  public AppsServiceImpl(OrientDbService orientDbService, EventBus eventBus) {
    this.orientDbService = orientDbService;
    this.eventBus = eventBus;
  }

  @Override
  public void registerApp(App app) {
    registryLock.lock();
    try {
      List<App> existing = findApps(app);
      if (existing.isEmpty()) {
        app.setId(UUID.randomUUID().toString());
        orientDbService.save(app, app);
        eventBus.post(new AppRegisteredEvent(app));
      }
    } finally {
      registryLock.unlock();
    }
  }

  @Override
  public void unregisterApp(App app) {
    registryLock.lock();
    try {
      if (Strings.isNullOrEmpty(app.getId())) {
        findApps(app).forEach(this::unregisterApp);
      } else {
        orientDbService.delete(app);
        eventBus.post(new AppUnregisteredEvent(app));
      }
    } finally {
      registryLock.unlock();
    }
  }

  @Subscribe
  public synchronized void onAppRejected(AppRejectedEvent event) {
    registryLock.lock();
    try {
      App app = event.getApp();
      if (Strings.isNullOrEmpty(app.getId())) {
        findApps(app).forEach(this::unregisterApp);
      } else {
        orientDbService.delete(app);
      }
    } finally {
      registryLock.unlock();
    }
  }

  @Override
  public List<App> getApps() {
    List<App> apps = Lists.newArrayList(orientDbService.list(App.class));
    return apps;
  }

  @Override
  public List<App> getApps(String type) {
    if (Strings.isNullOrEmpty(type)) return getApps();
    return Lists.newArrayList(orientDbService.list(App.class,
        String.format("select from %s where type = ?", App.class.getSimpleName()), type));
  }

  @Override
  public App getApp(String id) {
    App found = orientDbService.findUnique(new App(id));
    if (found != null) return found;
    throw new NoSuchElementException("No registered app with ID: " + id);
  }

  @Override
  public boolean hasApp(String id) {
    App app = orientDbService.findUnique(new App(id));
    return app != null;
  }

  @Override
  public void checkToken(String value) {
    if (Strings.isNullOrEmpty(value) || !value.equals(getAppsConfig().getToken()))
      throw new UnauthorizedException("App registration operation not authorized");
  }

  @Override
  public void checkSelfRegistrationRules(String server) {
    if (!Strings.isNullOrEmpty(registrationInclude)) {
      Pattern p = Pattern.compile(registrationInclude);
      Matcher m = p.matcher(server);
      if (!m.matches()) {
        log.info("App server {} does not pass the white list rule", server);
        throw new UnauthorizedException("App registration operation not authorized");
      }
    }
    if (!Strings.isNullOrEmpty(registrationExclude)) {
      Pattern p = Pattern.compile(registrationExclude);
      Matcher m = p.matcher(server);
      if (m.matches()) {
        log.info("App server {} does not pass the black list rule", server);
        throw new UnauthorizedException("App registration operation not authorized");
      }
    }
  }

  public AppsConfig getAppsConfig() {
    AppsConfig found = orientDbService.findUnique(new AppsConfig());
    return found == null ? getDefaultAppsConfig() : found;
  }

  @Override
  public void updateAppsConfig(AppsConfig config) {
    configLock.lock();
    try {
      saveAppsConfig(config);
    } finally {
      configLock.unlock();
    }
  }

  @Override
  public void resetConfig() {
    configLock.lock();
    try {
      orientDbService.deleteAll(AppsConfig.class);
    } finally {
      configLock.unlock();
    }
  }

  @Override
  public RockAppConfig getRockAppConfig(App app) {
    AppsConfig config = getAppsConfig();
    for (RockAppConfig rockConfig : config.getRockAppConfigs()) {
      if (app.getServer().equals(rockConfig.getHost())) {
        return rockConfig;
      }
    }
    return new RockAppConfig(app.getServer());
  }

  @Override
  public void start() {
    orientDbService.createUniqueIndex(AppsConfig.class);
    orientDbService.createUniqueIndex(App.class);

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        orientDbService.list(App.class).forEach(app -> eventBus.post(new AppRegisteredEvent(app)));
      }
    }, 5000);
  }

  @Override
  public void stop() {
    orientDbService.list(App.class).forEach(app -> eventBus.post(new AppUnregisteredEvent(app)));
  }

  //
  // Private methods
  //

  private List<App> findApps(App template) {
    return Lists.newArrayList(orientDbService.list(App.class,
        String.format("select from %s where name = ? and type = ? and server = ?", App.class.getSimpleName()),
        template.getName(), template.getType(), template.getServer()));
  }

  private void saveAppsConfig(AppsConfig config) {
    orientDbService.save(config, config);
  }

  private AppsConfig getDefaultAppsConfig() {
    AppsConfig config = new AppsConfig();
    config.setToken(defaultToken);
    for (String host : defaultRockHosts) {
      config.addRockAppConfig(new RockAppConfig(host));
    }
    return config;
  }

}
