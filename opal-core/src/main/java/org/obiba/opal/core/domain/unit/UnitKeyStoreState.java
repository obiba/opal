/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.unit;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;

/**
 * Persisted keystore.
 */
@Entity
@Table(name = "unit_key_store", uniqueConstraints = { @UniqueConstraint(columnNames = { "unit" }) })
public class UnitKeyStoreState extends AbstractEntity {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @Column(nullable = false)
  private String unit;

  @Column(nullable = false, length = 1048576)
  private byte[] keyStore;

  //
  // Methods
  //

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public byte[] getKeyStore() {
    if(keyStore != null) {
      return Arrays.copyOf(keyStore, keyStore.length);
    }
    return null;
  }

  public void setKeyStore(byte[] keyStore) {
    if(keyStore != null) {
      this.keyStore = Arrays.copyOf(keyStore, keyStore.length);
    }
  }
}
