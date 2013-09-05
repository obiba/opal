/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.project.cfg;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.project.NoSuchProjectException;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.project.domain.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
public class ProjectsConfigurationService implements ProjectService {

  public static final String PROJECTS_DIR = "projects";

  private final OpalRuntime opalRuntime;

  private final ExtensionConfigurationSupplier<ProjectsConfiguration> configSupplier;

  @Autowired
  public ProjectsConfigurationService(OpalConfigurationService configService, OpalRuntime opalRuntime) {
    configSupplier = new ExtensionConfigurationSupplier<ProjectsConfiguration>(configService,
        ProjectsConfiguration.class);
    this.opalRuntime = opalRuntime;
  }

  private ProjectsConfiguration getConfig() {
    if(!configSupplier.hasExtension()) {
      configSupplier.addExtension(new ProjectsConfiguration());
    }
    return configSupplier.get();
  }

  @Override
  public List<Project> getProjects() {
    return getConfig().getProjects();
  }

  @Override
  public boolean hasProject(String name) {
    return getConfig().hasProject(name);
  }

  @Override
  public void removeProject(final String name) {
    configSupplier.modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<ProjectsConfiguration>() {
      @Override
      public void doWithConfig(ProjectsConfiguration config) {
        config.removeProject(name);
      }
    });
  }

  @Override
  public void addOrReplaceProject(final Project project) {
    configSupplier.modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<ProjectsConfiguration>() {
      @Override
      public void doWithConfig(ProjectsConfiguration config) {
        config.putProject(project);
      }
    });
  }

  @Override
  public Project getOrCreateProject(Datasource ds) {
    Project project;
    if(hasProject(ds.getName())) {
      project = getProject(ds.getName());
    } else {
      Project.Builder builder = Project.Builder.create(ds.getName()) //
          .title(ds.getName()) //
          .tags(getAttributeNamespaces(ds));

      addOrReplaceProject(project = builder.build());
    }
    return project;
  }

  @Override
  public Project getProject(String name) {
    return getConfig().getProject(name);
  }

  @Override
  public FileObject getProjectDirectory(String name) throws NoSuchFunctionalUnitException, FileSystemException {
    if(!hasProject(name)) {
      throw new NoSuchProjectException(name);
    }

    FileObject projectsDir = opalRuntime.getFileSystem().getRoot().resolveFile(PROJECTS_DIR);
    projectsDir.createFolder();

    FileObject projectDir = projectsDir.resolveFile(name);
    projectDir.createFolder();

    return projectDir;
  }

  @Override
  public String getProjectDirectoryPath(String name) {
    try {
      FileObject fo = getProjectDirectory(name);
      return fo.getURL().getPath().substring(2);
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  private Iterable<String> getAttributeNamespaces(Datasource ds) {
    List<String> namespaces = Lists.newArrayList();
    for(ValueTable table : ds.getValueTables()) {
      for(Variable variable : table.getVariables()) {
        for(Attribute attr : variable.getAttributes()) {
          String ns = attr.getNamespace();
          if(!Strings.isNullOrEmpty(ns) && !"opal".equals(ns) && !namespaces.contains(ns)) {
            namespaces.add(ns);
          }
        }
      }
    }
    return namespaces;
  }
}
