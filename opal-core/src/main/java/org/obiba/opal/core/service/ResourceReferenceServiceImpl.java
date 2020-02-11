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
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.event.DatasourceDeletedEvent;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.security.CryptoService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.tools.SimpleOrientDbQueryBuilder;
import org.obiba.opal.spi.resource.Resource;
import org.obiba.opal.spi.resource.ResourceFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.text.html.Option;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ResourceReferenceServiceImpl implements ResourceReferenceService {

  private static final Logger logger = LoggerFactory.getLogger(ResourceReferenceServiceImpl.class);

  private final static Authorizer authorizer = new ShiroAuthorizer();

  private final OrientDbService orientDbService;

  private final OpalRuntime opalRuntime;

  private final CryptoService cryptoService;

  private final SubjectAclService subjectAclService;

  @Autowired
  public ResourceReferenceServiceImpl(OrientDbService orientDbService, OpalRuntime opalRuntime, CryptoService cryptoService, SubjectAclService subjectAclService) {
    this.orientDbService = orientDbService;
    this.opalRuntime = opalRuntime;
    this.cryptoService = cryptoService;
    this.subjectAclService = subjectAclService;
  }

  @Override
  public Iterable<ResourceReference> getResourceReferences(String project) {
    String query = SimpleOrientDbQueryBuilder.newInstance()
        .table(ResourceReference.class.getSimpleName())
        .whereClauses("project = ?")
        .build();
    return StreamSupport.stream(orientDbService.list(ResourceReference.class, query, project).spliterator(), false)
        .filter(ref -> canViewResourceReference(project, ref.getName()))
        .map(this::decryptCredentials)
        .collect(Collectors.toList());
  }

  @Override
  public ResourceReference getResourceReference(String project, String name) throws NoSuchResourceReferenceException {
    String query = SimpleOrientDbQueryBuilder.newInstance()
        .table(ResourceReference.class.getSimpleName())
        .whereClauses("project = ?", "name = ?")
        .build();
    ResourceReference resourceReference = orientDbService.uniqueResult(ResourceReference.class, query, project, name);
    if (resourceReference == null || !canViewResourceReference(project, name))
      throw new NoSuchResourceReferenceException(project, name);
    return decryptCredentials(resourceReference);
  }

  @Override
  public Resource createResource(ResourceReference resourceReference) {
    ResourceFactoryService resourceFactoryService = (ResourceFactoryService) opalRuntime.getServicePlugin(resourceReference.getProvider());
    return resourceFactoryService.getResourceFactories().stream()
        .filter(fac -> fac.getName().equals(resourceReference.getFactory()))
        .map(fac -> fac.createResource(resourceReference.getName(), resourceReference.getParameters(), resourceReference.getCredentials()))
        .filter(Objects::nonNull)
        .findFirst().orElse(null);
  }

  @Override
  public String getRequiredPackageName(ResourceReference resourceReference) {
    ResourceFactoryService resourceFactoryService = (ResourceFactoryService) opalRuntime.getServicePlugin(resourceReference.getProvider());
    return resourceFactoryService.getResourceFactories().stream()
        .filter(fac -> fac.getName().equals(resourceReference.getFactory()))
        .map(fac -> fac.getRequiredPackage(resourceReference.getName(), resourceReference.getParameters(), resourceReference.getCredentials()))
        .filter(Objects::nonNull)
        .findFirst().orElse(null);
  }

  @Override
  public void save(ResourceReference resourceReference) {
    if (resourceReference == null) return;
    resourceReference.setUpdated(new Date());
    ResourceReference resourceReference1 = encryptCredentials(resourceReference);
    orientDbService.save(resourceReference1, resourceReference1);
  }

  @Override
  public void delete(ResourceReference resourceReference) {
    if (resourceReference == null) return;
    orientDbService.delete(resourceReference);
    subjectAclService.deleteNodePermissions(getPermissionNode(resourceReference.getProject(), resourceReference.getName()));
  }

  @Override
  public void delete(String project, String name) {
    try {
      orientDbService.delete(getResourceReference(project, name));
      subjectAclService.deleteNodePermissions(getPermissionNode(project, name));
    } catch(NoSuchResourceReferenceException e) {
      // ignore
    }
  }

  @Override
  public void deleteAll(String project) {
    getResourceReferences(project).forEach(orientDbService::delete);
    subjectAclService.deleteNodePermissions("/project/" + project + "/resource");
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

  private ResourceReference encryptCredentials(ResourceReference resourceReference) {
    if (resourceReference.getCredentialsModel() != null) {
      resourceReference.setEncryptedCredentialsModel(cryptoService.encrypt(resourceReference.getCredentialsModel()));
      resourceReference.setCredentialsModel(null);
    }
    return resourceReference;
  }

  private ResourceReference decryptCredentials(ResourceReference resourceReference) {
    if (resourceReference.getEncryptedCredentialsModel() != null) {
      resourceReference.setCredentialsModel(cryptoService.decrypt(resourceReference.getEncryptedCredentialsModel()));
      resourceReference.setEncryptedCredentialsModel(null);
    }
    return resourceReference;
  }

  private boolean canViewResourceReference(String project, String name) {
    return authorizer.isPermitted("rest:/project/" + project + "/resource/" + name + ":GET");
  }

  private String getPermissionNode(String project, String name) {
    return "/project/" + project + "/resource/" + name;
  }

}
