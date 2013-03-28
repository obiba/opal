/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
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
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.IndexSynchronizationManager;
import org.obiba.opal.search.Schedule;
import org.obiba.opal.search.ValueTableValuesIndex;
import org.obiba.opal.search.ValuesIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class IndexResource {

  protected final ValuesIndexManager indexManager;

  protected final IndexManagerConfigurationService configService;

  protected final ElasticSearchProvider esProvider;

  protected final IndexSynchronizationManager synchroManager;

  @Autowired
  protected IndexResource(ValuesIndexManager indexManager, IndexManagerConfigurationService configService,
      ElasticSearchProvider esProvider, IndexSynchronizationManager synchroManager) {
    this.indexManager = indexManager;
    this.configService = configService;
    this.esProvider = esProvider;
    this.synchroManager = synchroManager;
  }

  protected Opal.ScheduleDto getScheduleDto(String datasource, String table) {
    // Set schedule
    Schedule schedule = configService.getConfig().getSchedule(getValueTable(datasource, table));

    if(schedule == null) {
      return Opal.ScheduleDto.newBuilder().setType(Opal.ScheduleType.MINUTES_15).build();
    }

    Opal.ScheduleDto dto = Opal.ScheduleDto.newBuilder().setType(schedule.getType()).build();

    if(schedule.getDay() != null) {
      dto = dto.toBuilder().setDay(schedule.getDay()).build();
    }
    if(schedule.getHours() != null) {
      dto = dto.toBuilder().setHours(schedule.getHours()).build();
    }
    if(schedule.getMinutes() != null) {
      dto = dto.toBuilder().setMinutes(schedule.getMinutes()).build();
    }

    return dto;
  }

  protected ValueTable getValueTable(String datasource, String table) {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }

  protected float getValueTableIndexationProgress(String table) {
    float progress = 0f;
    IndexSynchronization currentTask = synchroManager.getCurrentTask();
    if(currentTask != null && currentTask.getValueTable().getName().equals(table)) {

      progress = synchroManager.getCurrentTask().getProgress();
    }
    return progress;
  }

  protected boolean isInProgress(String table) {
    return Float.compare(getValueTableIndexationProgress(table), 0f) > 0;
  }

  protected ValueTableValuesIndex getValueTableIndex(String datasource, String table) {
    return indexManager.getIndex(getValueTable(datasource, table));
  }

  protected Opal.TableIndexationStatus getTableIndexationStatus(String datasource, String table) {
    boolean inProgress = isInProgress(table);

    // Set Indexation status
    boolean upToDate = getValueTableIndex(datasource, table).isUpToDate();
    if(indexManager.isReady() && !upToDate && !inProgress) {
      return Opal.TableIndexationStatus.OUTDATED;
    }
    if(indexManager.isReady() && inProgress) {
      return Opal.TableIndexationStatus.IN_PROGRESS;
    }
    if(indexManager.isReady() && upToDate) {
      return Opal.TableIndexationStatus.UPTODATE;
    }
    return Opal.TableIndexationStatus.NOT_INDEXED;
  }
}
