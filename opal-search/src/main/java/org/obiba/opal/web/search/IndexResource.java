/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronizationManager;
import org.obiba.opal.search.Schedule;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class IndexResource {

  protected final IndexManager indexManager;

  protected final IndexManagerConfigurationService configService;

  protected final ElasticSearchProvider esProvider;

  protected final IndexSynchronizationManager synchroManager;

  @Autowired
  protected IndexResource(IndexManager indexManager, IndexManagerConfigurationService configService,
      ElasticSearchProvider esProvider, IndexSynchronizationManager synchroManager) {
    this.indexManager = indexManager;
    this.configService = configService;
    this.esProvider = esProvider;
    this.synchroManager = synchroManager;
  }

  protected Opal.ScheduleDto getScheduleDto(String datasource, String table) {
    // Set schedule
    Schedule schedule = configService.getConfig().getSchedule(getValueTable(datasource, table));

    if(schedule == null) return Opal.ScheduleDto.newBuilder().setType(Opal.ScheduleType.NOT_SCHEDULED).build();

    return Opal.ScheduleDto.newBuilder().setType(schedule.getType()).setDay(schedule.getDay())
        .setHours(schedule.getHours()).setMinutes(schedule.getMinutes()).build();
  }

  protected ValueTable getValueTable(String datasource, String table) {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }

  protected float getValueTableIndexationProgress(String table) {
    float progress = 0f;
    if(synchroManager.hasTask() && synchroManager.getCurrentTask().getValueTable().getName().equals(table)) {

      progress = synchroManager.getCurrentTask().getProgress();
    }
    return progress;
  }

  protected boolean isInProgress(String table) {
    return (Float.compare(getValueTableIndexationProgress(table), 0f) > 1);
  }

  protected ValueTableIndex getValueTableIndex(String datasource, String table) {
    return this.indexManager.getIndex(getValueTable(datasource, table));
  }

  protected Opal.TableIndexationStatus getTableIndexationStatus(String datasource, String table) {
    boolean inProgress = isInProgress(table);
    ValueTable valueTable = getValueTable(datasource, table);

    // Set Indexation status
    boolean upToDate = getValueTableIndex(datasource, table).isUpToDate();
    if(indexManager.isIndexable(valueTable) && !upToDate && !inProgress) {
      return Opal.TableIndexationStatus.OUTDATED;
    } else if(indexManager.isIndexable(valueTable) && !upToDate && inProgress) {
      return Opal.TableIndexationStatus.IN_PROGRESS;
    } else if(indexManager.isIndexable(valueTable) && upToDate) {
      return Opal.TableIndexationStatus.UPTODATE;
    }
    return Opal.TableIndexationStatus.NOT_INDEXED;
  }
}
