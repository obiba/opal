/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;

public abstract class AbstractDatasourceFactoryDtoParser implements DatasourceFactoryDtoParser {
  DatasourceFactoryDtoParser next;

  public DatasourceFactoryDtoParser setNext(DatasourceFactoryDtoParser next) {
    this.next = next;
    return next;
  }

  public DatasourceFactory parse(DatasourceFactoryDto dto) {
    DatasourceFactory factory = internalParse(dto);
    if(factory == null && next != null) {
      factory = next.parse(dto);
    }
    return factory;
  }

  protected abstract DatasourceFactory internalParse(DatasourceFactoryDto dto);
}
