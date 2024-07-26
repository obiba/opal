/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import com.google.common.eventbus.EventBus;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.event.ValueTableDeletedEvent;
import org.obiba.opal.core.service.SubjectProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.core.Response;

/**
 * A table resource that supports DELETE (drop)
 */
@Component("droppableTableResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DroppableTableResourceImpl extends TableResourceImpl implements DroppableTableResource {

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private EventBus eventBus;

  @Override
  @DELETE
  public Response drop() {
    try {
      ValueTable table = getValueTable();
      if (table.isView()) {
        viewManager.removeView(table.getDatasource().getName(), table.getName());
      } else {
        getDatasource().dropTable(table.getName());
      }
      eventBus.post(new ValueTableDeletedEvent(table));
      subjectProfileService.deleteBookmarks("/datasource/" + getDatasource().getName() + "/table/" + getValueTable().getName());
    } catch (NoSuchValueTableException e) {
      // ignore
    }
    return Response.ok().build();
  }
}
