/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain.converter;

import com.google.common.base.Strings;
import jakarta.persistence.AttributeConverter;

/**
 * Stores an enumeration as its name in a plain {@code varchar}.
 * <p>
 * {@code @Enumerated(STRING)} would be the obvious way to say this, but it does not mean the same thing on every
 * server: Hibernate maps it to whatever native enum type the dialect offers, which is an inline
 * {@code enum ('A','B')} column on H2 and a {@code create type ... as enum} of its own on PostgreSQL. Both make the
 * schema server-specific and turn adding a constant into a schema migration. Converting here keeps the column a
 * varchar everywhere, which is what the changelog declares and what the previous document store held.
 */
public abstract class EnumNameConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

  private final Class<E> type;

  protected EnumNameConverter(Class<E> type) {
    this.type = type;
  }

  @Override
  public String convertToDatabaseColumn(E attribute) {
    return attribute == null ? null : attribute.name();
  }

  @Override
  public E convertToEntityAttribute(String dbData) {
    return Strings.isNullOrEmpty(dbData) ? null : Enum.valueOf(type, dbData);
  }
}
