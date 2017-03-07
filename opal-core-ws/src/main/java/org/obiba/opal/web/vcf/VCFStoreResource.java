/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * A REST resource to access to a {@link org.obiba.opal.spi.vcf.VCFStore}.
 */
public interface VCFStoreResource {

  /**
   * The store name.
   *
   * @param name
   */
  void setName(String name);

  /**
   * Get the {@link org.obiba.opal.spi.vcf.VCFStore} details.
   *
   * @return
   */
  @GET
  Plugins.VCFStoreDto get();

  /**
   * Get the {@link org.obiba.opal.spi.vcf.VCFStore.VCFSummary} list.
   *
   * @return
   */
  @GET
  @Path("/vcfs")
  List<Plugins.VCFSummaryDto> getVCFList();

  /**
   * Add a VCF file at the specified location in Opal file system. If name is not provided, the base name of the file
   * will be used.
   *
   * @param uriInfo
   * @param vcfPath
   * @param name Optional VCF file base name
   * @return
   */
  @POST
  @Path("/vcfs")
  Response addVCF(@Context UriInfo uriInfo, @QueryParam("file") String vcfPath, @QueryParam("name") String name);

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

  /**
   * Download the (compressed) VCF file, optionally encrypted.
   *
   * @param vcfName
   * @param table Table fully qualified name that provides either a Participant or a Sample list, to be used for subsetting the VCF file.
   * @param withCaseControls
   * @param fileKey
   * @return
   */
  @GET
  @Path("/vcf/{vcfName}/_download")
  Response downloadVCF(@PathParam("vcfName") String vcfName, @QueryParam("table") String table,
                       @QueryParam("cc") boolean withCaseControls, @HeaderParam("X-File-Key") String fileKey);
}
