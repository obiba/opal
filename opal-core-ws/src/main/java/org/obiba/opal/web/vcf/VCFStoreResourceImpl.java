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


import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.obiba.opal.web.model.Plugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VCFStoreResourceImpl implements VCFStoreResource {

  private static final Logger log = LoggerFactory.getLogger(VCFStoreResourceImpl.class);

  @Autowired
  private OpalRuntime opalRuntime;

  private final MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

  private VCFStore store;

  @Override
  public void setVCFStore(String serviceName, String name) {
    if (!opalRuntime.hasVCFStoreServices()) throw new NoSuchElementException("No VCF store service is available");
    VCFStoreService service = opalRuntime.getVCFStoreService(serviceName);
    if (!service.hasStore(name)) service.createStore(name);
    store = service.getStore(name);
  }

  @Override
  public Plugins.VCFStoreDto get() {
    return Dtos.asDto(store);
  }

  @Override
  public List<Plugins.VCFSummaryDto> getVCFList() {
    return store.getVCFNames().stream().map(n -> Dtos.asDto(store.getVCFSummary(n))).collect(Collectors.toList());
  }

  @Override
  public Response addVCF(UriInfo uriInfo, String vcfPath, String name) {
    try {
      FileObject fileObject = resolveFileInFileSystem(vcfPath);
      if (fileObject == null || !fileObject.exists() || fileObject.getType() == FileType.FOLDER)
        throw new IllegalArgumentException("Not a valid path to VCF file: " + vcfPath);
      if (!fileObject.isReadable()) throw new IllegalArgumentException("VCF file is not readable: " + vcfPath);
      File vcfFile = opalRuntime.getFileSystem().getLocalFile(fileObject);
      String vcfName = name;
      if (Strings.isNullOrEmpty(name)) vcfName = vcfFile.getName();
      store.writeVCF(vcfName, new FileInputStream(vcfFile));
      //String realVcfName = vcfName.replaceAll("\\.vcf\\.gz$", "").replaceAll("\\.vcf$", "");
      //URI vcfUri = uriInfo.getBaseUriBuilder().path("project").path(name).path("vcf-store").path(realVcfName).build();
      //return Response.created(vcfUri).build();
      return Response.ok().build();
    } catch (FileNotFoundException e) {
      // not supposed to happen as file was verified
      return Response.serverError().build();
    } catch (Exception e) {
      log.error("Failed at handling the VCF file: {}", vcfPath, e);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Override
  public Response deleteVCF(String vcfName) {
    try {
      store.deleteVCF(vcfName);
    } catch (Exception e) {
      // ignore
    }
    return Response.noContent().build();
  }

  @Override
  public Plugins.VCFSummaryDto getVCF(String vcfName) {
    return Dtos.asDto(store.getVCFSummary(vcfName));
  }

  @Override
  public Response downloadVCF(String vcfName, String table, boolean withCaseControls, @HeaderParam("X-File-Key") String fileKey) {
    String vcfFileName = vcfName + ".vcf.gz";
    String fileName = Strings.isNullOrEmpty(fileKey) ? vcfFileName : vcfFileName + ".zip";
    String mimeType = mimeTypes.getContentType(fileName);
    store.getVCFSummary(vcfName);

    StreamingOutput stream = os -> {
      // if file key is provided, file is encrypted in a zip
      if (!Strings.isNullOrEmpty(fileKey)) {
        // TODO
      } else {
        store.readVCF(vcfName, os);
      }
    };

    return Response.ok(stream, mimeType)
        .header("Content-Disposition", getContentDispositionOfAttachment(fileName)).build();
  }

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    if (Strings.isNullOrEmpty(path)) return null;
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  private String getContentDispositionOfAttachment(String fileName) {
    return "attachment; filename=\"" + fileName + "\"";
  }

}
