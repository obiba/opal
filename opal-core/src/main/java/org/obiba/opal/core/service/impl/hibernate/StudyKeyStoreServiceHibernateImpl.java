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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.obiba.opal.core.service.impl.DefaultStudyKeyStoreServiceImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class StudyKeyStoreServiceHibernateImpl extends DefaultStudyKeyStoreServiceImpl {

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  @SuppressWarnings("unchecked")
  public Set<String> getStudyIds() {
    String queryString = "select studyId from org.obiba.opal.core.crypt.StudyKeyStore";
    Query query = factory.getCurrentSession().createQuery(queryString);

    List<String> studyIds = query.list();
    if(studyIds.isEmpty()) {
      return Collections.emptySet();
    } else {
      return new HashSet<String>(studyIds);
    }
  }
}
