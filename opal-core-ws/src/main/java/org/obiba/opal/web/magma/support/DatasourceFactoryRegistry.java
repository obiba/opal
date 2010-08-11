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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class DatasourceFactoryRegistry implements ApplicationContextAware {

  private DatasourceFactoryDtoParser parser;

  public DatasourceFactory parse(DatasourceFactoryDto dto) {
    return parser.parse(dto);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DatasourceFactoryDtoParser current = null;
    // chain the parsers that live in Spring
    for(DatasourceFactoryDtoParser p : applicationContext.getBeansOfType(DatasourceFactoryDtoParser.class).values()) {
      if(current == null) {
        this.parser = p;
        current = p;
      } else {
        current = current.setNext(p);
      }
    }
  }

}
