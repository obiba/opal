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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.obiba.opal.core.domain.participant.Participant;
import org.obiba.opal.core.domain.participant.ParticipantKey;
import org.obiba.opal.core.service.impl.DefaultParticipantKeyRegistryImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public final class ParticipantKeyRegistryHibernateImpl extends DefaultParticipantKeyRegistryImpl {

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  @SuppressWarnings("unchecked")
  protected Participant getParticipant(String owner, String key) {
    assert owner != null : "The owner must not be null.";
    assert key != null : "The key must not be null.";

    List<ParticipantKey> result = getSession().createCriteria(ParticipantKey.class).add(Restrictions.eq("owner", owner)).add(Restrictions.eq("value", key)).list();

    if(result.size() == 1) {
      return result.get(0).getParticipant();
    } else if(result.size() == 0) {
      return null;
    } else {
      throw new IllegalStateException("Duplicate key mapping for owner=[" + owner + "] key=[" + key + "].");
    }
  }

}
