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
import org.obiba.magma.datasource.hibernate.support.HibernateDatasourceFactory;
import org.obiba.magma.datasource.hibernate.support.SpringBeanSessionFactoryProvider;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.HibernateDatasourceFactoryDto;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class HibernateDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  @Autowired
  private BeanFactory beanFactory;

  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto) {
    HibernateDatasourceFactory factory = new HibernateDatasourceFactory();
    HibernateDatasourceFactoryDto hDto = dto.getExtension(HibernateDatasourceFactoryDto.params);
    if(hDto.getKey()) {
      factory.setSessionFactoryProvider(new SpringBeanSessionFactoryProvider(beanFactory, "keySessionFactory"));
    } else {
      factory.setSessionFactoryProvider(new SpringBeanSessionFactoryProvider(beanFactory, "opalSessionFactory"));
    }
    return factory;
  }

  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(HibernateDatasourceFactoryDto.params);
  }

}
