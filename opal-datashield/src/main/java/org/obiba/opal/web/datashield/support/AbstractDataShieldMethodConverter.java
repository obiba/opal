/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield.support;

import org.obiba.datashield.core.DSMethod;
import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.web.model.DataShield.DataShieldMethodDto;

/**
 *
 */
public abstract class AbstractDataShieldMethodConverter implements DataShieldMethodConverter {
  protected DataShieldMethodDto.Builder getDataShieldMethodDtoBuilder(DSMethod method) {
    return DataShieldMethodDto.newBuilder().setName(method.getName());
  }
}
