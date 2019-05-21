package org.obiba.opal.core.domain;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ProjectsState {

  private final Map<String, State> projectsStateRegistry;

  public ProjectsState() {
    projectsStateRegistry = new HashMap<>();
  }

  public synchronized String getProjectState(@NotNull String name) {
    return projectsStateRegistry.getOrDefault(name, State.READY).name();
  }

  public synchronized void updateProjectState(@NotNull String name, State state) {
    if (!Strings.isNullOrEmpty(name)) {
      projectsStateRegistry.put(name, state != null ? state : State.READY);
    }
  }

  public enum State {
    BUSY, // project has read, write and refresh commands that are pending or being processed
    READY,
    REFRESHING // project's datasource is not ready
  }

}
