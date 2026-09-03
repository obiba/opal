/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The number of open DataSHIELD sessions, counted on collection rather than maintained.
 * <p/>
 * A session does not only end when a user closes it: it is also evicted when it times out, and when
 * the R server it belongs to goes away. An up-down counter incremented and decremented around the
 * REST endpoints would miss those and drift upwards forever, so the manager is asked instead - it
 * already knows, and the answer cannot be stale.
 */
@Component
public class DataShieldSessionMetrics {

  private static final AttributeKey<String> PROFILE = AttributeKey.stringKey("datashield.profile");

  private final OpalRSessionManager opalRSessionManager;

  private AutoCloseable registration;

  @Autowired
  public DataShieldSessionMetrics(OpalRSessionManager opalRSessionManager) {
    this.opalRSessionManager = opalRSessionManager;
  }

  @PostConstruct
  public void register() {
    registration = DataShieldMetrics.meter()
        .gaugeBuilder("datashield.session.active")
        .ofLongs()
        .setDescription("Open DataSHIELD sessions, by profile")
        .setUnit("{session}")
        .buildWithCallback(this::observe);
  }

  @PreDestroy
  public void unregister() throws Exception {
    if(registration != null) registration.close();
  }

  private void observe(ObservableLongMeasurement measurement) {
    countByProfile().forEach((profile, count) -> measurement.record(count, Attributes.of(PROFILE, profile)));
  }

  private Map<String, Long> countByProfile() {
    return opalRSessionManager.getRSessions().stream()
        .filter(session -> DataShieldContexts.DATASHIELD.equals(session.getExecutionContext()))
        .collect(Collectors.groupingBy(profileOf(), Collectors.counting()));
  }

  private Function<RServerSession, String> profileOf() {
    return session -> session.getProfile() == null ? "" : session.getProfile().getName();
  }
}
