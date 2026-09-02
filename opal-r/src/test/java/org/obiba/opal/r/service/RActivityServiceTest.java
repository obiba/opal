/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obiba.opal.r.repository.RSessionActivityRepository;
import org.obiba.opal.r.service.event.RServerSessionClosedEvent;
import org.obiba.opal.r.service.event.RServerSessionStartedEvent;
import org.obiba.opal.r.service.event.RServerSessionUpdatedEvent;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What the activity log records, which is what the quotas are measured against: the execution time of the commands a
 * session ran, and the wall-clock span of the session itself.
 */
public class RActivityServiceTest {

  private static final String SESSION_ID = "session-1";

  private static final String USER = "jsmith";

  private static final String CONTEXT = "DataSHIELD";

  private RSessionActivityRepository repository;

  private RActivityService service;

  @Before
  public void setUp() {
    repository = mock(RSessionActivityRepository.class);
    when(repository.findById(anyString())).thenReturn(Optional.empty());
    service = new RActivityService(repository);
  }

  /**
   * The session manager posts the started event when the session is created, so a session that never runs a command
   * still leaves a record - which is the whole point for a session time quota, since such a session is exactly the one
   * that holds an R server for nothing.
   */
  @Test
  public void test_a_session_that_never_runs_a_command_is_recorded() {
    service.onRServerSessionStarted(started(new Date()));

    assertThat(captureSaved().getId()).isEqualTo(SESSION_ID);
  }

  /**
   * The event is posted twice for the same session - once at creation, once when it first becomes busy - and the
   * second post has to leave the record alone: inserting again would reset the execution time already recorded.
   */
  @Test
  public void test_the_second_started_event_does_not_overwrite_the_record() {
    RSessionActivity existing = new RSessionActivity();
    existing.setId(SESSION_ID);
    existing.setExecutionTimeMillis(42);
    when(repository.findById(SESSION_ID)).thenReturn(Optional.of(existing));

    service.onRServerSessionStarted(started(new Date()));

    verify(repository, never()).upsert(any(RSessionActivity.class));
  }

  @Test
  public void test_the_internal_opal_user_is_not_recorded() {
    service.onRServerSessionStarted(new RServerSessionStartedEvent(SESSION_ID, "opal/system", CONTEXT, "default",
        new Date()));

    verify(repository, never()).upsert(any(RSessionActivity.class));
  }

  @Test
  public void test_the_session_time_is_the_span_of_the_record() {
    Date created = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30));
    service.onRServerSessionStarted(started(created));

    RSessionActivity saved = captureSaved();

    assertThat(saved.getSessionTimeMillis())
        .isEqualTo(saved.getUpdated().getTime() - saved.getCreated().getTime());
    assertThat(saved.getSessionTimeMillis()).isGreaterThanOrEqualTo(TimeUnit.MINUTES.toMillis(30));
  }

  /**
   * A command moves the end of the span, so both numbers move with it.
   */
  @Test
  public void test_a_command_advances_both_the_execution_time_and_the_session_time() {
    RSessionActivity existing = existing(TimeUnit.MINUTES.toMillis(30));
    when(repository.findById(SESSION_ID)).thenReturn(Optional.of(existing));

    service.onRServerSessionUpdated(new RServerSessionUpdatedEvent(SESSION_ID, USER, TimeUnit.MINUTES.toMillis(5)));

    RSessionActivity saved = captureSaved();
    assertThat(saved.getExecutionTimeMillis()).isEqualTo(TimeUnit.MINUTES.toMillis(5));
    assertThat(saved.getSessionTimeMillis()).isGreaterThanOrEqualTo(TimeUnit.MINUTES.toMillis(30));
    assertThat(saved.getIdleTimeMillis()).isGreaterThanOrEqualTo(TimeUnit.MINUTES.toMillis(25));
  }

  /**
   * Closing is the moment the span is finally known: the idle time between the last command and the close belongs to
   * the session, and to a session time quota.
   */
  @Test
  public void test_closing_the_session_records_the_idle_time_it_ended_with() {
    RSessionActivity existing = existing(TimeUnit.HOURS.toMillis(4));
    existing.setExecutionTimeMillis(TimeUnit.MINUTES.toMillis(2));
    when(repository.findById(SESSION_ID)).thenReturn(Optional.of(existing));

    service.onRServerSessionClosed(new RServerSessionClosedEvent(SESSION_ID, USER));

    RSessionActivity saved = captureSaved();
    assertThat(saved.getSessionTimeMillis()).isGreaterThanOrEqualTo(TimeUnit.HOURS.toMillis(4));
    assertThat(saved.getIdleTimeMillis())
        .isGreaterThanOrEqualTo(TimeUnit.HOURS.toMillis(4) - TimeUnit.MINUTES.toMillis(2));
  }

  private RServerSessionStartedEvent started(Date created) {
    return new RServerSessionStartedEvent(SESSION_ID, USER, CONTEXT, "default", created);
  }

  private RSessionActivity existing(long ageMillis) {
    RSessionActivity activity = new RSessionActivity();
    activity.setId(SESSION_ID);
    activity.setUser(USER);
    activity.setContext(CONTEXT);
    activity.setCreated(new Date(System.currentTimeMillis() - ageMillis));
    activity.setUpdated(new Date(System.currentTimeMillis() - ageMillis));
    return activity;
  }

  private RSessionActivity captureSaved() {
    ArgumentCaptor<RSessionActivity> captor = ArgumentCaptor.forClass(RSessionActivity.class);
    verify(repository).upsert(captor.capture());
    return captor.getValue();
  }
}
