/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.subject;

import org.obiba.opal.core.domain.sql.SQLExecution;
import org.obiba.opal.core.service.SQLService;
import org.obiba.opal.web.model.SQL;
import org.obiba.opal.web.sql.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SQLHistoryResourceImpl implements SQLHistoryResource {

  @Autowired
  private SQLService sqlService;

  private String subject;

  @Override
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @Override
  public List<SQL.SQLExecutionDto> getSQLHistory(String datasource, int offset, int limit) {
    List<SQLExecution> execs = sqlService.getSQLExecutions(subject, datasource);
    return execs.subList(offset, Math.min(execs.size(), offset + limit)).stream()
        .map(Dtos::asDto).collect(Collectors.toList());
  }
}
