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

import javax.annotation.Nonnull;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.opal.core.domain.AbstractOrientDbTimestampedEntity;
import org.obiba.opal.core.validator.Unique;

/**
 * Persisted keystore.
 */
@Unique(properties = "unit")
public class UnitKeyStoreState extends AbstractOrientDbTimestampedEntity {

  @Nonnull
  @NotBlank
  private String unit;

  @Nonnull
  private byte[] keyStore;

  @Nonnull
  public String getUnit() {
    return unit;
  }

  public void setUnit(@Nonnull String unit) {
    this.unit = unit;
  }

  @Nonnull
  public byte[] getKeyStore() {
    return Arrays.copyOf(keyStore, keyStore.length);
  }

  public void setKeyStore(@Nonnull byte... keyStore) {
    this.keyStore = Arrays.copyOf(keyStore, keyStore.length);
  }
}
