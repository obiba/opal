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

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.r.service.event.RServerSessionStartedEvent;
import org.obiba.opal.spi.r.ROperation;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The execution context a session activity is recorded in. The first R operation of a session is what records the
 * activity, and that operation can be the context initiator's, which runs while the session is being opened: the
 * context has to be settled by then, not afterwards.
 */
public class RServerSessionContextTest {

  private EventBus eventBus;

  private StartedEvents startedEvents;

  @Before
  public void setUp() {
    eventBus = new EventBus();
    startedEvents = new StartedEvents();
    eventBus.register(startedEvents);
  }

  @Test
  public void test_the_activity_is_recorded_in_the_context_the_session_was_created_for() {
    TestRServerSession rSession = new TestRServerSession("DataSHIELD", eventBus);

    rSession.execute(mock(ROperation.class));

    assertThat(startedEvents.events).hasSize(1);
    assertThat(startedEvents.events.get(0).getExecutionContext()).isEqualTo("DataSHIELD");
  }

  /**
   * The initiator's operations are executed before whoever asked for the session gets it back: a context set at that
   * later point would come too late for the activity record.
   */
  @Test
  public void test_the_operations_of_the_context_initiator_are_recorded_in_the_session_context() {
    TestRServerSession rSession = new TestRServerSession("DataSHIELD", eventBus);
    rSession.initiate(session -> session.execute(mock(ROperation.class)));

    rSession.setExecutionContext("DataSHIELD");

    assertThat(startedEvents.events).hasSize(1);
    assertThat(startedEvents.events.get(0).getExecutionContext()).isEqualTo("DataSHIELD");
  }

  @Test
  public void test_a_session_created_without_a_context_falls_back_to_the_default_one() {
    TestRServerSession rSession = new TestRServerSession(null, eventBus);

    rSession.execute(mock(ROperation.class));

    assertThat(startedEvents.events).hasSize(1);
    assertThat(startedEvents.events.get(0).getExecutionContext()).isEqualTo(RServerSession.DEFAULT_CONTEXT);
  }

  private static class StartedEvents {

    private final List<RServerSessionStartedEvent> events = Lists.newArrayList();

    @Subscribe
    public void onRServerSessionStarted(RServerSessionStartedEvent event) {
      events.add(event);
    }
  }

  /**
   * An R server session that executes nothing: only the busy state matters here, that is what times the activity.
   */
  private static class TestRServerSession extends AbstractRServerSession {

    private TestRServerSession(String executionContext, EventBus eventBus) {
      super("default", "session-1", "jsmith", profile(), executionContext, null, eventBus);
      setRunning();
    }

    /**
     * What the R server does when it opens the session: apply the first R operations, before the session is handed
     * over to its requester.
     */
    private void initiate(RContextInitiator initiator) {
      try {
        initiator.initiate(this);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }

    @Override
    public void execute(ROperation rop) {
      setBusy(true);
      setBusy(false);
    }

    @Override
    public boolean isClosed() {
      return false;
    }

    private static RServerProfile profile() {
      RServerProfile profile = mock(RServerProfile.class);
      when(profile.getName()).thenReturn("default");
      return profile;
    }
  }
}
