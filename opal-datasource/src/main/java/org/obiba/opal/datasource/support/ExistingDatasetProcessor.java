/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.support;

import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.opal.core.domain.data.Dataset;
import org.springframework.batch.item.ItemProcessor;

/**
 * Skips {@link Dataset} entries already present. A dataset already exists if it is for the same entity from the same datasource
 * with the same extraction date.
 */
public class ExistingDatasetProcessor implements ItemProcessor<Dataset, Dataset> {

  private SessionFactory sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public Dataset process(Dataset item) throws Exception {
    // Don't use template-based comparision due to Dataset.creationDate; which is set in Dataset ctor.
    int count = AssociationCriteria.create(Dataset.class, sessionFactory.getCurrentSession()).add("entity", Operation.match, item.getEntity()).add("datasource", Operation.eq, item.getDatasource()).add("extractionDate", Operation.eq, item.getExtractionDate()).count();
    // If dataset already exists, then skip it (return null)
    return count > 0 ? null : item;
  }
}
