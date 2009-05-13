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
import javax.persistence.Id;

import org.hibernate.annotations.CollectionOfElements;

@Entity
public class Keys {

  @SuppressWarnings("unused")
  @Id
  @Column(name = "id")
  private Integer id;

  @CollectionOfElements
  private final Set<String> keys = new HashSet<String>();

  void addKey(String key) {
    keys.add(key);
  }

  Collection<String> getKeys() {
    return Collections.checkedCollection(keys, String.class);
  }

  boolean contains(String key) {
    return keys.contains(key);
  }

}
