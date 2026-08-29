/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.repository;

import jakarta.persistence.Id;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Carries the primary key of a stored row onto the object that is about to replace it.
 * <p>
 * The configuration services identify their objects by a natural key - a project by its name, an ACL by the five
 * columns that make it up - and save whole objects rather than editing loaded ones. The document store this replaces
 * did exactly that: it looked the document up by its unique properties and, if one was there, overwrote its contents
 * while keeping the record. Copying the key across and letting {@code save()} merge reproduces that, and reproduces it
 * for the whole object rather than for the fields someone remembered to copy.
 * <p>
 * Reflection is the price of the surrogate key having no accessors, which is what keeps it out of the domain model's
 * public surface. One field, found once per class.
 */
public final class EntityKeys {

  private static final Map<Class<?>, Field> KEYS = new ConcurrentHashMap<>();

  private EntityKeys() {
  }

  /**
   * Give {@code incoming} the key of {@code existing}, so that saving it updates that row instead of inserting a
   * second one. A no-op in effect where the key is the natural one both already carry.
   */
  public static <T> T reuseKey(T existing, T incoming) {
    if(existing == null || incoming == null) return incoming;
    Field key = KEYS.computeIfAbsent(incoming.getClass(), EntityKeys::findKey);
    try {
      key.set(incoming, key.get(existing));
    } catch(IllegalAccessException e) {
      throw new IllegalStateException("Cannot copy the identifier of " + incoming.getClass().getName(), e);
    }
    return incoming;
  }

  private static Field findKey(Class<?> type) {
    for(Class<?> current = type; current != null; current = current.getSuperclass()) {
      for(Field field : current.getDeclaredFields()) {
        if(field.isAnnotationPresent(Id.class)) {
          field.setAccessible(true);
          return field;
        }
      }
    }
    throw new IllegalStateException("No @Id field on " + type.getName());
  }
}
