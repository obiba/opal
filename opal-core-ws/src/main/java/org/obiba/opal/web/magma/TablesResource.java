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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class TablesResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TablesResource.class);

  private final Datasource datasource;

  public TablesResource(Datasource datasource) {
    if(datasource == null) throw new IllegalArgumentException("datasource cannot be null");
    this.datasource = datasource;
  }

  @GET
  public List<Magma.TableDto> getTables() {
    final List<Magma.TableDto> tables = Lists.newArrayList();
    UriBuilder tableLink = UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable");
    for(ValueTable valueTable : datasource.getValueTables()) {
      tables.add(Dtos.asDto(valueTable, tableLink).build());
    }
    sortByName(tables);

    return tables;
  }

  private void sortByName(List<Magma.TableDto> tables) {
    // sort alphabetically
    Collections.sort(tables, new Comparator<Magma.TableDto>() {

      @Override
      public int compare(TableDto t1, TableDto t2) {
        return t1.getName().compareTo(t2.getName());
      }

    });
  }
}
