/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.Status;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * The point of the appender is that the DataSHIELD MDC keys can be exported under their OpenTelemetry
 * names without the file appender - whose output datashield.log consumers parse - ever seeing the
 * renamed ones. The last two tests are the ones that guard that promise.
 */
public class MdcRenamingAppenderTest {

  private LoggerContext context;

  private MdcRenamingAppender appender;

  private ListAppender<ILoggingEvent> downstream;

  @Before
  public void setUp() {
    context = new LoggerContext();
    downstream = new ListAppender<>();
    downstream.setContext(context);
    downstream.start();
    appender = new MdcRenamingAppender();
    appender.setContext(context);
    appender.setName("otelds");
    appender.addAppender(downstream);
  }

  @Test
  public void test_keys_are_renamed() {
    appender.addRename("ds_action=datashield.action");
    appender.addRename("username=enduser.id");
    appender.start();

    Map<String, String> forwarded = forward(mdc("ds_action", "AGGREGATE", "username", "administrator"));

    assertThat(forwarded).hasSize(2);
    assertThat(forwarded.get("datashield.action")).isEqualTo("AGGREGATE");
    assertThat(forwarded.get("enduser.id")).isEqualTo("administrator");
  }

  @Test
  public void test_unmapped_keys_are_forwarded_unchanged() {
    appender.addRename("ds_action=datashield.action");
    appender.start();

    Map<String, String> forwarded = forward(mdc("ds_action", "LS", "correlation", "abc"));

    assertThat(forwarded.get("correlation")).isEqualTo("abc");
  }

  @Test
  public void test_dropped_keys_never_reach_the_downstream_appender() {
    appender.addRename("ds_eval=datashield.script");
    appender.addDrop("ds_eval");
    appender.start();

    Map<String, String> forwarded = forward(mdc("ds_eval", "meanDS(D$age)", "ds_id", "rsession-42"));

    assertThat(forwarded).doesNotContainKey("datashield.script").doesNotContainKey("ds_eval");
    assertThat(forwarded.get("ds_id")).isEqualTo("rsession-42");
  }

  /**
   * The cap bounds what is exported, so it is a hard one: the ellipsis counts towards it.
   */
  @Test
  public void test_values_are_truncated_before_being_renamed() {
    appender.addRename("ds_eval=datashield.script");
    appender.addTruncate("ds_eval=6");
    appender.start();

    Map<String, String> forwarded = forward(mdc("ds_eval", "meanDS(D$age)"));

    assertThat(forwarded.get("datashield.script")).isEqualTo("mea...");
  }

  @Test
  public void test_a_limit_too_short_for_an_ellipsis_still_holds() {
    appender.addTruncate("ds_eval=2");
    appender.start();

    assertThat(forward(mdc("ds_eval", "meanDS(D$age)")).get("ds_eval")).isEqualTo("me");
  }

  @Test
  public void test_values_shorter_than_the_limit_are_left_alone() {
    appender.addTruncate("ds_eval=512");
    appender.start();

    assertThat(forward(mdc("ds_eval", "ls()")).get("ds_eval")).isEqualTo("ls()");
  }

  /**
   * datashield.log is written by an appender attached upstream of this one, so it must still see the
   * original keys: the rewrite may not touch the event that was handed in.
   */
  @Test
  public void test_the_original_event_is_not_modified() {
    appender.addRename("ds_action=datashield.action");
    appender.addDrop("ds_eval");
    appender.start();

    LoggingEvent event = event(mdc("ds_action", "ASSIGN", "ds_eval", "meanDS(D$age)"));
    appender.doAppend(event);

    assertThat(event.getMDCPropertyMap()).containsKey("ds_action").containsKey("ds_eval");
    assertThat(event.getMDCPropertyMap()).doesNotContainKey("datashield.action");
  }

  /**
   * Everything except the MDC map has to survive the wrapping, or the file and the OTLP stream would
   * disagree about what was logged.
   */
  @Test
  public void test_everything_else_is_delegated_to_the_original_event() {
    appender.addRename("ds_action=datashield.action");
    appender.start();

    LoggingEvent event = event(mdc("ds_action", "OPEN"));
    appender.doAppend(event);
    ILoggingEvent forwarded = downstream.list.get(0);

    assertThat(forwarded.getLoggerName()).isEqualTo(event.getLoggerName());
    assertThat(forwarded.getLevel()).isEqualTo(event.getLevel());
    assertThat(forwarded.getFormattedMessage()).isEqualTo(event.getFormattedMessage());
    assertThat(forwarded.getTimeStamp()).isEqualTo(event.getTimeStamp());
  }

  @Test
  public void test_a_malformed_rename_is_reported_rather_than_ignored() {
    appender.addRename("no-equals-sign");
    appender.addTruncate("ds_eval=not-a-number");
    appender.start();

    assertThat(errorCount()).isEqualTo(2);
  }

  @Test
  public void test_an_appender_with_nothing_nested_in_it_refuses_to_start() {
    MdcRenamingAppender orphan = new MdcRenamingAppender();
    orphan.setContext(context);
    orphan.setName("otelds");
    orphan.start();

    assertThat(orphan.isStarted()).isFalse();
    assertThat(errorCount()).isEqualTo(1);
  }

  private Map<String, String> forward(Map<String, String> mdc) {
    appender.doAppend(event(mdc));
    return downstream.list.get(downstream.list.size() - 1).getMDCPropertyMap();
  }

  private LoggingEvent event(Map<String, String> mdc) {
    LoggingEvent event = new LoggingEvent();
    event.setLoggerName("datashield.user");
    event.setLevel(Level.INFO);
    event.setMessage("created a datashield session {}");
    event.setArgumentArray(new Object[]{"rsession-42"});
    event.setTimeStamp(1_767_225_600_000L);
    event.setMDCPropertyMap(mdc);
    return event;
  }

  private Map<String, String> mdc(String... keysAndValues) {
    Map<String, String> mdc = new LinkedHashMap<>();
    for(int i = 0; i < keysAndValues.length; i += 2) {
      mdc.put(keysAndValues[i], keysAndValues[i + 1]);
    }
    return mdc;
  }

  private long errorCount() {
    return context.getStatusManager().getCopyOfStatusList().stream()
        .filter(status -> status.getLevel() == Status.ERROR).count();
  }
}
