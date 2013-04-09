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

import java.util.Set;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.nil.support.NullDatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class DatasourceFactoryRegistry {

  private final Set<DatasourceFactoryDtoParser> parsers;

  @Autowired
  public DatasourceFactoryRegistry(Set<DatasourceFactoryDtoParser> parsers) {
    if(parsers == null) throw new IllegalArgumentException("parsers cannot be null");
    this.parsers = parsers;
  }

  /**
   * Parses the provided {@code DatasourceFactoryDto} instance using one of the registered {@code
   * DatasourceFactoryDtoParser} instance. If none of the registered {@code DatasourceFactoryDtoParser} is able to parse
   * the given {@code DatasourceFactoryDto} this method will throw a {@code NoSuchDatasourceFactoryException}
   *
   * @param dto the {@code DatasourceFactoryDto} to parse
   * @return an instance of {@code DatasourceFactory} for the given {@code dto}
   * {@code dto}
   */
  public DatasourceFactory parse(DatasourceFactoryDto dto) throws NoSuchDatasourceFactoryException {
    if(dto == null) throw new IllegalArgumentException("dto cannot be null");
    for(DatasourceFactoryDtoParser parser : parsers) {
      if(parser.canParse(dto)) {
        return parser.parse(dto);
      }
    }
    DatasourceFactory factory = new NullDatasourceFactory();
    factory.setName(dto.getName());
    return factory;
  }

}
