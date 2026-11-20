/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.junit.Test;
import org.obiba.opal.core.service.ProjectsState.State;

import static org.fest.assertions.api.Assertions.assertThat;

public class ProjectsStateTest {

  @Test
  public void test_only_one_of_concurrent_loads_starts() {
    ProjectsState state = new ProjectsState();
    state.updateProjectState("p", State.ERRORS);

    assertThat(state.updateProjectState("p", State.ERRORS, State.LOADING)).isTrue();
    assertThat(state.updateProjectState("p", State.ERRORS, State.LOADING)).isFalse();
    assertThat(state.getProjectState("p")).isEqualTo(State.LOADING.name());
  }

  @Test
  public void test_unknown_project_is_ready() {
    ProjectsState state = new ProjectsState();

    assertThat(state.updateProjectState("p", State.ERRORS, State.LOADING)).isFalse();
    assertThat(state.updateProjectState("p", State.READY, State.ERRORS)).isTrue();
    assertThat(state.getProjectState("p")).isEqualTo(State.ERRORS.name());
  }

}
