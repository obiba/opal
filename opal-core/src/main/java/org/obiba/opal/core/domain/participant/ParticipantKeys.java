/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.participant;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;

@Entity
@Table(name = "PARTICIPANT_KEYS")
public class ParticipantKeys {

  @SuppressWarnings("unused")
  @Id
  @GeneratedValue
  @Column
  private long id;

  // TODO Must add contraints to the database the "id" (owner) and value must be unique. eg. constrain(id, value.item).
  @CollectionOfElements(targetElement = String.class)
  @Column(name = "ITEMS", nullable = false)
  private final Set<String> values = new HashSet<String>();

  void addKey(String key) {
    values.add(key);
  }

  Collection<String> getKeys() {
    return Collections.checkedCollection(values, String.class);
  }

  boolean contains(String key) {
    return values.contains(key);
  }

  void remove(String key) {
    values.remove(key);
  }

  int size() {
    return values.size();
  }

}
