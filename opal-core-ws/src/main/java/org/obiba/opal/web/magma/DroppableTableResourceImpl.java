/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.ValueTableUpdateListener;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A table resource that supports DELETE (drop)
 */
@Component("droppableTableResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DroppableTableResourceImpl extends TableResourceImpl implements DroppableTableResource {

  @Autowired
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private Collection<ValueTableUpdateListener> tableListeners;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private OpalAnalysisService opalAnalysisService;

  @Override
  @DELETE
  public Response drop() {
    try {
      ValueTable table = getValueTable();
      getDatasource().dropTable(table.getName());
      if (tableListeners != null && !tableListeners.isEmpty()) {
        for (ValueTableUpdateListener listener : tableListeners) {
          listener.onDelete(table);
        }
      }
      subjectProfileService.deleteBookmarks("/datasource/" + getDatasource().getName() + "/table/" + getValueTable().getName());

      opalAnalysisService.deleteAnalyses(getDatasource().getName(), getValueTable().getName());
    } catch (NoSuchValueTableException e) {
      // ignore
    }
    return Response.ok().build();
  }
}
