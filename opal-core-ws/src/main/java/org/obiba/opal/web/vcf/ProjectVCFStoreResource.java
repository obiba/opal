/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.vcf;


import com.google.common.collect.Lists;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.NoSuchProjectException;
import org.obiba.opal.core.service.NoSuchVCFSamplesMappingException;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.VCFSamplesMappingService;
import org.obiba.opal.core.support.vcf.VCFSamplesSummaryBuilder;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.plugins.Dtos;
import org.obiba.plugins.spi.ServicePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * A REST resource to access to a {@link org.obiba.opal.spi.vcf.VCFStore}.
 */
@Component
@Scope("request")
@Path("/project/{name}/vcf-store")
public class ProjectVCFStoreResource implements BaseResource {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  private final OpalRuntime opalRuntime;

  private final ProjectService projectService;

  private final VCFSamplesMappingService vcfSamplesMappingService;

  private VCFStore store;

  private VCFSamplesSummaryBuilder summaryBuilder;

  private VCFStoreService service;

  @Autowired
  public ProjectVCFStoreResource(OpalRuntime opalRuntime, ProjectService projectService, VCFSamplesMappingService vcfSamplesMappingService) {
    this.opalRuntime = opalRuntime;
    this.projectService = projectService;
    this.vcfSamplesMappingService = vcfSamplesMappingService;
  }


  /**
   * Get the {@link org.obiba.opal.spi.vcf.VCFStore} details.
   *
   * @return
   */
  @GET
  public Plugins.VCFStoreDto get(@PathParam("name") String name) {
    initVCFStoreResource(name);
    return Dtos.asDto(store, summaryBuilder.sampleIds(store.getSampleIds()).buildGeneralSummary());
  }

  /**
   * Removes the current {@link org.obiba.opal.core.domain.VCFSamplesMapping} along with all VCF files.
   *
   * @return
   */
  @DELETE
  public Response delete(@PathParam("name") String name) {
    initVCFStoreResource(name);
    removeSamplesMappings(name);
    service.deleteStore(name);
    return Response.noContent().build();
  }

  /**
   * Get a specific {@link org.obiba.opal.core.domain.VCFSamplesMapping}.
   *
   * @return
   */
  @GET
  @Path("/samples")
  public Plugins.VCFSamplesMappingDto getSamplesMapping(@PathParam("name") String name) {
    initVCFStoreResource(name);
    return Dtos.asDto(vcfSamplesMappingService.getVCFSamplesMapping(name));
  }

  @OPTIONS
  @Path("/samples")
  public Response getSamplesMappingOptions() {
    return Response.ok().build();
  }

  /**
   * Update or create a specific {@link org.obiba.opal.core.domain.VCFSamplesMapping}.
   *
   * @param vcfSamplesMappingDto
   * @return
   */
  @PUT
  @Path("/samples")
  public Response putSamplesMapping(@PathParam("name") String name, Plugins.VCFSamplesMappingDto vcfSamplesMappingDto) {
    initVCFStoreResource(name);
    vcfSamplesMappingService.save(Dtos.fromDto(vcfSamplesMappingDto));
    return Response.ok().build();
  }

  /**
   * Delete a specific {@link org.obiba.opal.core.domain.VCFSamplesMapping}.
   *
   * @return
   */
  @DELETE
  @Path("/samples")
  public Response deleteSamplesMapping(@PathParam("name") String name) {
    initVCFStoreResource(name);
    removeSamplesMappings(name);
    return Response.noContent().build();
  }

  /**
   * Exports the statistics .
   *
   * @return
   */
  @GET
  @Produces(value = "text/plain")
  @Path("/vcf/{vcfName}/_statistics")
  public Response getStatistics(@PathParam("name") String name, @PathParam("vcfName") String vcfName) {
    initVCFStoreResource(name);
    StreamingOutput stream = os -> store.readVCFStatistics(vcfName, os);
    return Response.ok(stream)
      .header("Content-Disposition", "attachment; filename=\"" +  store.getName() + "-" + vcfName + "-statistics.txt\"").build();
  }

  /**
   * Get the {@link org.obiba.opal.spi.vcf.VCFStore.VCFSummary} list.
   *
   * @return
   */
  @GET
  @Path("/vcfs")
  public List<Plugins.VCFSummaryDto> getVCFList(@PathParam("name") String name) {
    initVCFStoreResource(name);
    return store.getVCFNames().stream()
      .map(n -> {
        VCFStore.VCFSummary vcfSummary = store.getVCFSummary(n);
        return Dtos.asDto(vcfSummary, summaryBuilder.sampleIds(vcfSummary.getSampleIds()).buildSummary());
      })
      .collect(Collectors.toList());
  }

  @OPTIONS
  @Path("/vcfs")
  public Response getVCFListOptions() {
    return Response.ok().build();
  }
  /**
   * Delete a VCF file. Does not fail if such VCF is not found.
   *
   * @param vcfName
   * @return
   */
  @DELETE
  @Path("/vcf/{vcfName}")
  public Response deleteVCF(@PathParam("name") String name, @PathParam("vcfName") String vcfName) {
    initVCFStoreResource(name);
    return deleteVCFs(name, Lists.newArrayList(vcfName));
  }

  /**
   * Delete a collection of VCF files. Does not fail if one or all VCF file is not found.
   *
   * @param files
   * @return
   */
  @DELETE
  @Path("/vcfs")
  public Response deleteVCFs(@PathParam("name") String name, @QueryParam("file") List<String> files) {
    initVCFStoreResource(name);
    files.forEach(this::deleteVCFFile);
    return Response.noContent().build();
  }

  /**
   * Get a specific {@link org.obiba.opal.spi.vcf.VCFStore.VCFSummary}.
   *
   * @param vcfName
   * @return
   */
  @GET
  @Path("/vcf/{vcfName}")
  public Plugins.VCFSummaryDto getVCF(@PathParam("name") String name, @PathParam("vcfName") String vcfName) {
    initVCFStoreResource(name);
    VCFStore.VCFSummary vcfSummary = store.getVCFSummary(vcfName);
    return Dtos.asDto(vcfSummary, summaryBuilder.sampleIds(vcfSummary.getSampleIds()).buildSummary());
  }

  //
  // Private methods
  //

  private void removeSamplesMappings(String name) {
    VCFSamplesMapping vcfSamplesMapping = vcfSamplesMappingService.getVCFSamplesMapping(name);
    vcfSamplesMappingService.delete(vcfSamplesMapping.getProjectName());
  }

  private void deleteVCFFile(String vcfName) {
    try {
      store.deleteVCF(vcfName);
    } catch (Exception e) {
      // ignore
    }
  }

  private Project getProject(String name) {
    if (!isReadable(name)) throw new NoSuchProjectException(name);
    return projectService.getProject(name);
  }

  private boolean isReadable(String project) {
    return authorizer.isPermitted("rest:/project/" + project + ":GET");
  }

  public void initVCFStoreResource(String name) {
    Project project = getProject(name);
    if (!opalRuntime.hasServicePlugins(VCFStoreService.class)) throw new NoSuchServiceException(VCFStoreService.SERVICE_TYPE);
    if (!project.hasVCFStoreService()) throw new NotFoundException("Project has no VCF store: " + project.getName());
    String serviceName = project.getVCFStoreService();

    if (!opalRuntime.hasServicePlugin(serviceName)) throw new NoSuchElementException("No VCF store service: " + serviceName);
    ServicePlugin servicePlugin = opalRuntime.getServicePlugin(serviceName);
    if (servicePlugin instanceof VCFStoreService) {
      service = (VCFStoreService) servicePlugin;
      if (!service.hasStore(name)) service.createStore(name);
      store = service.getStore(name);
      summaryBuilder = new VCFSamplesSummaryBuilder();
    } else  throw new NoSuchElementException("No VCF store service: " + serviceName);

    try {
      summaryBuilder.mappings(vcfSamplesMappingService.getVCFSamplesMapping(name));
    } catch (NoSuchVCFSamplesMappingException e) {
      // ignore
    }
  }
}
