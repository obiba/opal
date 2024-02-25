/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain.security;

import com.google.common.collect.Lists;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.HasUniqueProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Persisted keystore.
 */
public class KeyStoreState extends AbstractTimestamped implements HasUniqueProperties {

  @NotNull
  @NotBlank
  private String name;

  @NotNull
  private byte[] keyStore;

  public KeyStoreState() {
  }

  public KeyStoreState(@NotNull String name) {
    this.name = name;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(name);
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
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
    if(!(o instanceof KeyStoreState)) return false;
    return name.equals(((KeyStoreState) o).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
