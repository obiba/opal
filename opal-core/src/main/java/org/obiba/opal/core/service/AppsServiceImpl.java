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
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.cfg.AppsService;
import org.obiba.opal.core.event.AppRegistered;
import org.obiba.opal.core.event.AppUnregistered;
import org.obiba.opal.core.runtime.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class AppsServiceImpl implements AppsService {

    @Autowired
    private EventBus eventBus;

    private final Map<String, App> registry = Maps.newHashMap();

    @Override
    public void registerApp(App app) {
        registry.put(app.getName(), app);
        eventBus.post(new AppRegistered(app));
    }

    @Override
    public void unregisterApp(String name) {
        if (registry.containsKey(name)) {
            App app = registry.get(name);
            registry.remove(name);
            eventBus.post(new AppUnregistered(app));
        }
    }

    @Override
    public List<App> getApps() {
        return Lists.newArrayList(registry.values());
    }

    @Override
    public List<App> getApps(String type) {
        if (Strings.isNullOrEmpty(type)) return getApps();
        return registry.values().stream().filter(a -> a.getType().equals(type)).collect(Collectors.toList());
    }

    @Override
    public App getApp(String name) {
        if (registry.containsKey(name))
            return registry.get(name);
        throw new NoSuchElementException("No registered app with name: " + name);
    }

    @Override
    public void start() {
        registry.clear();
    }

    @Override
    public void stop() {
        registry.clear();
    }
}
