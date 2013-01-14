/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronizationManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/service/search/indices")
public class SearchServiceResource extends IndexResource {

  @Autowired
  public SearchServiceResource(IndexManager indexManager, IndexManagerConfigurationService configService,
      ElasticSearchProvider esProvider, IndexSynchronizationManager synchroManager) {

    super(indexManager, configService, esProvider, synchroManager);
  }

  @GET
  public List<Opal.TableIndexStatusDto> indices() {
    List<Opal.TableIndexStatusDto> tableStatusDtos = Lists.newArrayList();

    // isrunning
    if(!esProvider.isEnabled() && esProvider.getClient() == null) return tableStatusDtos;

    Set<Datasource> datasources = MagmaEngine.get().getDatasources();
    for(Datasource datasource : datasources) {

      Set<ValueTable> valueTables = MagmaEngine.get().getDatasource(datasource.getName()).getValueTables();
      for(ValueTable valueTable : valueTables) {
        ValueTable table = MagmaEngine.get().getDatasource(datasource.getName()).getValueTable(valueTable.getName());

        float progress = 0f;
        if(synchroManager.getCurrentTask() != null && synchroManager.getCurrentTask().getValueTable().getName()
            .equals(table.getName())) {
          progress = synchroManager.getCurrentTask().getProgress();
        }

        URI link = UriBuilder.fromPath("/").path(ValueTableIndexResource.class)
            .build(datasource.getName(), table.getName());
        Opal.TableIndexStatusDto tableStatusDto = Opal.TableIndexStatusDto.newBuilder()
            .setDatasource(datasource.getName()).setTable(table.getName())
            .setSchedule(getScheduleDto(datasource.getName(), table.getName()))
            .setStatus(getTableIndexationStatus(datasource.getName(), table.getName())).setProgress(progress)
            .setLink(link.getPath()).setTableLastUpdate(valueTable.getTimestamps().getLastUpdate().toString()).build();

        if(!indexManager.getIndex(valueTable).getTimestamps().getCreated().isNull()) {
          tableStatusDto = tableStatusDto.toBuilder()
              .setIndexCreated(indexManager.getIndex(valueTable).getTimestamps().getCreated().toString()).build();
        }
        if(!indexManager.getIndex(valueTable).getTimestamps().getLastUpdate().isNull()) {
          tableStatusDto = tableStatusDto.toBuilder()
              .setIndexLastUpdate(indexManager.getIndex(valueTable).getTimestamps().getLastUpdate().toString()).build();
        }
        tableStatusDtos.add(tableStatusDto);
      }
    }

    return tableStatusDtos;
  }
}
