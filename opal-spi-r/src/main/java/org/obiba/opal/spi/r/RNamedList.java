/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import java.util.List;
import java.util.Map;

/**
 * R named list implemented as a read-only map.
 *
 * @param <T>
 */
public interface RNamedList<T> extends Map<String, T> {

  List<String> getNames();

  default T put(String s, T value) {
    throw new IllegalArgumentException("Operation not available");
  }

  default T remove(Object o) {
    throw new IllegalArgumentException("Operation not available");
  }

  default void putAll(Map<? extends String, ? extends T> map) {
    throw new IllegalArgumentException("Operation not available");
  }

  default void clear() {
    throw new IllegalArgumentException("Operation not available");
  }

}
