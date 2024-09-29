/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.json.JSONArray;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.core.event.DatasourceDeletedEvent;
import org.obiba.opal.core.service.security.CryptoService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.tools.SimpleOrientDbQueryBuilder;
import org.obiba.opal.spi.r.ResourceAssignROperation;
import org.obiba.opal.spi.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ResourceReferenceServiceImpl implements ResourceReferenceService {

  private static final Logger logger = LoggerFactory.getLogger(ResourceReferenceServiceImpl.class);

  private final static Authorizer authorizer = new ShiroAuthorizer();

  private final OrientDbService orientDbService;

  private final CryptoService cryptoService;

  private final SubjectAclService subjectAclService;

  private final ResourceProvidersService resourceProvidersService;

  @Autowired
  public ResourceReferenceServiceImpl(OrientDbService orientDbService, CryptoService cryptoService, SubjectAclService subjectAclService, ResourceProvidersService resourceProvidersService) {
    this.orientDbService = orientDbService;
    this.cryptoService = cryptoService;
    this.subjectAclService = subjectAclService;
    this.resourceProvidersService = resourceProvidersService;
  }

  @Override
  public List<ResourceReference> getResourceReferences(String project) {
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
    ResourceReference resourceReference = getResourceReferenceInternal(project, name);
    if (!canViewResourceReference(project, name))
      throw new NoSuchResourceReferenceException(project, name);
    return resourceReference;
  }

  @Override
  public boolean hasResourceReference(String project, String name) {
    try {
      getResourceReference(project, name);
      return true;
    } catch (NoSuchResourceReferenceException e) {
      return false;
    }
  }

  @Override
  public Resource createResource(ResourceReference resourceReference) {
    try {
      ResourceProvidersService.ResourceFactory factory = resourceProvidersService.getResourceFactory(resourceReference.getProvider(), resourceReference.getFactory());
      return factory.createResource(resourceReference.getName(), resourceReference.getParameters(), resourceReference.getCredentials());
    } catch (Exception e) {
      logger.warn("Cannot make resource object of resource reference {}", resourceReference.getProject() + "." + resourceReference.getName(), e);
    }
    return null;
  }

  @Override
  public List<String> getRequiredPackages(ResourceReference resourceReference) {
    List<String> names = Lists.newArrayList(resourceReference.getProvider());
    String pkg = resourceReference.getParameters().optString("_package");
    if (!Strings.isNullOrEmpty(pkg)) names.add(pkg);
    JSONArray pkgs = resourceReference.getParameters().optJSONArray("_packages");
    if (pkgs != null) {
      for (int i = 0; i < pkgs.length(); i++) {
        String p = pkgs.optString(i);
        if (!Strings.isNullOrEmpty(p)) names.add(p);
      }
    }
    return names;
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
    } catch (NoSuchResourceReferenceException e) {
      // ignore
    }
  }

  @Override
  public void deleteAll(String project) {
    getResourceReferences(project).forEach(orientDbService::delete);
    subjectAclService.deleteNodePermissions("/project/" + project + "/resource");
  }

  @Override
  public ResourceAssignROperation asAssignOperation(String project, String name, String symbol) throws NoSuchResourceReferenceException {
    ResourceReference ref = getResourceReferenceInternal(project, name);
    Resource resource = createResource(ref);
    List<String> requiredPackages = getRequiredPackages(ref);
    if (resource == null) {
      throw new ResourceAssignException(project, name, requiredPackages);
    }
    return new ResourceAssignROperation(symbol, project, resource, requiredPackages);
  }

  @Override
  public String getProfile(String project, String name) {
    ResourceReference ref = getResourceReferenceInternal(project, name);
    try {
      ResourceProvidersService.ResourceProvider provider = resourceProvidersService.getResourceProvider(ref.getProvider());
      return provider.getProfile();
    } catch (Exception e) {
      logger.warn("Failed at getting resource profile: {}", e.getMessage());
      return "default";
    }
  }

  @Override
  public void start() {
    orientDbService.createUniqueIndex(ResourceReference.class);
  }

  @Override
  public void stop() {
  }

  @Subscribe
  public void onProjectDeleted(DatasourceDeletedEvent event) {
    deleteAll(event.getDatasource().getName());
  }

  //
  // Private methods
  //

  private ResourceReference getResourceReferenceInternal(String project, String name) throws NoSuchResourceReferenceException {
    String query = SimpleOrientDbQueryBuilder.newInstance()
        .table(ResourceReference.class.getSimpleName())
        .whereClauses("project = ?", "name = ?")
        .build();
    ResourceReference resourceReference = orientDbService.uniqueResult(ResourceReference.class, query, project, name);
    if (resourceReference == null)
      throw new NoSuchResourceReferenceException(project, name);
    return decryptCredentials(resourceReference);
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
