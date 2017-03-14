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


import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.plugins.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VCFStoreResourceImpl implements VCFStoreResource {

  @Autowired
  private OpalRuntime opalRuntime;

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

}
