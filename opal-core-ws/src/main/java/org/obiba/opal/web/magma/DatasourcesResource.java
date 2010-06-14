/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/datasources")
public class DatasourcesResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DatasourcesResource.class);

  private String keysDatasourceName;

  @Autowired
  public DatasourcesResource(@Value("${org.obiba.opal.keys.tableReference}") String keysTableReference) {
    keysDatasourceName = extractKeysDatasourceName(keysTableReference);
  }

  @GET
  public List<Magma.DatasourceDto> getDatasources() {
    final List<Magma.DatasourceDto> datasources = Lists.newArrayList();

    for(Datasource from : MagmaEngine.get().getDatasources()) {
      // OPAL-365: Hide the keys datasource.
      if(from.getName().equals(keysDatasourceName)) continue;

      URI dslink = UriBuilder.fromPath("/").path(DatasourceResource.class).build(from.getName());
      Magma.DatasourceDto.Builder ds = Magma.DatasourceDto.newBuilder() //
      .setName(from.getName()) //
      .setLink(dslink.toString());
      for(ValueTable table : from.getValueTables()) {
        ds.addTable(table.getName());
      }
      datasources.add(ds.build());
    }
    return datasources;
  }

  private String extractKeysDatasourceName(String keysTableReference) {
    if(keysTableReference == null) {
      throw new IllegalArgumentException("null keysTableReference");
    }

    int separatorIndex = keysTableReference.indexOf('.');
    if(separatorIndex == -1) {
      throw new IllegalArgumentException("keysTableReference missing datasource");
    }

    return keysTableReference.substring(0, separatorIndex);
  }
}
