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

public abstract class IndexResource {

  @Autowired
  protected ValuesIndexManager indexManager;

  @Autowired
  protected IndexManagerConfigurationService configService;

  @Autowired
  protected ElasticSearchProvider esProvider;

  @Autowired
  protected IndexSynchronizationManager synchroManager;

  protected Opal.ScheduleDto getScheduleDto(String datasource, String table) {

    Schedule schedule = configService.getConfig().getSchedule(getValueTable(datasource, table));
    if(schedule == null) {
      return Opal.ScheduleDto.newBuilder().setType(Opal.ScheduleType.MINUTES_15).build();
    }

    Opal.ScheduleDto.Builder dtoBuilder = Opal.ScheduleDto.newBuilder().setType(schedule.getType());
    if(schedule.getDay() != null) {
      dtoBuilder.setDay(schedule.getDay());
    }
    if(schedule.getHours() != null) {
      dtoBuilder.setHours(schedule.getHours());
    }
    if(schedule.getMinutes() != null) {
      dtoBuilder.setMinutes(schedule.getMinutes());
    }
    return dtoBuilder.build();
  }

  protected ValueTable getValueTable(String datasource, String table) {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }

  protected Float getValueTableIndexationProgress(String datasource, String table) {
    IndexSynchronization currentTask = synchroManager.getCurrentTask();
    if(currentTask != null && currentTask.getValueTable().getName().equals(table) &&
        currentTask.getValueTable().getDatasource().getName().equals(datasource)) {
      return synchroManager.getCurrentTask().getProgress();
    }

    return synchroManager.isAlreadyQueued(indexManager, getValueTableIndex(datasource, table)) ? 0f : null;
  }

  protected boolean isInProgress(String datasource, String table) {
    return getValueTableIndexationProgress(datasource, table) != null;
  }

  protected ValueTableValuesIndex getValueTableIndex(String datasource, String table) {
    return indexManager.getIndex(getValueTable(datasource, table));
  }

  protected Opal.TableIndexationStatus getTableIndexationStatus(String datasource, String table) {
    boolean inProgress = isInProgress(datasource, table);

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
