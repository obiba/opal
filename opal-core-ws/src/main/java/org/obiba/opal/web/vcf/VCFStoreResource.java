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

import org.obiba.opal.web.model.Plugins;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * A REST resource to access to a {@link org.obiba.opal.spi.vcf.VCFStore}.
 */
public interface VCFStoreResource {

  /**
   * The store name for the VCF store service.
   *
   * @param serviceName
   * @param name
   */
  void setVCFStore(String serviceName, String name);

  /**
   * Get the {@link org.obiba.opal.spi.vcf.VCFStore} details.
   *
   * @return
   */
  @GET
  Plugins.VCFStoreDto get();

  /**
   * Removes the current {@link org.obiba.opal.core.domain.VCFSamplesMapping} along with all VCF files.
   *
   * @return
   */
  @DELETE
  Response delete();

  /**
   * Get a specific {@link org.obiba.opal.core.domain.VCFSamplesMapping}.
   *
   * @return
   */
  @GET
  @Path("/samples")
  Plugins.VCFSamplesMappingDto getSamplesMapping();

  /**
   * Update or create a specific {@link org.obiba.opal.core.domain.VCFSamplesMapping}.
   *
   * @param vcfSamplesMappingDto
   * @return
   */
  @PUT
  @Path("/samples")
  Response putSamplesMapping(Plugins.VCFSamplesMappingDto vcfSamplesMappingDto);

  /**
   * Delete a specific {@link org.obiba.opal.core.domain.VCFSamplesMapping}.
   *
   * @return
   */
  @DELETE
  @Path("/samples")
  Response deleteSamplesMapping();

  /**
   * Exports the statistics .
   *
   * @return
   */
  @GET
  @Produces(value = "text/plain")
  @Path("/vcf/{vcfName}/_statistics")
  Response getStatistics(@PathParam("vcfName") String vcfName);

  /**
   * Get the {@link org.obiba.opal.spi.vcf.VCFStore.VCFSummary} list.
   *
   * @return
   */
  @GET
  @Path("/vcfs")
  List<Plugins.VCFSummaryDto> getVCFList();

  /**
   * Delete a collection of VCF files. Does not fail if one or all VCF file is not found.
   *
   * @param vcfName
   * @return
   */
  @DELETE
  @Path("/vcfs")
  Response deleteVCFs(@QueryParam("file") List<String> files);

  /**
   * Delete a VCF file. Does not fail if such VCF is not found.
   *
   * @param vcfName
   * @return
   */
  @DELETE
  @Path("/vcf/{vcfName}")
  Response deleteVCF(@PathParam("vcfName") String vcfName);

  /**
   * Get a specific {@link org.obiba.opal.spi.vcf.VCFStore.VCFSummary}.
   *
   * @param vcfName
   * @return
   */
  @GET
  @Path("/vcf/{vcfName}")
  Plugins.VCFSummaryDto getVCF(@PathParam("vcfName") String vcfName);
}
