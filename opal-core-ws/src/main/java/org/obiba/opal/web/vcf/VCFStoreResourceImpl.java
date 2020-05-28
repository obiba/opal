/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.vcf;


import com.google.common.collect.Lists;
import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.NoSuchVCFSamplesMappingException;
import org.obiba.opal.core.service.VCFSamplesMappingService;
import org.obiba.opal.core.support.vcf.VCFSamplesSummaryBuilder;
import org.obiba.plugins.spi.ServicePlugin;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.plugins.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VCFStoreResourceImpl implements VCFStoreResource {

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private VCFSamplesMappingService vcfSamplesMappingService;

  private VCFStore store;

  private VCFSamplesSummaryBuilder summaryBuilder;

  private String name;

  private VCFStoreService service;

  @Override
  public void setVCFStore(String serviceName, String name) {
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

    this.name = name;
  }

  @Override
  public Plugins.VCFStoreDto get() {
    return Dtos.asDto(store, summaryBuilder.sampleIds(store.getSampleIds()).buildGeneralSummary());
  }

  @Override
  public Response delete() {
    removeSamplesMappings();
    service.deleteStore(name);
    return Response.noContent().build();
  }

  @Override
  public Plugins.VCFSamplesMappingDto getSamplesMapping() {
    return Dtos.asDto(vcfSamplesMappingService.getVCFSamplesMapping(name));
  }

  @Override
  public Response putSamplesMapping(Plugins.VCFSamplesMappingDto vcfSamplesMappingDto) {
    vcfSamplesMappingService.save(Dtos.fromDto(vcfSamplesMappingDto));
    return Response.ok().build();
  }

  @Override
  public Response deleteSamplesMapping() {
    removeSamplesMappings();
    return Response.noContent().build();
  }

  @Override
  public Response getStatistics(String vcfName) {
    StreamingOutput stream = os -> store.readVCFStatistics(vcfName, os);
    return Response.ok(stream)
      .header("Content-Disposition", "attachment; filename=\"" +  store.getName() + "-" + vcfName + "-statistics.txt\"").build();
  }

  private void removeSamplesMappings() {
    VCFSamplesMapping vcfSamplesMapping = vcfSamplesMappingService.getVCFSamplesMapping(name);
    vcfSamplesMappingService.delete(vcfSamplesMapping.getProjectName());
  }

  @Override
  public List<Plugins.VCFSummaryDto> getVCFList() {
    return store.getVCFNames().stream()
      .map(n -> {
        VCFStore.VCFSummary vcfSummary = store.getVCFSummary(n);
        return Dtos.asDto(vcfSummary, summaryBuilder.sampleIds(vcfSummary.getSampleIds()).buildSummary());
      })
      .collect(Collectors.toList());
  }

  @Override
  public Response deleteVCF(String vcfName) {
    return deleteVCFs(Lists.newArrayList(vcfName));
  }

  @Override
  public Response deleteVCFs(List<String> files) {
    files.forEach(vcfName -> deleteVCFFile(vcfName));
    return Response.noContent().build();
  }

  private void deleteVCFFile(String vcfName) {
    try {
      store.deleteVCF(vcfName);
    } catch (Exception e) {
      // ignore
    }
  }

  @Override
  public Plugins.VCFSummaryDto getVCF(String vcfName) {
    VCFStore.VCFSummary vcfSummary = store.getVCFSummary(vcfName);
    return Dtos.asDto(vcfSummary, summaryBuilder.sampleIds(vcfSummary.getSampleIds()).buildSummary());
  }

}
