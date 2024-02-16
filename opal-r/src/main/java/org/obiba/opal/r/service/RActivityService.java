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

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.apache.commons.compress.utils.Lists;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.r.service.event.RServerSessionClosedEvent;
import org.obiba.opal.r.service.event.RServerSessionEvent;
import org.obiba.opal.r.service.event.RServerSessionStartedEvent;
import org.obiba.opal.r.service.event.RServerSessionUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class RActivityService implements SystemService {

  private final OrientDbService orientDbService;

  @Autowired
  public RActivityService(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  public List<RSessionActivity> getActivities(String context, String user, String profile, Date fromDate, Date toDate) {
    checkAlphanumeric(context);
    Iterable<RSessionActivity> records;
    if (Strings.isNullOrEmpty(user) && Strings.isNullOrEmpty(profile)) {
      String sql = String.format("select * from %s where context = ?", RSessionActivity.class.getSimpleName());
      records = orientDbService.list(RSessionActivity.class,
          sql, context);
    } else if (Strings.isNullOrEmpty(user)) {
      checkAlphanumeric(profile);
      String sql = String.format("select * from %s where context = ? and profile = ?", RSessionActivity.class.getSimpleName());
      records = orientDbService.list(RSessionActivity.class,
          sql, context, profile);
    } else if (Strings.isNullOrEmpty(profile)) {
      checkAlphanumeric(user);
      String sql = String.format("select * from %s where context = ? and user = ?", RSessionActivity.class.getSimpleName());
      records = orientDbService.list(RSessionActivity.class,
          sql, context, user);
    } else {
      checkAlphanumeric(profile);
      checkAlphanumeric(user);
      String sql = String.format("select * from %s where context = ? and user = ? and profile = ?", RSessionActivity.class.getSimpleName());
      records = orientDbService.list(RSessionActivity.class,
          sql, context, user, profile);
    }
    // TODO filter dates in the SQL query
    return StreamSupport.stream(records.spliterator(), false)
        .filter(rec -> fromDate == null || rec.getCreated().equals(fromDate) || rec.getCreated().after(fromDate))
        .filter(rec -> toDate == null || rec.getCreated().equals(toDate) || rec.getCreated().before(toDate))
        .collect(Collectors.toList());
  }

  /**
   * Get R context activity per profile (or any profile), for a specific user (or any user).
   *
   * @param context
   * @param user
   * @param profile
   * @param fromDate
   * @param toDate
   * @return
   */
  public List<RActivitySummary> getActivitySummaries(String context, String user, String profile, Date fromDate, Date toDate) {
    if (Strings.isNullOrEmpty(user)) {
      return getActivitySummaries(context, profile, fromDate, toDate);
    }
    List<RSessionActivity> records = getActivities(context, user, profile, fromDate, toDate);
    List<RActivitySummary> summaries = Lists.newArrayList();
    Map<String, List<RSessionActivity>> profileRecords = records.stream()
        .collect(Collectors.groupingBy(RSessionActivity::getProfile));
    for (String profileKey : profileRecords.keySet()) {
      RActivitySummary summary = getActivitySummary(profileRecords.get(profileKey));
      summary.setUser(user);
      summary.setContext(context);
      summary.setProfile(profileKey);
      summaries.add(summary);
    }
    return summaries;
  }

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

  /**
   * Get R context activity per profile, for any user.
   *
   * @param context
   * @param profile
   * @param fromDate
   * @param toDate
   * @return
   */
  private List<RActivitySummary> getActivitySummaries(String context, String profile, Date fromDate, Date toDate) {
    List<RActivitySummary> summaries = Lists.newArrayList();
    Map<String, List<RSessionActivity>> profileRecords = getActivities(context, null, profile, fromDate, toDate).stream()
        .collect(Collectors.groupingBy(RSessionActivity::getProfile));
    for (String profileKey : profileRecords.keySet()) {
      Map<String, List<RSessionActivity>> userProfileRecords = profileRecords.get(profileKey).stream()
          .collect(Collectors.groupingBy(RSessionActivity::getUser));
      for (String user : userProfileRecords.keySet()) {
        RActivitySummary summary = getActivitySummary(userProfileRecords.get(user));
        summary.setUser(user);
        summary.setContext(context);
        summary.setProfile(profileKey);
        summaries.add(summary);
      }
    }
    return summaries;
  }

  /**
   * User activity summary.
   *
   * @param records
   * @return
   */
  private RActivitySummary getActivitySummary(List<RSessionActivity> records) {
    RActivitySummary summary = new RActivitySummary();
    summary.setExecutionTimeMillis(records.stream()
        .map(RActivity::getExecutionTimeMillis)
        .reduce(0L, Long::sum));
    summary.setCreated(records.stream()
        .map(AbstractTimestamped::getCreated)
        .reduce(null, (a, d) -> a == null ? d : (a.after(d) ? d : a)));
    summary.setUpdated(records.stream()
        .map(AbstractTimestamped::getUpdated)
        .reduce(null, (a, d) -> a == null ? d : (a.before(d) ? d : a)));
    summary.setSessionsCount(records.size());
    return summary;
  }

  private boolean isOpalSystemUser(RServerSessionEvent event) {
    return "opal/system".equals(event.getUser());
  }

  /**
   * Minimal sanity check.
   * 
   * @param name
   */
  private void checkAlphanumeric(String name) {
    if (!name.matches("[a-zA-Z0-9_@\\-\\\\.]+"))
      throw new IllegalArgumentException("Invalid string: " + name);
  }

}
