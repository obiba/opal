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
import org.obiba.opal.core.repository.AppRepository;
import org.obiba.opal.core.repository.AppsConfigRepository;
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

  private final AppRepository appRepository;

  private final AppsConfigRepository appsConfigRepository;

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
  public AppsServiceImpl(AppRepository appRepository, AppsConfigRepository appsConfigRepository, EventBus eventBus) {
    this.appRepository = appRepository;
    this.appsConfigRepository = appsConfigRepository;
    this.eventBus = eventBus;
  }

  @Override
  public void registerApp(App app) {
    registryLock.lock();
    try {
      List<App> existing = findApps(app);
      if (existing.isEmpty()) {
        app.setId(UUID.randomUUID().toString());
        appRepository.upsert(app);
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
        appRepository.delete(app);
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
        appRepository.delete(app);
      }
    } finally {
      registryLock.unlock();
    }
  }

  @Override
  public List<App> getApps() {
    return appRepository.findAll();
  }

  @Override
  public List<App> getApps(String type) {
    if (Strings.isNullOrEmpty(type)) return getApps();
    return appRepository.findByType(type);
  }

  @Override
  public App getApp(String id) {
    if (Strings.isNullOrEmpty(id)) throw new NoSuchElementException("No registered app with ID: " + id);
    return appRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("No registered app with ID: " + id));
  }

  /**
   * An application that was never registered has no identifier, and the answer for it is no. The document store gave
   * that answer by itself - a lookup on a null key simply matched nothing - where a repository rejects the null
   * outright, so the question has to be settled here.
   */
  @Override
  public boolean hasApp(String id) {
    return !Strings.isNullOrEmpty(id) && appRepository.existsById(id);
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
    return appsConfigRepository.findConfig().orElseGet(this::getDefaultAppsConfig);
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
      appsConfigRepository.deleteAll();
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
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        appRepository.findAll().forEach(app -> eventBus.post(new AppRegisteredEvent(app)));
      }
    }, 5000);
  }

  @Override
  public void stop() {
    appRepository.findAll().forEach(app -> eventBus.post(new AppUnregisteredEvent(app)));
  }

  //
  // Private methods
  //

  private List<App> findApps(App template) {
    return appRepository.findByNameAndTypeAndServer(template.getName(), template.getType(), template.getServer());
  }

  private void saveAppsConfig(AppsConfig config) {
    appsConfigRepository.upsert(config);
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
