/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.eventbus.Subscribe;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.event.DatasourceDeletedEvent;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.tools.SimpleOrientDbQueryBuilder;
import org.obiba.opal.spi.resource.Resource;
import org.obiba.opal.spi.resource.ResourceFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ResourceReferenceServiceImpl implements ResourceReferenceService {

  private static final Logger logger = LoggerFactory.getLogger(ResourceReferenceServiceImpl.class);

  private final OrientDbService orientDbService;

  private final OpalRuntime opalRuntime;

  @Autowired
  public ResourceReferenceServiceImpl(OrientDbService orientDbService, OpalRuntime opalRuntime) {
    this.orientDbService = orientDbService;
    this.opalRuntime = opalRuntime;
  }

  @Override
  public Iterable<ResourceReference> getResourceReferences(String project) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
        .table(ResourceReference.class.getSimpleName())
        .whereClauses("project = ?")
        .build();
    return orientDbService.list(ResourceReference.class, query, project);
  }

  @Override
  public ResourceReference getResourceReference(String project, String name) throws NoSuchResourceReferenceException {
    String query = SimpleOrientDbQueryBuilder.newInstance()
        .table(ResourceReference.class.getSimpleName())
        .whereClauses("project = ?", "name = ?")
        .build();
    ResourceReference resourceReference = orientDbService.uniqueResult(ResourceReference.class, query, project, name);
    if (resourceReference == null)
      throw new NoSuchResourceReferenceException(project, name);
    return resourceReference;
  }

  @Override
  public Resource createResource(ResourceReference resourceReference) {
    ResourceFactoryService resourceFactoryService = (ResourceFactoryService) opalRuntime.getServicePlugin(resourceReference.getProvider());
    return resourceFactoryService.getResourceFactories().stream()
        .filter(fac -> fac.getName().equals(resourceReference.getFactory()))
        .map(fac -> fac.createResource(resourceReference.getName(), resourceReference.getParameters(), resourceReference.getCredentials()))
        .findFirst().orElse(null);
  }

  @Override
  public void save(ResourceReference resourceReference) {
    orientDbService.save(resourceReference, resourceReference);
  }

  @Override
  public void delete(ResourceReference resourceReference) {
    orientDbService.delete(resourceReference);
  }

  @Override
  public void deleteAll(String project) {
    getResourceReferences(project).forEach(orientDbService::delete);
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(ResourceReference.class);
  }

  @Override
  @PreDestroy
  public void stop() {

  }

  @Subscribe
  public void onProjectDeleted(DatasourceDeletedEvent event) {
    deleteAll(event.getDatasource().getName());
  }
}
