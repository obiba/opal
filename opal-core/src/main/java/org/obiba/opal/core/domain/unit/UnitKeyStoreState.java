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
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;

import com.google.common.collect.Lists;

/**
 * Persisted keystore.
 */
public class UnitKeyStoreState extends AbstractTimestamped implements HasUniqueProperties {

  @NotNull
  @NotBlank
  private String unit;

  @NotNull
  private byte[] keyStore;

  public UnitKeyStoreState() {
  }

  public UnitKeyStoreState(@NotNull String unit) {
    this.unit = unit;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("unit");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(unit);
  }

  @NotNull
  public String getUnit() {
    return unit;
  }

  public void setUnit(@NotNull String unit) {
    this.unit = unit;
  }

  @NotNull
  public byte[] getKeyStore() {
    return Arrays.copyOf(keyStore, keyStore.length);
  }

  public void setKeyStore(@NotNull byte... keyStore) {
    this.keyStore = Arrays.copyOf(keyStore, keyStore.length);
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    //noinspection SimplifiableIfStatement
    if(!(o instanceof UnitKeyStoreState)) return false;
    return unit.equals(((UnitKeyStoreState) o).unit);
  }

  @Override
  public int hashCode() {
    return unit.hashCode();
  }
}
