/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.r.service.event.RServerSessionClosedEvent;
import org.obiba.opal.r.service.event.RServerSessionEvent;
import org.obiba.opal.r.service.event.RServerSessionStartedEvent;
import org.obiba.opal.r.service.event.RServerSessionUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
public class RSessionActivityService implements SystemService {

  @Autowired
  private OrientDbService orientDbService;

  @Subscribe
  public void onRServerSessionStarted(RServerSessionStartedEvent event) {
    if (isOpalSystemUser(event)) return;
    RSessionActivity metric = new RSessionActivity();
    metric.setId(event.getId());
    metric.setUser(event.getUser());
    metric.setContext(event.getExecutionContext());
    metric.setProfile(event.getProfile());
    metric.setCreated(event.getCreated());
    metric.setUpdated(new Date());
    orientDbService.save(metric, metric);
  }

  @Subscribe
  public void onRServerSessionUpdated(RServerSessionUpdatedEvent event) {
    if (isOpalSystemUser(event)) return;
    RSessionActivity template = new RSessionActivity();
    template.setId(event.getId());
    RSessionActivity metric = orientDbService.findUnique(template);
    if (metric == null) return; // broken for some reason
    metric.setUpdated(new Date());
    metric.setExecutionTimeMillis(event.getExecutionTimeMillis());
    orientDbService.save(template, metric);
  }

  @Subscribe
  public void onRServerSessionClosed(RServerSessionClosedEvent event) {
    if (isOpalSystemUser(event)) return;
    RSessionActivity template = new RSessionActivity();
    template.setId(event.getId());
    RSessionActivity metric = orientDbService.findUnique(template);
    if (metric == null) return; // broken for some reason
    metric.setUpdated(new Date());
    orientDbService.save(template, metric);
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(RSessionActivity.class);
    orientDbService.createIndex(RSessionActivity.class, OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING, "user");
    orientDbService.createIndex(RSessionActivity.class, OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING, "context");
    orientDbService.createIndex(RSessionActivity.class, OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING, "profile");
  }

  @Override
  public void stop() {

  }

  //
  // Private methods
  //

  private boolean isOpalSystemUser(RServerSessionEvent event) {
    return "opal/system".equals(event.getUser());
  }
}
