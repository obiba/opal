/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ProjectsState {

  private static final Logger log = LoggerFactory.getLogger(ProjectsState.class);

  private final Map<String, State> projectsStateRegistry;

  public ProjectsState() {
    projectsStateRegistry = new HashMap<>();
  }

  public synchronized String getProjectState(@NotNull String name) {
    return projectsStateRegistry.getOrDefault(name, State.READY).name();
  }

  public synchronized void updateProjectState(@NotNull String name, State state) {
    if (!Strings.isNullOrEmpty(name)) {
      State toState = state == null ? State.READY : state;
      log.info("Project {} to state {}", name, toState);
      projectsStateRegistry.put(name, toState);
    }
  }

  public enum State {
    BUSY, // project has read, write and refresh commands that are pending or being processed
    READY,
    LOADING, // project's datasource is not ready

    ERRORS // project's datasource has errors
  }

}
