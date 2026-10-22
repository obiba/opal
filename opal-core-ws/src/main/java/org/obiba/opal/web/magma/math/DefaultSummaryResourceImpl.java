/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.math;

import jakarta.ws.rs.core.Response;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@Transactional
public class DefaultSummaryResourceImpl extends AbstractSummaryResource implements DefaultSummaryResource {

  @Override
  public Response get(Integer offset, Integer limit, boolean fullIfCached, boolean resetCache) {
    return getDefaultSummary(offset, limit, fullIfCached, resetCache);
  }

}
