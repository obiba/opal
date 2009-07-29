/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.opal.core.domain.metadata.Catalogue;
import org.obiba.opal.core.service.MetaDataService;

/**
 *
 */
public class MetaDataServiceHibernateImpl implements MetaDataService {

  private SessionFactory sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public Catalogue getCatalogue(String datasource, String name) {
    List<Catalogue> catalogues = AssociationCriteria.create(Catalogue.class, sessionFactory.getCurrentSession()).add("datasource", Operation.eq, datasource).add("name", Operation.eq, name).list();
    if(catalogues == null || catalogues.size() == 0) {
      return null;
    }
    if(catalogues.size() > 1) {
      throw new IllegalStateException("multiple catalogues: " + datasource + ":" + name);
    }
    return catalogues.get(0);
  }

}
