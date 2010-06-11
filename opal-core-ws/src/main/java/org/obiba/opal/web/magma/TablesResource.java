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
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TablesResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TablesResource.class);

  private final String datasource;

  public TablesResource(String datasource) {
    this.datasource = datasource;
  }

  @GET
  public List<Magma.TableDto> getTables() {
    final List<Magma.TableDto> tables = Lists.newArrayList();
    Datasource ds = MagmaEngine.get().getDatasource(datasource);
    for(ValueTable valueTable : ds.getValueTables()) {
      URI tableLink = UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable").build(ds.getName(), valueTable.getName());
      Magma.TableDto.Builder table = Magma.TableDto.newBuilder() //
      .setName(valueTable.getName()) //
      .setEntityType(valueTable.getEntityType()) //
      .setVariableCount(Iterables.size(valueTable.getVariables())) //
      .setValueSetCount(valueTable.getVariableEntities().size()) //
      .setLink(tableLink.toString());
      tables.add(table.build());
    }
    return tables;
  }
}
