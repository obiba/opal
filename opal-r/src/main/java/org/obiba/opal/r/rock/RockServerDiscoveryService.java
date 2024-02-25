/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.core.cfg.AppsService;
import org.obiba.opal.core.domain.AppConfig;
import org.obiba.opal.core.event.AppUnregisteredEvent;
import org.obiba.opal.core.runtime.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scan specified hosts periodically to discore Rock R server instances or to check
 * whether they are still alive.
 */
@Component
public class RockServerDiscoveryService {

  private static final Logger log = LoggerFactory.getLogger(RockServerDiscoveryService.class);

  @Autowired
  private AppsService appsService;

  private final Map<String, App> hostsToCheck = Maps.newConcurrentMap();

  @Scheduled(fixedDelayString = "${apps.discovery.interval:10000}")
  public void scanHosts() {
    List<String> hosts = appsService.getAppsConfig().getRockAppConfigs().stream()
        .map(AppConfig::getHost).collect(Collectors.toList());
    for (String host : hosts)
      discoverOrCheckHost(host);
  }

  @Subscribe
  public synchronized void onAppUnregistered(AppUnregisteredEvent event) {
    if ("rock".equals(event.getApp().getType())) {
      hostsToCheck.remove(event.getApp().getServer());
    }
  }

  private boolean discoverOrCheckHost(String url) {
    if (hostsToCheck.containsKey(url)) {
      return checkHost(url);
    } else {
      return discoverHost(url);
    }
  }

  private boolean discoverHost(String url) {
    log.debug("Trying {} ...", url);
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response = restTemplate.getForEntity(url + "/_info", String.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        JSONObject jsonApp = new JSONObject(response.getBody());
        if (!"rock".equals(jsonApp.getString("type"))) {
          log.debug("Not a Rock R server: {}", jsonApp.toString(2));
          return false;
        }
        if (!jsonApp.has("id")) {
          log.debug("Not a valid Rock R server: {}", jsonApp.toString(2));
          return false;
        }
        App app = new App();
        app.setName(jsonApp.getString("id"));
        app.setCluster(jsonApp.getString("cluster"));
        app.setType("rock");
        JSONArray tags = jsonApp.has("tags") ? jsonApp.getJSONArray("tags") : null;
        if (tags != null && tags.length() > 0) {
          List<String> tagList = Lists.newArrayList();
          for (int i = 0; i < tags.length(); i++)
            tagList.add(tags.getString(i));
          app.setTags(tagList);
        }
        if (!jsonApp.has("server"))
          app.setServer(url);
        else
          app.setServer(jsonApp.getString("server"));
        log.debug("Discovered Rock R server: {}", app);
        appsService.registerApp(app);
        hostsToCheck.put(url, app);
        return true;
      } else {
        log.debug(">> Down! {}", response.getStatusCode());
        return false;
      }
    } catch (Exception e) {
      log.debug(">> Not found!", e);
      return false;
    }
  }

  private boolean checkHost(String url) {
    log.debug("Checking {} ...", url);
    App app = hostsToCheck.get(url);
    if (!appsService.hasApp(app.getId())) {
      hostsToCheck.remove(url);
      return false;
    }
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<Void> response = restTemplate.getForEntity(url + "/_check", Void.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        log.debug(">> OK!");
        return true;
      } else {
        log.warn("Critical Rock R server: {}", app);
        return false;
      }
    } catch (HttpServerErrorException e) {
      log.warn("Critical Rock R server: {}", app);
      return false;
    } catch (Exception e) {
      log.debug("Failing Rock R server: {}", app);
      hostsToCheck.remove(url);
      appsService.unregisterApp(app);
      return false;
    }
  }
}
