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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.obiba.opal.core.domain.AbstractTimestamped;

import java.util.Arrays;

/**
 * Persisted keystore.
 */
@Entity
@Table(name = "keystore_states",
    uniqueConstraints = @UniqueConstraint(name = "uk_keystore_states_name", columnNames = "name"))
public class KeyStoreState extends AbstractTimestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @NotBlank
  @Column(nullable = false)
  private String name;

  /**
   * The serialised keystore itself: bytes, not text, and of no fixed size.
   */
  @NotNull
  @Lob
  @Column(name = "key_store")
  private byte[] keyStore;

  public KeyStoreState() {
  }

  public KeyStoreState(@NotNull String name) {
    this.name = name;
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
