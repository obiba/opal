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
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class DatasourceFactoryRegistry {

  private DatasourceFactoryDtoParser parser;

  public DatasourceFactoryRegistry() {
    super();
    this.parser = new ExcelDatasourceFactoryDtoParser();
    this.parser.setNext(new CsvDatasourceFactoryDtoParser());
  }

  public DatasourceFactory parse(DatasourceFactoryDto dto) {
    return parser.parse(dto);
  }

}
