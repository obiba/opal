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

import com.google.common.base.Strings;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

/**
 * The DataSHIELD instruments.
 * <p/>
 * Every attribute here is bounded on purpose. A metric attribute multiplies the number of time series
 * the backend keeps, so the things that identify a single operation - the session id, the user, the
 * submitted expression, the symbol a user chose to name - belong on the logs and the spans, and
 * never here.
 */
public final class DataShieldMetrics {

  static final String SCOPE = "org.obiba.opal.datashield";

  private static final AttributeKey<String> ACTION = AttributeKey.stringKey("datashield.action");

  private static final AttributeKey<String> PROFILE = AttributeKey.stringKey("datashield.profile");

  private static final AttributeKey<String> OUTCOME = AttributeKey.stringKey("datashield.outcome");

  private static final AttributeKey<String> QUOTA_METRIC = AttributeKey.stringKey("datashield.quota.metric");

  /**
   * Rebuilt when the global OpenTelemetry changes, so that instruments resolved before the SDK was
   * installed - or between tests - are not cached forever as no-ops.
   */
  private static volatile Instruments instruments;

  private DataShieldMetrics() {
  }

  public static void recordOperation(DataShieldLog.Action action, String profile, boolean failed, long durationNanos) {
    Attributes attributes = attributes(action, profile, failed);
    Instruments current = instruments();
    current.operations.add(1, attributes);
    current.duration.record(durationNanos / 1_000_000_000d, attributes);
  }

  /**
   * A session refused because the user has spent their allowance. Counted per metric rather than per
   * profile: a quota is held against a user and a metric, not against a profile.
   */
  public static void recordQuotaRejection(String quotaMetric) {
    instruments().quotaRejections.add(1, Attributes.of(QUOTA_METRIC, Strings.nullToEmpty(quotaMetric)));
  }

  static Meter meter() {
    return GlobalOpenTelemetry.getMeter(SCOPE);
  }

  private static Attributes attributes(DataShieldLog.Action action, String profile, boolean failed) {
    AttributesBuilder builder = Attributes.builder()
        .put(ACTION, action == null ? "?" : action.name())
        .put(OUTCOME, failed ? "error" : "ok");
    if(!Strings.isNullOrEmpty(profile)) builder.put(PROFILE, profile);
    return builder.build();
  }

  private static Instruments instruments() {
    OpenTelemetry source = GlobalOpenTelemetry.get();
    Instruments current = instruments;
    if(current == null || current.source != source) {
      current = new Instruments(source);
      instruments = current;
    }
    return current;
  }

  private static final class Instruments {

    private final OpenTelemetry source;

    private final LongCounter operations;

    private final DoubleHistogram duration;

    private final LongCounter quotaRejections;

    private Instruments(OpenTelemetry source) {
      this.source = source;
      Meter meter = source.getMeter(SCOPE);
      this.operations = meter.counterBuilder("datashield.operation.count")
          .setDescription("DataSHIELD operations, by action and outcome")
          .setUnit("{operation}")
          .build();
      this.duration = meter.histogramBuilder("datashield.operation.duration")
          .setDescription("Time the R server spent on a DataSHIELD operation")
          .setUnit("s")
          .build();
      this.quotaRejections = meter.counterBuilder("datashield.quota.rejection")
          .setDescription("DataSHIELD sessions refused because a usage quota was spent")
          .setUnit("{rejection}")
          .build();
    }
  }
}
