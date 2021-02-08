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
import org.obiba.opal.core.event.AppRegisteredEvent;
import org.obiba.opal.core.event.AppRejectedEvent;
import org.obiba.opal.core.event.AppUnregisteredEvent;
import org.obiba.opal.core.runtime.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class AppsServiceImpl implements AppsService {

  private static final Logger log = LoggerFactory.getLogger(AppsServiceImpl.class);

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private EventBus eventBus;

  @Value("${apps.token}")
  private String token;

  private final Lock registryLock = new ReentrantLock();

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
    return Lists.newArrayList(orientDbService.list(App.class));
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
  public void checkToken(String value) {
    if (Strings.isNullOrEmpty(value) || !value.equals(token))
      throw new UnauthorizedException("App registration operation not authorized");
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(App.class);

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        orientDbService.list(App.class).forEach(app -> eventBus.post(new AppRegisteredEvent(app)));
      }
    }, 5000);
  }

  @Override
  @PreDestroy
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

}
